package com.se411.assetmanagement.repository;

import com.se411.assetmanagement.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access layer. JpaRepository already gives us save, findById, findAll(Pageable),
 * existsById, deleteById and count, so no SQL needs to be written by hand.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
}
