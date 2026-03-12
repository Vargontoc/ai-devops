package es.vargontoc.agents.devops.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "devops")
public class DevopsProperties {

    /**
     * Absolute path directly inside the host or container volume where projects will be cloned/stored.
     */
    private String workspaceDir = "/workspace";

    public String getWorkspaceDir() {
        return workspaceDir;
    }

    public void setWorkspaceDir(String workspaceDir) {
        this.workspaceDir = workspaceDir;
    }
}
