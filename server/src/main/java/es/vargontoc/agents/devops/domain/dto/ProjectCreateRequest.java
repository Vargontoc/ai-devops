package es.vargontoc.agents.devops.domain.dto;

import es.vargontoc.agents.devops.domain.enums.ProjectType;

public record ProjectCreateRequest(
        String name,
        String path,
        String branch,
        ProjectType type,
        String token
) {
}
