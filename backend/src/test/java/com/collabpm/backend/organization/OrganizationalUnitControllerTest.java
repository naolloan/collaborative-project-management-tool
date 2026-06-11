package com.collabpm.backend.organization;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabpm.backend.config.SecurityConfig;
import com.collabpm.backend.organization.dto.OrganizationalUnitRequest;
import com.collabpm.backend.organization.dto.OrganizationalUnitResponse;
import com.collabpm.backend.organization.model.OrganizationalUnitType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrganizationalUnitController.class)
@Import(SecurityConfig.class)
class OrganizationalUnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrganizationalUnitService organizationalUnitService;

    @Test
    void listsOrganizationalUnitsForAuthenticatedUsers() throws Exception {
        given(organizationalUnitService.listActiveUnits()).willReturn(List.of(new OrganizationalUnitResponse(
            1L,
            "Digital Banking Department",
            "DEPARTMENT",
            "Owns digital delivery initiatives",
            true)));

        mockMvc.perform(get("/api/organizational-units").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Digital Banking Department"))
            .andExpect(jsonPath("$[0].type").value("DEPARTMENT"));
    }

    @Test
    void createsOrganizationalUnitForAuthenticatedUsers() throws Exception {
        OrganizationalUnitRequest request = new OrganizationalUnitRequest(
            "Adama Branch",
            OrganizationalUnitType.BRANCH,
            "Regional branch delivery work");
        given(organizationalUnitService.createUnit(eq(request), any(Authentication.class)))
            .willReturn(new OrganizationalUnitResponse(2L, request.name(), request.type().name(), request.description(), true));

        mockMvc.perform(post("/api/organizational-units")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Adama Branch",
                      "type": "BRANCH",
                      "description": "Regional branch delivery work"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Adama Branch"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void updatesOrganizationalUnitForAuthenticatedUsers() throws Exception {
        OrganizationalUnitRequest request = new OrganizationalUnitRequest(
            "Digital Channels Team",
            OrganizationalUnitType.TEAM,
            "Digital channel delivery");
        given(organizationalUnitService.updateUnit(eq(1L), eq(request), any(Authentication.class)))
            .willReturn(new OrganizationalUnitResponse(1L, request.name(), request.type().name(), request.description(), true));

        mockMvc.perform(patch("/api/organizational-units/1")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Digital Channels Team",
                      "type": "TEAM",
                      "description": "Digital channel delivery"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Digital Channels Team"))
            .andExpect(jsonPath("$.type").value("TEAM"));
    }

    @Test
    void deactivatesOrganizationalUnitForAuthenticatedUsers() throws Exception {
        given(organizationalUnitService.deactivateUnit(eq(1L), any(Authentication.class)))
            .willReturn(new OrganizationalUnitResponse(1L, "Legacy Team", "TEAM", null, false));

        mockMvc.perform(patch("/api/organizational-units/1/deactivate").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void rejectsAnonymousOrganizationalUnitRequests() throws Exception {
        mockMvc.perform(get("/api/organizational-units"))
            .andExpect(status().isUnauthorized());
    }
}
