package com.se411.assetmanagement.config;

import com.se411.assetmanagement.model.Asset;
import com.se411.assetmanagement.repository.AssetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Fills the empty in-memory database with sample assets at startup,
 * so the demo has enough rows to show more than one page.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final AssetRepository assetRepository;

    public DataSeeder(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public void run(String... args) {
        if (assetRepository.count() > 0) {
            return;
        }
        assetRepository.saveAll(List.of(
                new Asset("MacBook Pro 16\"", "Laptop", "SN-90812", "Mohammed Al-Jarbou", "In Use", LocalDate.of(2024, 1, 15)),
                new Asset("Dell XPS 15", "Laptop", "SN-33211", "Eyad Al-Shuhail", "In Use", LocalDate.of(2024, 3, 10)),
                new Asset("ThinkPad T14", "Laptop", "SN-44512", "Hamad Al-Waheebi", "In Use", LocalDate.of(2024, 2, 20)),
                new Asset("HP ZBook Studio", "Laptop", "SN-88219", "Ahmad Al-Talhi", "Available", LocalDate.of(2024, 4, 5)),
                new Asset("Dell OptiPlex 7010", "Desktop", "SN-51023", "Reception", "In Use", LocalDate.of(2023, 11, 2)),
                new Asset("HP EliteDesk 800", "Desktop", "SN-51877", "Finance Dept", "In Use", LocalDate.of(2023, 9, 18)),
                new Asset("iMac 24\"", "Desktop", "SN-62240", "Design Team", "Available", LocalDate.of(2024, 6, 1)),
                new Asset("Dell PowerEdge R750", "Server", "SN-70031", "Data Center", "In Use", LocalDate.of(2022, 5, 30)),
                new Asset("HPE ProLiant DL380", "Server", "SN-70458", "Data Center", "Under Maintenance", LocalDate.of(2021, 8, 12)),
                new Asset("iPhone 15 Pro", "Mobile", "SN-80115", "Sales Team", "In Use", LocalDate.of(2024, 9, 22)),
                new Asset("Samsung Galaxy S24", "Mobile", "SN-80342", "Support Team", "Available", LocalDate.of(2024, 2, 14)),
                new Asset("iPad Air", "Mobile", "SN-80977", "Warehouse", "Under Maintenance", LocalDate.of(2023, 12, 5))
        ));
    }
}
