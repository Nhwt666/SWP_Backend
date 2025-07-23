package com.group2.ADN.service;

import com.group2.ADN.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceService {
    @Autowired
    private ServiceRepository serviceRepository;

    public List<com.group2.ADN.entity.Service> getAllServices() {
        return serviceRepository.findAll();
    }
} 