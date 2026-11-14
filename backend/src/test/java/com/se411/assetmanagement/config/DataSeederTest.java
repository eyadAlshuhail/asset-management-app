package com.se411.assetmanagement.config;

import com.se411.assetmanagement.model.Asset;
import com.se411.assetmanagement.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Test
    @SuppressWarnings("unchecked")
    void run_seedsSampleAssets_whenDatabaseIsEmpty() {
        when(assetRepository.count()).thenReturn(0L);

        dataSeeder.run();

        ArgumentCaptor<List<Asset>> captor = ArgumentCaptor.forClass(List.class);
        verify(assetRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(12);
    }

    @Test
    void run_doesNothing_whenDatabaseAlreadyHasData() {
        when(assetRepository.count()).thenReturn(3L);

        dataSeeder.run();

        verify(assetRepository, never()).saveAll(anyList());
    }
}
