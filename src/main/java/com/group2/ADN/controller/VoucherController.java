package com.group2.ADN.controller;

import com.group2.ADN.entity.Voucher;
import com.group2.ADN.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {
    @Autowired
    private VoucherService voucherService;

    // Admin: Tạo mới voucher
    @PostMapping
    public ResponseEntity<Voucher> createVoucher(@RequestBody Voucher voucher) {
        return ResponseEntity.ok(voucherService.createVoucher(voucher));
    }

    // Admin: Lấy danh sách voucher
    @GetMapping
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    // Admin: Cập nhật voucher
    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(@PathVariable Integer id, @RequestBody Voucher voucher) {
        Voucher updated = voucherService.updateVoucher(id, voucher);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    // Admin: Xóa voucher
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Integer id) {
        if (voucherService.deleteVoucher(id)) return ResponseEntity.ok().build();
        return ResponseEntity.notFound().build();
    }

    // Admin: Gia hạn voucher
    @PatchMapping("/{id}/extend")
    public ResponseEntity<Voucher> extendVoucher(@PathVariable Integer id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Voucher updated = voucherService.extendVoucher(id, endDate);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    // Member: Lấy danh sách voucher còn hiệu lực
    @GetMapping("/active")
    public ResponseEntity<List<Voucher>> getActiveVouchers(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime now) {
        return ResponseEntity.ok(voucherService.getActiveVouchers(now));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkVoucher(@RequestParam String code) {
        Voucher voucher = voucherService.findByCode(code.trim());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        System.out.println("--- CHECK VOUCHER LOG ---");
        System.out.println("Voucher: " + (voucher != null ? voucher.getName() : "null"));
        System.out.println("Status: " + (voucher != null ? voucher.getStatus() : "null"));
        System.out.println("Start: " + (voucher != null ? voucher.getStart() : "null"));
        System.out.println("EndDate: " + (voucher != null ? voucher.getEndDate() : "null"));
        System.out.println("Now: " + now);
        System.out.println("isVoucherActive: " + voucherService.isVoucherActive(voucher, now));
        if (!voucherService.isVoucherActive(voucher, now)) {
            String reason = voucherService.getVoucherInvalidReason(voucher, now);
            System.out.println("Reason: " + reason);
            System.out.println("--- END CHECK VOUCHER LOG ---");
            return ResponseEntity.status(404).body(java.util.Map.of("message", reason != null ? reason : "Voucher không hợp lệ hoặc đã hết hạn"));
        }
        System.out.println("Voucher hợp lệ!");
        System.out.println("--- END CHECK VOUCHER LOG ---");
        return ResponseEntity.ok(voucher);
    }
} 