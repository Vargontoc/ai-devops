package es.vargontoc.agents.devops.tools;

import es.vargontoc.agents.devops.domain.entity.DeploymentLog;
import es.vargontoc.agents.devops.domain.entity.Project;
import es.vargontoc.agents.devops.repository.ProjectRepository;
import es.vargontoc.agents.devops.service.DeploymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Optional;
import java.util.function.Function;

@Configuration
public class DeployTools {

    private static final Logger log = LoggerFactory.getLogger(DeployTools.class);
    
    private final DeploymentService deploymentService;
    private final ProjectRepository projectRepository;

    public DeployTools(DeploymentService deploymentService, ProjectRepository projectRepository) {
        this.deploymentService = deploymentService;
        this.projectRepository = projectRepository;
    }

    public record DeployRequest(String projectId) {}
    public record DeployResponse(String deploymentId, String status, int exitCode, String message) {}

    @Bean
    @Description("Triggers the deployment of a specific project by reading and executing its deploy.md instructions in the workspace. " +
                 "Requires the exact projectId of the target project. Wait for the result as it might take some seconds depending on the build.")
    public Function<DeployRequest, DeployResponse> deployProjectTool() {
        return request -> {
            try {
                log.info("Tool triggered: Deploying project ID {}", request.projectId());
                
                Optional<Project> projectOpt = projectRepository.findById(request.projectId());
                
                if (projectOpt.isEmpty()) {
                    return new DeployResponse(null, "FAILED", -1, "Project not found with ID: " + request.projectId());
                }

                Project project = projectOpt.get();

                // Triggering async deployment but waiting for it to provide immediate feedback to the Agent
                DeploymentLog resultLog = deploymentService.executeDeploymentAsync(project).join();
                
                String status = resultLog.getExitCode() == 0 ? "SUCCESS" : "FAILED";
                String msg = resultLog.getExitCode() == 0 ? 
                             "Deployment executed successfully." : 
                             "Deployment failed. Check the logs for errors.";
                             
                return new DeployResponse(
                        resultLog.getId(), 
                        status, 
                        resultLog.getExitCode(), 
                        msg
                );
            } catch (Exception e) {
                log.error("Deploy Tool execution failed: {}", e.getMessage(), e);
                return new DeployResponse(null, "FAILED", -1, "Error: " + e.getMessage());
            }
        };
    }
}
