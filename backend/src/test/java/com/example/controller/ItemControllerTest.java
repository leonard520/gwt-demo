package com.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.model.Item;
import com.example.service.ItemService;

/**
 * Controller tests for {@link ItemController}.
 * <p>
 * Uses {@code @WebMvcTest} to test the web layer in isolation with mocked services.
 * Spring Security is active with default test configuration (HTTP Basic auth).
 * {@code @WithMockUser} simulates an authenticated user for protected endpoints.
 * CSRF tokens are provided via {@code csrf()} for state-changing requests.
 * <p>
 * Source reference: com.example.server.controller.RpcControllerTest
 * (original tested session validation; rewrite tests REST CRUD behavior)
 * <p>
 * Tests cover:
 * <ul>
 *   <li>findAll (200) — returns list of items</li>
 *   <li>create (201) — returns created item with ID</li>
 *   <li>update (200) — returns updated item</li>
 *   <li>batch delete (204) — no content</li>
 *   <li>unauthenticated access (401)</li>
 * </ul>
 */
@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    // ==================== Find All Tests ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/items — returns 200 with list of items")
    void findAll_returns200WithItems() throws Exception {
        Item item1 = new Item("Item 1", "Description of Item 1", LocalDate.of(2025, 1, 30));
        item1.setId(1L);
        Item item2 = new Item("Foo", "Foo's description", LocalDate.of(2025, 1, 30));
        item2.setId(2L);
        List<Item> items = Arrays.asList(item1, item2);

        when(itemService.findAll()).thenReturn(items);

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Item 1"))
                .andExpect(jsonPath("$[0].description").value("Description of Item 1"))
                .andExpect(jsonPath("$[0].date").value("2025-01-30"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Foo"));

        verify(itemService).findAll();
    }

    // ==================== Create Tests ====================

    @Test
    @WithMockUser
    @DisplayName("POST /api/items — returns 201 with created item including ID")
    void create_returns201WithCreatedItem() throws Exception {
        Item created = new Item("New Item", "A new item description", LocalDate.of(2025, 1, 30));
        created.setId(10L);

        when(itemService.create(any(Item.class))).thenReturn(created);

        mockMvc.perform(post("/api/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Item\",\"description\":\"A new item description\",\"date\":\"2025-01-30\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("New Item"))
                .andExpect(jsonPath("$.description").value("A new item description"))
                .andExpect(jsonPath("$.date").value("2025-01-30"));

        verify(itemService).create(any(Item.class));
    }

    // ==================== Update Tests ====================

    @Test
    @WithMockUser
    @DisplayName("PUT /api/items/{id} — returns 200 with updated item")
    void update_returns200WithUpdatedItem() throws Exception {
        doNothing().when(itemService).update(anyLong(), any(Item.class));

        mockMvc.perform(put("/api/items/5")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Item\",\"description\":\"Updated description\",\"date\":\"2025-02-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.date").value("2025-02-01"));

        verify(itemService).update(anyLong(), any(Item.class));
    }

    // ==================== Delete Tests ====================

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/items — batch delete returns 204 No Content")
    void delete_returns204() throws Exception {
        doNothing().when(itemService).delete(any());

        mockMvc.perform(delete("/api/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[1,3,5]}"))
                .andExpect(status().isNoContent());

        verify(itemService).delete(any());
    }

    // ==================== Unauthenticated Access Tests ====================

    @Test
    @DisplayName("GET /api/items — unauthenticated returns 401")
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isUnauthorized());
    }
}
