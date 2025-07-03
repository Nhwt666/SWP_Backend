package com.group2.ADN;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.ADN.entity.Voucher;
import com.group2.ADN.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class VoucherControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private VoucherRepository voucherRepository;

    @BeforeEach
    void setup() {
        voucherRepository.deleteAll();
    }

    @Test
    void testCreateVoucher() throws Exception {
        Voucher voucher = new Voucher();
        voucher.setName("TEST10");
        voucher.setType("percent");
        voucher.setValue(new BigDecimal("10"));
        voucher.setStart(LocalDateTime.now().minusDays(1));
        voucher.setEndDate(LocalDateTime.now().plusDays(5));
        voucher.setStatus("active");

        mockMvc.perform(post("/api/vouchers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voucher)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TEST10"))
                .andExpect(jsonPath("$.type").value("percent"));
    }

    @Test
    void testGetAllVouchers() throws Exception {
        Voucher voucher = new Voucher();
        voucher.setName("TEST20");
        voucher.setType("amount");
        voucher.setValue(new BigDecimal("20000"));
        voucher.setStart(LocalDateTime.now().minusDays(1));
        voucher.setEndDate(LocalDateTime.now().plusDays(5));
        voucher.setStatus("active");
        voucherRepository.save(voucher);

        mockMvc.perform(get("/api/vouchers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("TEST20"));
    }

    @Test
    void testGetActiveVouchers() throws Exception {
        Voucher voucher = new Voucher();
        voucher.setName("ACTIVE1");
        voucher.setType("percent");
        voucher.setValue(new BigDecimal("5"));
        voucher.setStart(LocalDateTime.now().minusDays(1));
        voucher.setEndDate(LocalDateTime.now().plusDays(1));
        voucher.setStatus("active");
        voucherRepository.save(voucher);

        String now = LocalDateTime.now().toString();
        mockMvc.perform(get("/api/vouchers/active?now=" + now))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ACTIVE1"));
    }
} 