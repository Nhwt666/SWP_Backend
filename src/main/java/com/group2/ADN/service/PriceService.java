package com.group2.ADN.service;

import com.group2.ADN.dto.AdminCreatePriceRequest;
import com.group2.ADN.dto.AdminUpdatePriceRequest;
import com.group2.ADN.entity.Price;
import com.group2.ADN.entity.PriceType;
import com.group2.ADN.repository.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceService {
    @Autowired
    private PriceRepository priceRepository;

    public List<Price> getAllPrices() {
        return priceRepository.findAll();
    }

    public List<Price> getPricesByType(PriceType type) {
        return priceRepository.findByType(type);
    }

    public Price createPrice(AdminCreatePriceRequest request) {
        Price price = new Price(
            request.getValue(),
            request.getCurrency(),
            request.getName(),
            request.getType()
        );
        return priceRepository.save(price);
    }

    public Price updatePrice(Long id, AdminUpdatePriceRequest request) {
        Price price = priceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Price not found with id: " + id));
        
        price.setValue(request.getValue());
        price.setCurrency(request.getCurrency());
        price.setName(request.getName());
        price.setType(request.getType());
        
        return priceRepository.save(price);
    }

    public void deletePrice(Long id) {
        if (!priceRepository.existsById(id)) {
            throw new RuntimeException("Price not found with id: " + id);
        }
        priceRepository.deleteById(id);
    }

    public Price getPriceById(Long id) {
        return priceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Price not found with id: " + id));
    }
} 