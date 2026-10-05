package com.moh.vaxtrack.service;

import com.moh.vaxtrack.entity.NationalStock;
import com.moh.vaxtrack.repository.NationalStockRepository;
import org.springframework.stereotype.Component;

import java.util.List;


// Strategy pattern: this class implements the FEFO rule for selecting vaccine stock.
// Batches with the earliest expiry date are selected first to help use stock before it expire
@Component
public class FefoAllocationStrategy implements StockAllocationStrategy {

    private final NationalStockRepository nationalStockRepository;

    public FefoAllocationStrategy(NationalStockRepository nationalStockRepository) {
        this.nationalStockRepository = nationalStockRepository;
    }

    @Override

    // Gets the available stock batches for the vaccine and orders them by expiry date.

    public List<NationalStock> selectBatches(Long vaccineId) {
        return nationalStockRepository
                .findByVaccine_VaccineIdAndQuantityGreaterThanOrderByExpiryDateAsc(vaccineId, 0);
    }
}
