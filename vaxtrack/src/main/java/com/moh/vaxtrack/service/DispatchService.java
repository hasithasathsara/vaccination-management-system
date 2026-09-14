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

    public DispatchService(NationalStockRepository nationalStockRepository,
                            HospitalStockRepository hospitalStockRepository,
                            StockRequestRepository stockRequestRepository) {
        this.nationalStockRepository = nationalStockRepository;
        this.hospitalStockRepository = hospitalStockRepository;
        this.stockRequestRepository = stockRequestRepository;
    }

    @Transactional
    public void dispatch(StockRequest request, int dispatchQuantity) {

        Long vaccineId = request.getVaccine().getVaccineId();

        // Automatic Rejection Rule: block the whole thing if there isn't enough stock
        long available = nationalStockRepository.sumQuantityByVaccine(vaccineId);
        if (available < dispatchQuantity) {
            throw new InsufficientStockException(available);
        }

        // Deduct across batches, soonest-expiring first (FEFO) — a batch closer
        // to expiry gets used up before a fresher one.
        int remaining = dispatchQuantity;
        List<NationalStock> batches = nationalStockRepository
                .findByVaccine_VaccineIdAndQuantityGreaterThanOrderByExpiryDateAsc(vaccineId, 0);

        for (NationalStock batch : batches) {
            if (remaining <= 0) {
                break;
            }
            int deductFromThisBatch = Math.min(batch.getQuantity(), remaining);
            batch.setQuantity(batch.getQuantity() - deductFromThisBatch);
            nationalStockRepository.save(batch);
            remaining -= deductFromThisBatch;
        }

        // Credit the hospital's own stock — increment if they already have some
        // of this vaccine, otherwise start a new running balance at this amount.
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

        // Mark the request resolved
        request.setStatus(StockRequestStatus.DISPATCHED);
        request.setDispatchedQuantity(dispatchQuantity);
        request.setResolvedAt(LocalDateTime.now());
        stockRequestRepository.save(request);
    }
}
