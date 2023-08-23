package com.mcsv.inventory.repository;

import com.mcsv.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByCodeSkuIn(List<String> codeSku);

}
