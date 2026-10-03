package com.moh.vaxtrack.service;

import com.moh.vaxtrack.entity.*;
import com.moh.vaxtrack.repository.HospitalStockRepository;
import com.moh.vaxtrack.repository.NationalStockRepository;
import com.moh.vaxtrack.repository.StockRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class DispatchService {

    private final NationalStockRepository nationalStockRepository;
    private final HospitalStockRepository hospitalStockRepository;
    private final StockRequestRepository stockRequestRepository;
    private final StockAllocationStrategy stockAllocationStrategy;

    public DispatchService(NationalStockRepository nationalStockRepository,
                            HospitalStockRepository hospitalStockRepository,
                            StockRequestRepository stockRequestRepository,
                            StockAllocationStrategy stockAllocationStrategy) {
        this.nationalStockRepository = nationalStockRepository;
        this.hospitalStockRepository = hospitalStockRepository;
        this.stockRequestRepository = stockRequestRepository;
        this.stockAllocationStrategy = stockAllocationStrategy;
    }

    @Transactional
    public void dispatch(StockRequest request, int dispatchQuantity) {

        Long vaccineId = request.getVaccine().getVaccineId();

        // Check the total available stock before starting the dispatch.
        // If there is not enough stock, the request is rejected.
        long available = nationalStockRepository.sumQuantityByVaccine(vaccineId);
        if (available < dispatchQuantity) {
            throw new InsufficientStockException(available);
        }

        // Get the stock batches using the selected allocation strategy.
        // Currently, FEFO is used, so the batch with the earliest expiry is used first.
        int remaining = dispatchQuantity;
        List<NationalStock> batches = stockAllocationStrategy.selectBatches(vaccineId);

        for (NationalStock batch : batches) {
            if (remaining <= 0) {
                break;
            }
            int deductFromThisBatch = Math.min(batch.getQuantity(), remaining);
            batch.setQuantity(batch.getQuantity() - deductFromThisBatch);
            nationalStockRepository.save(batch);
            remaining -= deductFromThisBatch;
        }

        // Add the dispatched quantity to the hospital stock.
        // If the hospital does not have this vaccine yet, create a new stock record.
        HospitalStock hospitalStock = hospitalStockRepository
                .findByHospitalAndVaccine(request.getHospital(), request.getVaccine())
                .orElseGet(() -> {
                    HospitalStock newBalance = new HospitalStock();
                    newBalance.setHospital(request.getHospital());
                    newBalance.setVaccine(request.getVaccine());
                    newBalance.setQuantity(0);
                    return newBalance;
                });
        hospitalStock.setQuantity(hospitalStock.getQuantity() + dispatchQuantity);
        hospitalStockRepository.save(hospitalStock);

        // Mark the stock request as dispatched and save the dispatch details.

        request.setStatus(StockRequestStatus.DISPATCHED);
        request.setDispatchedQuantity(dispatchQuantity);
        request.setResolvedAt(LocalDateTime.now());
        stockRequestRepository.save(request);
    }
}
