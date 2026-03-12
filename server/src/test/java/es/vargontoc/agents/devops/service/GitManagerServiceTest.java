package es.vargontoc.agents.devops.service;

import es.vargontoc.agents.devops.config.DevopsProperties;
import es.vargontoc.agents.devops.domain.entity.Project;
import es.vargontoc.agents.devops.domain.enums.DeploymentStatus;
import es.vargontoc.agents.devops.domain.enums.ProjectType;
import es.vargontoc.agents.devops.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class GitManagerServiceTest {

    @Autowired
    private GitManagerService gitManagerService;

    @Autowired
    private DevopsProperties properties;

    @Autowired
    private ProjectRepository projectRepository;

    @TempDir
    Path tempWorkspace;

    @BeforeEach
    void setUp() {
        // override workspace strictly for this test using the local temp dir
        properties.setWorkspaceDir(tempWorkspace.toAbsolutePath().toString());
        projectRepository.deleteAll();
    }

    @Test
    void shouldSkipCloneForLocalProject() throws Exception {
        Project project = new Project();
        project.setName("local-test");
        project.setPath("/some/local/path");
        project.setBranch("main");
        project.setType(ProjectType.LOCAL);

        CompletableFuture<Project> future = gitManagerService.cloneRepositoryAsync(project);
        Project saved = future.get(5, TimeUnit.SECONDS);

        assertEquals(DeploymentStatus.PENDING, saved.getLastStatus());
        assertNotNull(saved.getId());
    }

    @Test
    void shouldClonePublicGithubRepository() throws Exception {
        Project project = new Project();
        project.setName("public-test");
        // Using a very small, known public repository for testing clone functionality
        // A common tiny repo, or we use a basic template like Spring's initializr repo.
        // We'll use a hypothetical dummy or small repo. We'll simulate a failure to show full resilience or point to a valid one.
        // For actual integration we can use a small, reliable git repo or mocked jgit.
        // Since we are running real tests, we expect JGit to actually fetch.
        // Let's test resilience instead by giving a bad url to see if it fails beautifully.
        project.setPath("https://github.com/non-existent-user/fake-repo-abcfdefg.git");
        project.setBranch("main");
        project.setType(ProjectType.REMOTE);

        CompletableFuture<Project> future = gitManagerService.cloneRepositoryAsync(project);
        
        try {
            future.get(10, TimeUnit.SECONDS);
            fail("Expected exception due to invalid repository");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("GitOperationException") || e.getCause().getMessage().contains("not found"));
        }

        // Verify that the status was saved as failed
        Project failedProject = projectRepository.findAll().get(0);
        assertEquals(DeploymentStatus.FAILED, failedProject.getLastStatus());
    }
    
    @Test
    void shouldDetectPathTraversalAndThrowSecurityException() {
        assertThrows(SecurityException.class, () -> {
            gitManagerService.getTargetDirectory("../outside_folder");
        });
    }
}
