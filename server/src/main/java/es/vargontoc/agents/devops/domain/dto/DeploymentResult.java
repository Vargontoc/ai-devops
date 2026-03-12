package es.vargontoc.agents.devops.domain.dto;

public record DeploymentResult(
        int exitCode,
        String outputLog,
        String errorLog
) {
}
