package es.vargontoc.agents.devops.tools;

import es.vargontoc.agents.devops.domain.entity.Project;
import es.vargontoc.agents.devops.domain.enums.ProjectType;
import es.vargontoc.agents.devops.service.GitManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.util.StringUtils;

import java.util.function.Function;

@Configuration
public class GitTools {

    private static final Logger log = LoggerFactory.getLogger(GitTools.class);
    private final GitManagerService gitManagerService;

    public GitTools(GitManagerService gitManagerService) {
        this.gitManagerService = gitManagerService;
    }

    public record CloneRequest(String projectName, String repositoryUrl, String token) {}
    public record CloneResponse(String projectId, String status, String message, String localPath) {}

    @Bean
    @Description("Clones a remote git repository into the local /workspace volume. " +
            "Requires the project name and the repository URL. " +
            "Optionally accepts a token for private repositories.")
    public Function<CloneRequest, CloneResponse> cloneRepositoryTool() {
        return request -> {
            try {
                Project project = new Project();
                project.setName(request.projectName());
                project.setPath(request.repositoryUrl());
                project.setType(ProjectType.REMOTE);
                
                if (StringUtils.hasText(request.token())) {
                    project.setEncryptedToken(request.token());
                }

                log.info("Tool triggered: Cloning repository {} for project {}", request.repositoryUrl(), request.projectName());
                
                // Triggering async clone but waiting for it to provide immediate feedback to the LLM agent
                // Depending on the Agent flow, we could just return "cloning started" and let it check status later,
                // but for simplicity in this tool we'll wait an acceptable amount of time or return the future's result.
                Project result = gitManagerService.cloneRepositoryAsync(project).join();
                
                return new CloneResponse(
                        result.getId(), 
                        result.getLastStatus().name(), 
                        "Repository cloning process executed.", 
                        result.getPath()
                );
            } catch (Exception e) {
                log.error("Tool execution failed: {}", e.getMessage(), e);
                return new CloneResponse(null, "FAILED", "Error: " + e.getMessage(), null);
            }
        };
    }
}
