package com.group2.ADN.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.group2.ADN.entity.Voucher;
import com.group2.ADN.repository.VoucherRepository;
import org.springframework.boot.CommandLineRunner;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner seedVoucher(VoucherRepository voucherRepository) {
        return args -> {
            if (voucherRepository.count() == 0) {
                Voucher voucher = new Voucher();
                voucher.setName("SALE10");
                voucher.setType("percent");
                voucher.setValue(new BigDecimal("10"));
                voucher.setStart(LocalDateTime.now().minusDays(1));
                voucher.setEndDate(LocalDateTime.now().plusDays(10));
                voucher.setStatus("active");
                voucher.setMaxUsage(100);
                voucher.setUsedCount(0);
                voucherRepository.save(voucher);
            }
        };
    }
}
