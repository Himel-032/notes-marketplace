package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.dto.admin.AdminUserDto;
import com.notes.notesmarketplace.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    void getAllUsers() throws Exception {
        AdminUserDto user = AdminUserDto.builder()
                .id(1L)
                .name("Admin")
                .email("admin@mail.com")
                .enabled(true)
                .roles(Set.of("ADMIN"))
                .build();
        when(adminService.getUsers(any())).thenReturn(new PageImpl<>(List.of(user)));

        mockMvc.perform(get("/api/admin/users").with(user("admin@mail.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].email").value("admin@mail.com"));
    }

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    void deleteUser() throws Exception {
        doNothing().when(adminService).deleteUser(3L);

        mockMvc.perform(delete("/api/admin/users/3").with(user("admin@mail.com").roles("ADMIN")))
                .andExpect(status().isOk());

        verify(adminService).deleteUser(3L);
    }
}
