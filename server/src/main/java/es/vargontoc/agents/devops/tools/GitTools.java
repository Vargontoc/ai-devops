package es.vargontoc.agents.devops.tools;

import es.vargontoc.agents.devops.domain.entity.Project;
import es.vargontoc.agents.devops.repository.ProjectRepository;
import es.vargontoc.agents.devops.service.GitManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Optional;
import java.util.function.Function;

@Configuration
public class GitTools {

    private static final Logger log = LoggerFactory.getLogger(GitTools.class);
    private final GitManagerService gitManagerService;
    private final ProjectRepository projectRepository;

    public GitTools(GitManagerService gitManagerService, ProjectRepository projectRepository) {
        this.gitManagerService = gitManagerService;
        this.projectRepository = projectRepository;
    }

    public record CloneRequest(String projectId) {}
    public record CloneResponse(String targetDirectoryPath, String status, String message) {}

    @Bean
    @Description("Clones or updates a remote Git repository for a specific project. " +
                 "Requires the exact projectId of the target project. Call this tool before deploying if the project is REMOTE.")
    public Function<CloneRequest, CloneResponse> cloneRepositoryTool() {
        return request -> {
            try {
                log.info("Tool triggered: Cloning/Updating project ID {}", request.projectId());
                Optional<Project> projectOpt = projectRepository.findById(request.projectId());
                
                if (projectOpt.isEmpty()) {
                    return new CloneResponse(null, "FAILED", "Project not found with ID: " + request.projectId());
                }

                Project project = projectOpt.get();
                // We wait for the future to finish to give immediate feedback to the LLM agent
                Project cloned = gitManagerService.cloneRepositoryAsync(project).join();

                return new CloneResponse(
                        cloned.getPath(), 
                        "SUCCESS", 
                        "Repository cloned successfully onto: " + cloned.getPath()
                );
            } catch (Exception e) {
                log.error("Clone Tool execution failed: {}", e.getMessage(), e);
                return new CloneResponse(null, "FAILED", "Error: " + e.getMessage());
            }
        };
    }
}
