package es.vargontoc.agents.devops.repository;

import es.vargontoc.agents.devops.domain.entity.DeploymentLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DeploymentLogRepositoryTest {

    @Autowired
    private DeploymentLogRepository deploymentLogRepository;

    @Test
    void shouldSaveAndFindLogsByProjectIdOrdered() throws InterruptedException {
        String projectId = "proj-123";

        DeploymentLog log1 = new DeploymentLog();
        log1.setProjectId(projectId);
        log1.setExitCode(0);
        log1.setFullLog("Starting deployment...");
        
        deploymentLogRepository.save(log1);
        
        Thread.sleep(100); // ensure timestamp ordering

        DeploymentLog log2 = new DeploymentLog();
        log2.setProjectId(projectId);
        log2.setExitCode(1);
        log2.setFullLog("Failed deployment...");
        
        deploymentLogRepository.save(log2);

        List<DeploymentLog> logs = deploymentLogRepository.findByProjectIdOrderByTimestampDesc(projectId);
        
        assertEquals(2, logs.size());
        assertEquals(1, logs.get(0).getExitCode()); // Most recent should be log2 (exit code 1)
        assertEquals(0, logs.get(1).getExitCode());
    }
}
