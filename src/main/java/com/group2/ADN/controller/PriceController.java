package com.group2.ADN.controller;

import com.group2.ADN.entity.Price;
import com.group2.ADN.entity.PriceType;
import com.group2.ADN.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class PriceController {
    @Autowired
    private PriceService priceService;

    @GetMapping
    public List<Price> getAllPrices() {
        return priceService.getAllPrices();
    }

    @GetMapping("/type/{type}")
    public List<Price> getPricesByType(@PathVariable String type) {
        try {
            PriceType priceType = PriceType.valueOf(type.toUpperCase());
            return priceService.getPricesByType(priceType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid price type. Must be 'civil', 'administrative', or 'other'");
        }
    }
} 