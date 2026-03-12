package es.vargontoc.agents.devops.domain.dto;

import es.vargontoc.agents.devops.domain.enums.DeploymentStatus;
import es.vargontoc.agents.devops.domain.enums.ProjectType;

public record ProjectDto(
        String id,
        String name,
        String path,
        ProjectType type,
        DeploymentStatus lastStatus
) {
}
