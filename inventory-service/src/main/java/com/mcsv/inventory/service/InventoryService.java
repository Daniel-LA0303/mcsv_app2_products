package com.mcsv.inventory.service;

import com.mcsv.inventory.dto.InventoryResponse;
import com.mcsv.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    //this method receive a list of codeSku and return a list of InventoryResponse
    @Transactional(readOnly = true)
    public List<InventoryResponse> inInStock(List<String> codeSku) {
        return inventoryRepository.findByCodeSkuIn(codeSku).stream()
                .map(inventory -> InventoryResponse.builder()
                        .codeSku(inventory.getCodeSku())
                        .inStock(inventory.getQuantity() > 0)
                        .build()
                ).collect(Collectors.toList());
    }
}
