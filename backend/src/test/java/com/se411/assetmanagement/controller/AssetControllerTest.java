package com.se411.assetmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se411.assetmanagement.exception.AssetNotFoundException;
import com.se411.assetmanagement.model.Asset;
import com.se411.assetmanagement.service.AssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer unit tests: only the controller, the exception handler and the CORS config are loaded.
 * AssetService is replaced by a Mockito mock (@MockitoBean), so no database is involved.
 */
@WebMvcTest(AssetController.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssetService assetService;

    private Asset laptop;

    @BeforeEach
    void setUp() {
        laptop = new Asset("Dell XPS 15", "Laptop", "SN-33211", "Eyad Al-Shuhail", "In Use", LocalDate.of(2024, 3, 10));
        laptop.setId(1L);
    }

    // ---------- POST /api/assets ----------

    @Test
    @DisplayName("POST /api/assets returns 201 and the created asset")
    void createAsset_returns201() throws Exception {
        when(assetService.createAsset(any(Asset.class))).thenReturn(laptop);

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(laptop)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Dell XPS 15"))
                .andExpect(jsonPath("$.purchaseDate").value("2024-03-10"));
    }

    @Test
    @DisplayName("POST /api/assets with missing fields returns 400 and never calls the service")
    void createAsset_invalid_returns400() throws Exception {
        String invalidJson = "{\"name\":\"\",\"category\":\"Laptop\"}";

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.serialNumber").exists());

        verify(assetService, never()).createAsset(any());
    }

    @Test
    @DisplayName("POST /api/assets with a duplicate serial number returns 409")
    void createAsset_duplicateSerial_returns409() throws Exception {
        when(assetService.createAsset(any(Asset.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(laptop)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ---------- GET /api/assets/{id} ----------

    @Test
    @DisplayName("GET /api/assets/{id} returns 200 and the asset")
    void getAssetById_returns200() throws Exception {
        when(assetService.getAssetById(1L)).thenReturn(laptop);

        mockMvc.perform(get("/api/assets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.serialNumber").value("SN-33211"));
    }

    @Test
    @DisplayName("GET /api/assets/{id} returns 404 when the asset does not exist")
    void getAssetById_returns404() throws Exception {
        when(assetService.getAssetById(42L)).thenThrow(new AssetNotFoundException(42L));

        mockMvc.perform(get("/api/assets/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Asset not found with id 42"));
    }

    // ---------- GET /api/assets (paginated) ----------

    @Test
    @DisplayName("GET /api/assets?page=1&size=5 returns the requested page with paging metadata")
    void getAllAssets_returnsPage() throws Exception {
        Pageable pageable = PageRequest.of(1, 5, Sort.by("id"));
        when(assetService.getAllAssets(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(laptop), pageable, 12));

        mockMvc.perform(get("/api/assets").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Dell XPS 15"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(assetService).getAllAssets(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("GET /api/assets without params uses the defaults: page 0, size 5, sorted by id")
    void getAllAssets_usesDefaults() throws Exception {
        when(assetService.getAllAssets(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(laptop), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(assetService).getAllAssets(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        assertThat(captor.getValue().getSort().getOrderFor("id")).isNotNull();
    }

    // ---------- PUT /api/assets/{id} ----------

    @Test
    @DisplayName("PUT /api/assets/{id} returns 200 and the updated asset")
    void updateAsset_returns200() throws Exception {
        Asset updated = new Asset("Dell XPS 15", "Laptop", "SN-33211", "Mohammed", "Available", LocalDate.of(2024, 3, 10));
        updated.setId(1L);
        when(assetService.updateAsset(eq(1L), any(Asset.class))).thenReturn(updated);

        mockMvc.perform(put("/api/assets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTo").value("Mohammed"))
                .andExpect(jsonPath("$.status").value("Available"));
    }

    @Test
    @DisplayName("PUT /api/assets/{id} returns 404 when the asset does not exist")
    void updateAsset_returns404() throws Exception {
        when(assetService.updateAsset(eq(42L), any(Asset.class))).thenThrow(new AssetNotFoundException(42L));

        mockMvc.perform(put("/api/assets/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(laptop)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/assets/{id} with invalid body returns 400")
    void updateAsset_invalid_returns400() throws Exception {
        mockMvc.perform(put("/api/assets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(assetService, never()).updateAsset(any(), any());
    }

    // ---------- DELETE /api/assets/{id} ----------

    @Test
    @DisplayName("DELETE /api/assets/{id} returns 204")
    void deleteAsset_returns204() throws Exception {
        mockMvc.perform(delete("/api/assets/1"))
                .andExpect(status().isNoContent());

        verify(assetService).deleteAsset(1L);
    }

    @Test
    @DisplayName("DELETE /api/assets/{id} returns 404 when the asset does not exist")
    void deleteAsset_returns404() throws Exception {
        doThrow(new AssetNotFoundException(42L)).when(assetService).deleteAsset(42L);

        mockMvc.perform(delete("/api/assets/42"))
                .andExpect(status().isNotFound());
    }

    // ---------- CORS ----------

    @Test
    @DisplayName("CORS preflight from the React dev server is allowed")
    void corsPreflight_allowsFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/assets")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }
}
