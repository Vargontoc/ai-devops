package es.vargontoc.agents.devops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.agents.devops.domain.dto.ProjectCreateRequest;
import es.vargontoc.agents.devops.domain.entity.Project;
import es.vargontoc.agents.devops.domain.enums.DeploymentStatus;
import es.vargontoc.agents.devops.domain.enums.ProjectType;
import es.vargontoc.agents.devops.repository.ProjectRepository;
import es.vargontoc.agents.devops.service.GitManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private GitManagerService gitManagerService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        Mockito.reset(projectRepository, gitManagerService);
    }

    @Test
    void shouldReturnCreatedWhenLocalProject() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "my-local-app",
                "/some/path",
                "main",
                ProjectType.LOCAL,
                null
        );

        Project mockedProject = new Project();
        mockedProject.setId("123");
        mockedProject.setName("my-local-app");
        mockedProject.setPath("/some/path");
        mockedProject.setBranch("main");
        mockedProject.setType(ProjectType.LOCAL);
        mockedProject.setLastStatus(DeploymentStatus.PENDING);

        Mockito.when(projectRepository.save(any(Project.class))).thenReturn(mockedProject);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.branch").value("main"))
                .andExpect(jsonPath("$.type").value(ProjectType.LOCAL.name()));

        // Local project shouldn't trigger git clone
        Mockito.verify(gitManagerService, Mockito.never()).cloneRepositoryAsync(any());
    }

    @Test
    void shouldReturnAcceptedWhenRemoteProjectAndTriggerClone() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "my-remote-app",
                "https://github.com/test",
                "dev",
                ProjectType.REMOTE,
                "token123"
        );

        Project mockedProject = new Project();
        mockedProject.setId("456");
        mockedProject.setName("my-remote-app");
        mockedProject.setPath("https://github.com/test");
        mockedProject.setBranch("dev");
        mockedProject.setType(ProjectType.REMOTE);
        mockedProject.setLastStatus(DeploymentStatus.CLONING);

        // First save during creation, second save when updating status to cloning
        Mockito.when(projectRepository.save(any(Project.class))).thenReturn(mockedProject);
        Mockito.when(gitManagerService.cloneRepositoryAsync(any(Project.class)))
                .thenReturn(CompletableFuture.completedFuture(mockedProject));

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value("456"))
                .andExpect(jsonPath("$.branch").value("dev"))
                .andExpect(jsonPath("$.lastStatus").value(DeploymentStatus.CLONING.name()));

        Mockito.verify(gitManagerService, Mockito.times(1)).cloneRepositoryAsync(any());
        Mockito.verify(projectRepository, Mockito.times(2)).save(any(Project.class));
    }

    @Test
    void shouldReturnConflictWhenNameExists() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "existing-app",
                "https://github.com/new-path",
                "main",
                ProjectType.REMOTE,
                null
        );

        Mockito.when(projectRepository.existsByName("existing-app")).thenReturn(true);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnConflictWhenPathExists() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "new-app",
                "https://github.com/existing-path",
                "main",
                ProjectType.REMOTE,
                null
        );

        Mockito.when(projectRepository.existsByName(any())).thenReturn(false);
        Mockito.when(projectRepository.existsByPath("https://github.com/existing-path")).thenReturn(true);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
