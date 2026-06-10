package io.github.AdityaPatankar71.batchpilot.sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end check of the batch-pilot security model on the sample app:
 * role enforcement and CSRF on write actions.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BatchPilotSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void jobs_unauthenticated_isUnauthorized() throws Exception {
        mockMvc.perform(get("/batch-pilot/api/jobs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jobs_withoutRole_isForbidden() throws Exception {
        mockMvc.perform(get("/batch-pilot/api/jobs").with(httpBasic("viewer", "viewer")))
                .andExpect(status().isForbidden());
    }

    @Test
    void jobs_withRole_isOk() throws Exception {
        mockMvc.perform(get("/batch-pilot/api/jobs").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());
    }

    @Test
    void writeAction_withoutCsrf_isForbidden() throws Exception {
        mockMvc.perform(post("/batch-pilot/api/executions/1/restart").with(httpBasic("admin", "admin")))
                .andExpect(status().isForbidden());
    }

    @Test
    void restartCompletedExecution_withRoleAndCsrf_isConflict() throws Exception {
        // Execution 1 is the successful job, whose instance is complete and not restartable.
        mockMvc.perform(post("/batch-pilot/api/executions/1/restart")
                        .with(httpBasic("admin", "admin"))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }
}
