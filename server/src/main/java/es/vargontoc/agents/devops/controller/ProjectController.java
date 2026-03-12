package es.vargontoc.agents.devops.controller;

import es.vargontoc.agents.devops.domain.dto.ProjectCreateRequest;
import es.vargontoc.agents.devops.domain.dto.ProjectDto;
import es.vargontoc.agents.devops.domain.entity.Project;
import es.vargontoc.agents.devops.domain.enums.DeploymentStatus;
import es.vargontoc.agents.devops.domain.enums.ProjectType;
import es.vargontoc.agents.devops.repository.ProjectRepository;
import es.vargontoc.agents.devops.service.GitManagerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects")
@CrossOrigin(origins = "*") // Allows Vue frontend to connect
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final GitManagerService gitManagerService;

    public ProjectController(ProjectRepository projectRepository, GitManagerService gitManagerService) {
        this.projectRepository = projectRepository;
        this.gitManagerService = gitManagerService;
    }

    @GetMapping
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ProjectDto getProject(@PathVariable String id) {
        return projectRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@RequestBody ProjectCreateRequest request) {
        Project project = new Project();
        project.setName(request.name());
        project.setPath(request.path());
        project.setBranch(request.branch());
        project.setType(request.type());
        project.setEncryptedToken(request.token());
        project.setLastStatus(DeploymentStatus.PENDING); // Initial status before async evaluation

        Project saved = projectRepository.save(project);

        if (saved.getType() == ProjectType.REMOTE) {
            // Trigger async clone for remote repositories
            saved.setLastStatus(DeploymentStatus.CLONING);
            projectRepository.save(saved);
            gitManagerService.cloneRepositoryAsync(saved);
            return ResponseEntity.accepted().body(mapToDto(saved));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(saved));
    }

    private ProjectDto mapToDto(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getPath(),
                project.getBranch(),
                project.getType(),
                project.getLastStatus()
        );
    }
}
