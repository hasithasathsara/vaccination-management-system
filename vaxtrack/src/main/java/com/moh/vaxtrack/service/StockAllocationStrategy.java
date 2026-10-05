package com.moh.vaxtrack.service;

import com.moh.vaxtrack.entity.NationalStock;

import java.util.List;

// Strategy pattern: this interface defines how vaccine stock batches are selected
// when stock is dispatched to a hospital. Different strategies can implement
// this interface without changing the main dispatch service.

public interface StockAllocationStrategy {


    // Selects the available stock batches for the given vaccine.

    List<NationalStock> selectBatches(Long vaccineId);
}
