package es.vargontoc.agents.devops.service;

import es.vargontoc.agents.devops.domain.entity.DeploymentLog;
import es.vargontoc.agents.devops.domain.entity.Project;
import es.vargontoc.agents.devops.domain.enums.DeploymentStatus;
import es.vargontoc.agents.devops.domain.enums.ProjectType;
import es.vargontoc.agents.devops.repository.DeploymentLogRepository;
import es.vargontoc.agents.devops.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DeploymentServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private DeploymentLogRepository deploymentLogRepository;

    private org.springframework.ai.chat.client.ChatClient.Builder chatClientBuilder;

    @Mock
    private org.springframework.ai.chat.client.ChatClient chatClient;

    @Mock
    private WebSocketNotificationService wsNotificationService;

    private DeploymentService deploymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatClientBuilder = Mockito.mock(org.springframework.ai.chat.client.ChatClient.Builder.class, Mockito.RETURNS_SELF);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        
        deploymentService = new DeploymentService(projectRepository, deploymentLogRepository, chatClientBuilder, wsNotificationService);
    }

    @Test
    void shouldExecuteDeploymentSuccessfully(@TempDir Path tempDir) throws Exception {
        // Arrange
        Project project = new Project();
        project.setId("proj-1");
        project.setName("test-deploy");
        project.setPath(tempDir.toAbsolutePath().toString());
        project.setBranch("main");
        project.setType(ProjectType.LOCAL);

        // Create a dummy deploy.md in the temp directory
        File deployFile = new File(tempDir.toFile(), "deploy.md");
        
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String echoCommand = isWindows ? "echo \"hello deployment windows\"" : "echo \"hello deployment linux\"";
        
        Files.writeString(deployFile.toPath(), "```bash\n" + echoCommand + "\n```");

        DeploymentLog expectedLog = new DeploymentLog();
        expectedLog.setId("log-1");
        expectedLog.setExitCode(0);

        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(deploymentLogRepository.save(any(DeploymentLog.class))).thenAnswer(invocation -> {
            DeploymentLog saved = invocation.getArgument(0);
            saved.setId("log-1");
            return saved;
        });

        // Act
        CompletableFuture<DeploymentLog> future = deploymentService.executeDeploymentAsync(project);
        DeploymentLog result = future.get(5, TimeUnit.SECONDS);

        // Assert
        assertEquals(0, result.getExitCode());
        assertTrue(result.getFullLog().contains("hello deployment"));
        Mockito.verify(projectRepository, Mockito.atLeast(2)).save(project);
        assertEquals(DeploymentStatus.SUCCESS, project.getLastStatus());
    }

    @Test
    void shouldFailWhenProjectDirDoesNotExist() throws Exception {
        // Arrange
        Project project = new Project();
        project.setId("proj-2");
        project.setName("invalid-deploy");
        project.setPath("/path/that/does/not/exist/surely");
        project.setBranch("main");
        project.setType(ProjectType.LOCAL);

        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(deploymentLogRepository.save(any(DeploymentLog.class))).thenAnswer(i -> {
            DeploymentLog logs = i.getArgument(0);
            logs.setId("log-fake");
            return logs;
        });

        // Act
        CompletableFuture<DeploymentLog> future = deploymentService.executeDeploymentAsync(project);
        DeploymentLog result = future.get(5, TimeUnit.SECONDS);

        // Assert
        assertEquals(-1, result.getExitCode());
        assertTrue(result.getFullLog().contains("does not exist"));
        assertEquals(DeploymentStatus.FAILED, project.getLastStatus());
    }

    @Test
    void shouldFailWhenDeployMdIsMissing(@TempDir Path tempDir) throws Exception {
        // Arrange
        Project project = new Project();
        project.setId("proj-3");
        project.setName("no-deploy-md");
        project.setPath(tempDir.toAbsolutePath().toString()); // Dir exists but no deploy.md
        project.setBranch("main");
        project.setType(ProjectType.LOCAL);

        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(deploymentLogRepository.save(any(DeploymentLog.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        CompletableFuture<DeploymentLog> future = deploymentService.executeDeploymentAsync(project);
        DeploymentLog result = future.get(5, TimeUnit.SECONDS);

        // Assert
        assertEquals(-1, result.getExitCode());
        assertTrue(result.getFullLog().contains("No deploy.md found"));
        assertEquals(DeploymentStatus.FAILED, project.getLastStatus());
    }
}
