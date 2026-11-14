package com.se411.assetmanagement.service;

import com.se411.assetmanagement.exception.AssetNotFoundException;
import com.se411.assetmanagement.model.Asset;
import com.se411.assetmanagement.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for the service layer: the repository is a Mockito mock,
 * so no Spring context and no database are started.
 */
@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    private Asset laptop;

    @BeforeEach
    void setUp() {
        laptop = new Asset("Dell XPS 15", "Laptop", "SN-33211", "Eyad Al-Shuhail", "In Use", LocalDate.of(2024, 3, 10));
        laptop.setId(1L);
    }

    @Test
    @DisplayName("createAsset saves the asset and returns it with a generated id")
    void createAsset_savesAndReturnsAsset() {
        Asset input = new Asset("ThinkPad T14", "Laptop", "SN-44512", "Hamad", "Available", LocalDate.of(2024, 2, 20));
        input.setId(99L); // client-sent id must be ignored
        Asset saved = new Asset("ThinkPad T14", "Laptop", "SN-44512", "Hamad", "Available", LocalDate.of(2024, 2, 20));
        saved.setId(5L);
        when(assetRepository.save(any(Asset.class))).thenReturn(saved);

        Asset result = assetService.createAsset(input);

        ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("ThinkPad T14");
    }

    @Test
    @DisplayName("getAssetById returns the asset when it exists")
    void getAssetById_found() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(laptop));

        Asset result = assetService.getAssetById(1L);

        assertThat(result).isSameAs(laptop);
        verify(assetRepository).findById(1L);
    }

    @Test
    @DisplayName("getAssetById throws AssetNotFoundException when the id does not exist")
    void getAssetById_notFound() {
        when(assetRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.getAssetById(42L))
                .isInstanceOf(AssetNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    @DisplayName("getAllAssets passes the Pageable to the repository and returns the page")
    void getAllAssets_returnsPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Asset> page = new PageImpl<>(List.of(laptop), pageable, 12);
        when(assetRepository.findAll(pageable)).thenReturn(page);

        Page<Asset> result = assetService.getAllAssets(pageable);

        assertThat(result.getContent()).containsExactly(laptop);
        assertThat(result.getTotalElements()).isEqualTo(12);
        assertThat(result.getTotalPages()).isEqualTo(3);
        verify(assetRepository).findAll(pageable);
    }

    @Test
    @DisplayName("updateAsset copies all fields onto the existing asset and saves it")
    void updateAsset_found() {
        Asset changes = new Asset("Dell XPS 15 (2024)", "Laptop", "SN-33211-B", "Mohammed", "Under Maintenance", LocalDate.of(2024, 5, 1));
        when(assetRepository.findById(1L)).thenReturn(Optional.of(laptop));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asset result = assetService.updateAsset(1L, changes);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Dell XPS 15 (2024)");
        assertThat(result.getSerialNumber()).isEqualTo("SN-33211-B");
        assertThat(result.getAssignedTo()).isEqualTo("Mohammed");
        assertThat(result.getStatus()).isEqualTo("Under Maintenance");
        assertThat(result.getPurchaseDate()).isEqualTo(LocalDate.of(2024, 5, 1));
        verify(assetRepository).save(laptop);
    }

    @Test
    @DisplayName("updateAsset throws and never saves when the id does not exist")
    void updateAsset_notFound() {
        when(assetRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.updateAsset(42L, laptop))
                .isInstanceOf(AssetNotFoundException.class);
        verify(assetRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteAsset deletes when the id exists")
    void deleteAsset_found() {
        when(assetRepository.existsById(1L)).thenReturn(true);

        assetService.deleteAsset(1L);

        verify(assetRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteAsset throws and never deletes when the id does not exist")
    void deleteAsset_notFound() {
        when(assetRepository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> assetService.deleteAsset(42L))
                .isInstanceOf(AssetNotFoundException.class);
        verify(assetRepository, never()).deleteById(anyLong());
    }
}
