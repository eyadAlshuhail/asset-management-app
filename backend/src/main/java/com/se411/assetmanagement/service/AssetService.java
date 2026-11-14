package com.se411.assetmanagement.service;

import com.se411.assetmanagement.exception.AssetNotFoundException;
import com.se411.assetmanagement.model.Asset;
import com.se411.assetmanagement.repository.AssetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Business logic layer. The controller calls this class; this class calls the repository.
 * The repository is injected through the constructor, which is what lets the unit tests
 * pass in a Mockito mock instead of a real database.
 */
@Service
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    /** Add a new asset. Any id sent by the client is ignored so the database generates it. */
    public Asset createAsset(Asset asset) {
        asset.setId(null);
        return assetRepository.save(asset);
    }

    /** Find one asset by id, or throw 404. */
    public Asset getAssetById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(id));
    }

    /** Return one page of assets (page number, size and sort come from the Pageable). */
    public Page<Asset> getAllAssets(Pageable pageable) {
        return assetRepository.findAll(pageable);
    }

    /** Update every editable field of an existing asset. */
    public Asset updateAsset(Long id, Asset updated) {
        Asset existing = getAssetById(id);
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setSerialNumber(updated.getSerialNumber());
        existing.setAssignedTo(updated.getAssignedTo());
        existing.setStatus(updated.getStatus());
        existing.setPurchaseDate(updated.getPurchaseDate());
        return assetRepository.save(existing);
    }

    /** Delete an asset, or throw 404 if it does not exist. */
    public void deleteAsset(Long id) {
        if (!assetRepository.existsById(id)) {
            throw new AssetNotFoundException(id);
        }
        assetRepository.deleteById(id);
    }
}
