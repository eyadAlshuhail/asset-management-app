package com.se411.assetmanagement.controller;

import com.se411.assetmanagement.dto.PageResponse;
import com.se411.assetmanagement.model.Asset;
import com.se411.assetmanagement.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for assets. All routes live under /api/assets.
 * The controller only handles HTTP (status codes, JSON); the logic is in AssetService.
 */
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    /** POST /api/assets  -> add an element (201 Created). */
    @PostMapping
    public ResponseEntity<Asset> createAsset(@Valid @RequestBody Asset asset) {
        Asset saved = assetService.createAsset(asset);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** GET /api/assets/{id}  -> search an element by id (200, or 404). */
    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getAssetById(id));
    }

    /** GET /api/assets?page=0&size=5&sort=id,asc  -> all elements, paginated. */
    @GetMapping
    public ResponseEntity<PageResponse<Asset>> getAllAssets(
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(assetService.getAllAssets(pageable)));
    }

    /** PUT /api/assets/{id}  -> update an element (200, or 404). */
    @PutMapping("/{id}")
    public ResponseEntity<Asset> updateAsset(@PathVariable Long id, @Valid @RequestBody Asset asset) {
        return ResponseEntity.ok(assetService.updateAsset(id, asset));
    }

    /** DELETE /api/assets/{id}  -> remove an element (204, or 404). Keeps the Part 1 delete button working. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }
}
