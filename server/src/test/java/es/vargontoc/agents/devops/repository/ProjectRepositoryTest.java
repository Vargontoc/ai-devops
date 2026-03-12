package es.vargontoc.agents.devops.repository;

import es.vargontoc.agents.devops.domain.entity.Project;
import es.vargontoc.agents.devops.domain.enums.DeploymentStatus;
import es.vargontoc.agents.devops.domain.enums.ProjectType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldSaveAndRetrieveProject() {
        Project project = new Project();
        project.setName("test-project");
        project.setPath("https://github.com/test/repo.git");
        project.setBranch("main");
        project.setType(ProjectType.REMOTE);
        project.setLastStatus(DeploymentStatus.PENDING);

        Project saved = projectRepository.save(project);
        
        assertNotNull(saved.getId());

        Optional<Project> retrieved = projectRepository.findById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("test-project", retrieved.get().getName());
        assertEquals("main", retrieved.get().getBranch());
        assertEquals(ProjectType.REMOTE, retrieved.get().getType());
        assertEquals(DeploymentStatus.PENDING, retrieved.get().getLastStatus());
    }
}
