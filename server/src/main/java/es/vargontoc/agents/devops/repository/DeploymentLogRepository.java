package es.vargontoc.agents.devops.repository;

import es.vargontoc.agents.devops.domain.entity.DeploymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeploymentLogRepository extends JpaRepository<DeploymentLog, String> {
    
    List<DeploymentLog> findByProjectIdOrderByTimestampDesc(String projectId);
}
