package es.vargontoc.agents.devops.service;

import es.vargontoc.agents.devops.domain.dto.DeploymentResult;
import es.vargontoc.agents.devops.domain.entity.DeploymentLog;
import es.vargontoc.agents.devops.domain.entity.Project;
import es.vargontoc.agents.devops.domain.enums.DeploymentStatus;
import es.vargontoc.agents.devops.repository.DeploymentLogRepository;
import es.vargontoc.agents.devops.repository.ProjectRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class DeploymentService {

    private static final Logger log = LoggerFactory.getLogger(DeploymentService.class);
    
    // Commands file name expected in the root of the project
    private static final String DEPLOY_FILE_NAME = "deploy.md";

    private final ProjectRepository projectRepository;
    private final DeploymentLogRepository deploymentLogRepository;
    private final ChatClient chatClient;
    private final WebSocketNotificationService wsNotificationService;

    public DeploymentService(ProjectRepository projectRepository, 
                             DeploymentLogRepository deploymentLogRepository,
                             ChatClient.Builder chatClientBuilder,
                             WebSocketNotificationService wsNotificationService) {
        this.projectRepository = projectRepository;
        this.deploymentLogRepository = deploymentLogRepository;
        this.wsNotificationService = wsNotificationService;
        this.chatClient = chatClientBuilder
            .defaultSystem("Eres el DevOps AI Orchestrator. Tu misión es cumplir con las peticiones de despliegue clonando e instalando el proyecto mediante las herramientas.")
            .defaultFunctions("deployProjectTool", "cloneRepositoryTool")
            .build();
    }

    @Async
    public CompletableFuture<Void> executeAgenticDeployment(String projectId) {
        log.info("Agentic deployment triggered for project ID: {}", projectId);
        
        return projectRepository.findById(projectId).map(project -> {
            wsNotificationService.sendProjectStatus(projectId, "DEPLOYING");
            
            String userPrompt = String.format(
                "Inicia el proceso de despliegue para el proyecto. ID: %s, Nombre: %s, Repo/Path: %s, Rama: %s, Tipo: %s. " +
                "Primero, usa la tool 'cloneRepositoryTool' si el proyecto es REMOTE para clonarlo/actualizarlo. " +
                "Luego, usa la tool 'deployProjectTool' para ejecutar el deploy.md del repositorio. " +
                "Mantenme informado paso a paso.",
                project.getId(), project.getName(), project.getPath(), project.getBranch(), project.getType()
            );

            try {
                Flux<String> responseStream = chatClient.prompt()
                    .user(userPrompt)
                    .stream()
                    .content();

                responseStream.subscribe(
                    contentFragment -> {
                        if (contentFragment != null) {
                            wsNotificationService.sendProjectLog(projectId, contentFragment);
                        }
                    },
                    error -> {
                        log.error("AI stream error for project {}: {}", projectId, error.getMessage(), error);
                        wsNotificationService.sendProjectStatus(projectId, "FAILED: Agente detenido por error de conexión o tiempo de espera.");
                        updateProjectStatus(project, DeploymentStatus.FAILED);
                    },
                    () -> {
                        log.info("Agent finished reasoning and executing for project {}", projectId);
                        wsNotificationService.sendProjectStatus(projectId, "FINISHED");
                    }
                );
            } catch (Exception e) {
                log.error("Failed to start agent for project {}: {}", projectId, e.getMessage());
                wsNotificationService.sendProjectStatus(projectId, "FAILED: Agente detenido al inicio.");
                updateProjectStatus(project, DeploymentStatus.FAILED);
            }
            return CompletableFuture.completedFuture((Void) null);
        }).orElseGet(() -> {
            log.warn("Project {} not found for deployment.", projectId);
            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Identifies OS and starts the deployment using the rules specified in the deploy file.
     * Async returns the DeploymentLog containing the final execution status.
     */
    @Async
    public CompletableFuture<DeploymentLog> executeDeploymentAsync(Project project) {
        log.info("Starting deployment execution for project: {}", project.getName());
        updateProjectStatus(project, DeploymentStatus.IN_PROGRESS);

        try {
            File projectDir = new File(project.getPath());
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                throw new IllegalStateException("Project directory does not exist or is inaccessible: " + project.getPath());
            }

            File deployFile = new File(projectDir, DEPLOY_FILE_NAME);
            if (!deployFile.exists()) {
                throw new IllegalStateException("No " + DEPLOY_FILE_NAME + " found in project root: " + projectDir.getAbsolutePath());
            }

            // Read deploy.md to extract commands. For simplicity in Sprint 3 base module, 
            // we will pass the file to a shell execution if OS allows, or just parse basic strings.
            // A more complex parser for Markdown blocks -> commands will be built later.
            // For now, if running on windows we use cmd, on linux we use bash.
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(projectDir);

            // Command logic: 
            // We read the markdown looking for script blocks or just execute a predefined wrapper
            // For Sprint 3 requirement, we read the deploy.md content and execute it.
            // Assuming the whole file is an executable shell script for now or parsing markdown for codeblocks.
            String scriptContents = extractScriptFromMarkdown(deployFile.toPath());
            
            // Writing a temporary executable script to run safely.
            File tempScript = File.createTempFile("devops-deploy-", isWindows ? ".bat" : ".sh", projectDir);
            tempScript.deleteOnExit();
            Files.writeString(tempScript.toPath(), scriptContents);
            
            if (!isWindows) {
                tempScript.setExecutable(true);
            }

            if (isWindows) {
                processBuilder.command("cmd.exe", "/c", tempScript.getAbsolutePath());
            } else {
                processBuilder.command("bash", tempScript.getAbsolutePath());
            }

            log.info("Deploying via script: {}", tempScript.getAbsolutePath());
            Process process = processBuilder.start();

            StringBuilder outputLog = new StringBuilder();
            StringBuilder errorLog = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLog.append(line).append("\n");
                }
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorLog.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            DeploymentResult result = new DeploymentResult(exitCode, outputLog.toString(), errorLog.toString());
            
            DeploymentLog logEntry = saveDeploymentLog(project, result);
            
            if (exitCode == 0) {
                updateProjectStatus(project, DeploymentStatus.SUCCESS);
            } else {
                updateProjectStatus(project, DeploymentStatus.FAILED);
            }
            
            return CompletableFuture.completedFuture(logEntry);

        } catch (Exception e) {
            log.error("Deployment failed exceptionally for project: {}", project.getName(), e);
            updateProjectStatus(project, DeploymentStatus.FAILED);
            DeploymentResult failedResult = new DeploymentResult(-1, "", "Exception during deployment: " + e.getMessage());
            DeploymentLog logEntry = saveDeploymentLog(project, failedResult);
            return CompletableFuture.completedFuture(logEntry);
        }
    }

    private void updateProjectStatus(Project project, DeploymentStatus status) {
        project.setLastStatus(status);
        projectRepository.save(project);
    }

    private DeploymentLog saveDeploymentLog(Project project, DeploymentResult result) {
        DeploymentLog logEntry = new DeploymentLog();
        logEntry.setProjectId(project.getId());
        logEntry.setTimestamp(LocalDateTime.now());
        logEntry.setExitCode(result.exitCode());
        logEntry.setFullLog("OUTPUT:\n" + result.outputLog() + "\nERRORS:\n" + result.errorLog());
        
        // Initializing without AI suggestions for now
        return deploymentLogRepository.save(logEntry);
    }

    /**
     * Reads the deploy.md and extracts code blocks.
     * Temporary simple parser: returns only the content between ```bash or ```sh or ``` 
     */
    private String extractScriptFromMarkdown(Path deployFilePath) throws Exception {
        String content = Files.readString(deployFilePath);
        StringBuilder script = new StringBuilder();
        boolean inCodeBlock = false;
        
        for (String line : content.split("\n")) {
            if (line.trim().startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                continue; // skip the syntax marker
            }
            if (inCodeBlock) {
                script.append(line).append("\n");
            }
        }
        
        // Fallback: If no codeblocks found, we assume the whole file might just be plain text commands
        if (script.length() == 0) {
            return content;
        }
        
        return script.toString();
    }
}
