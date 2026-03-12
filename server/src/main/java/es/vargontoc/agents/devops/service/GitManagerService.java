package es.vargontoc.agents.devops.service;

import es.vargontoc.agents.devops.config.DevopsProperties;
import es.vargontoc.agents.devops.domain.entity.Project;
import es.vargontoc.agents.devops.domain.enums.DeploymentStatus;
import es.vargontoc.agents.devops.domain.enums.ProjectType;
import es.vargontoc.agents.devops.exception.GitOperationException;
import es.vargontoc.agents.devops.repository.ProjectRepository;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@Service
public class GitManagerService {

    private static final Logger log = LoggerFactory.getLogger(GitManagerService.class);

    private final DevopsProperties properties;
    private final ProjectRepository projectRepository;

    public GitManagerService(DevopsProperties properties, ProjectRepository projectRepository) {
        this.properties = properties;
        this.projectRepository = projectRepository;
    }

    @Async
    public CompletableFuture<Project> cloneRepositoryAsync(Project project) {
        log.info("Starting async clone for project: {}", project.getName());
        try {
            validatePath(project.getPath());

            if (project.getType() == ProjectType.LOCAL) {
                log.info("Project {} is local, skipping clone.", project.getName());
                project.setLastStatus(DeploymentStatus.PENDING);
                return CompletableFuture.completedFuture(projectRepository.save(project));
            }

            File targetDir = getTargetDirectory(project.getName());
            if (targetDir.exists() && targetDir.isDirectory() && targetDir.list() != null && targetDir.list().length > 0) {
                log.warn("Directory {} already exists and is not empty. Assuming repository is already cloned.", targetDir.getAbsolutePath());
                // In a real scenario we might want to do a git pull instead.
                project.setLastStatus(DeploymentStatus.PENDING);
                project.setPath(targetDir.getAbsolutePath());
                return CompletableFuture.completedFuture(projectRepository.save(project));
            }

            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(project.getPath())
                    .setDirectory(targetDir);

            if (StringUtils.hasText(project.getEncryptedToken())) {
                // In a real application, decrypt the token first.
                // Assuming pattern: "username:token" or just "token" mapped to a dummy username for PATs.
                // Here we use a simplistic approach of assuming the token itself is passed for PAT authentication.
                cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", project.getEncryptedToken()));
            }

            try (Git git = cloneCommand.call()) {
                log.info("Successfully cloned {} to {}", project.getName(), targetDir.getAbsolutePath());
                project.setPath(targetDir.getAbsolutePath());
                project.setLastStatus(DeploymentStatus.PENDING);
                return CompletableFuture.completedFuture(projectRepository.save(project));
            } catch (GitAPIException e) {
                log.error("Failed to clone repository {}", project.getName(), e);
                project.setLastStatus(DeploymentStatus.FAILED);
                projectRepository.save(project);
                throw new GitOperationException("Failed to clone repository: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Unexpected error during git clone for project {}", project.getName(), e);
            project.setLastStatus(DeploymentStatus.FAILED);
            projectRepository.save(project);
            CompletableFuture<Project> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    private void validatePath(String urlOrPath) {
        if (!StringUtils.hasText(urlOrPath)) {
            throw new IllegalArgumentException("Project URL or path cannot be empty.");
        }
    }

    public File getTargetDirectory(String projectName) {
        Path workspacePath = Paths.get(properties.getWorkspaceDir()).toAbsolutePath().normalize();
        
        // Resolve the project name against workspace path
        Path projectPath = workspacePath.resolve(projectName).toAbsolutePath().normalize();

        // Security check: ensure the resolved path starts with the workspace path
        if (!projectPath.startsWith(workspacePath)) {
            throw new SecurityException("Project directory path traversal detected!");
        }

        return projectPath.toFile();
    }
}
