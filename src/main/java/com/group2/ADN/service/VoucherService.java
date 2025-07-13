package com.group2.ADN.service;

import com.group2.ADN.entity.Voucher;
import com.group2.ADN.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherService {
    @Autowired
    private VoucherRepository voucherRepository;

    public Voucher createVoucher(Voucher voucher) {
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setUpdatedAt(LocalDateTime.now());
        voucher.setUsedCount(0);
        voucher.setStatus("active");
        return voucherRepository.save(voucher);
    }

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Optional<Voucher> getVoucherById(Integer id) {
        return voucherRepository.findById(id);
    }

    public Voucher updateVoucher(Integer id, Voucher update) {
        return voucherRepository.findById(id).map(voucher -> {
            if (update.getName() != null) voucher.setName(update.getName());
            if (update.getType() != null) voucher.setType(update.getType());
            if (update.getValue() != null) voucher.setValue(update.getValue());
            if (update.getStart() != null) voucher.setStart(update.getStart());
            if (update.getEndDate() != null) voucher.setEndDate(update.getEndDate());
            if (update.getMaxUsage() != null) voucher.setMaxUsage(update.getMaxUsage());
            voucher.setUpdatedAt(LocalDateTime.now());
            return voucherRepository.save(voucher);
        }).orElse(null);
    }

    public boolean deleteVoucher(Integer id) {
        if (voucherRepository.existsById(id)) {
            voucherRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Voucher extendVoucher(Integer id, LocalDateTime newEndDate) {
        return voucherRepository.findById(id).map(voucher -> {
            voucher.setEndDate(newEndDate);
            voucher.setUpdatedAt(LocalDateTime.now());
            return voucherRepository.save(voucher);
        }).orElse(null);
    }

    public List<Voucher> getActiveVouchers(LocalDateTime now) {
        return voucherRepository.findByStatusAndStartLessThanEqualAndEndDateGreaterThanEqual("active", now);
    }

    public boolean isVoucherValid(Voucher voucher, LocalDateTime now) {
        if (voucher == null) return false;
        if (!"active".equals(voucher.getStatus())) return false;
        if (voucher.getStart().isAfter(now) || voucher.getEndDate().isBefore(now)) return false;
        if (voucher.getMaxUsage() != null && voucher.getUsedCount() != null && voucher.getUsedCount() >= voucher.getMaxUsage()) return false;
        return true;
    }

    public void incrementUsedCount(Voucher voucher) {
        if (voucher.getUsedCount() == null) voucher.setUsedCount(1);
        else voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucher.setUpdatedAt(LocalDateTime.now());
        voucherRepository.save(voucher);
    }

    public Voucher findByCode(String code) {
        return voucherRepository.findFirstByNameIgnoreCase(code);
    }

    public boolean isVoucherActive(Voucher voucher, LocalDateTime now) {
        if (voucher == null) return false;
        if (!"active".equalsIgnoreCase(voucher.getStatus())) return false;
        if (voucher.getStart().isAfter(now) || voucher.getEndDate().isBefore(now)) return false;
        if (voucher.getMaxUsage() != null && voucher.getUsedCount() != null && voucher.getUsedCount() >= voucher.getMaxUsage()) return false;
        return true;
    }

    public String getVoucherInvalidReason(Voucher voucher, LocalDateTime now) {
        if (voucher == null) return "Voucher không tồn tại";
        if (!"active".equalsIgnoreCase(voucher.getStatus())) return "Voucher không hoạt động";
        if (voucher.getStart().isAfter(now)) return "Voucher chưa bắt đầu";
        if (voucher.getEndDate().isBefore(now)) return "Voucher đã hết hạn";
        if (voucher.getMaxUsage() != null && voucher.getUsedCount() != null && voucher.getUsedCount() >= voucher.getMaxUsage()) return "Voucher đã hết lượt sử dụng";
        return null;
    }
} 