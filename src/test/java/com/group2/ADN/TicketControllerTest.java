package com.group2.ADN;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.ADN.entity.User;
import com.group2.ADN.entity.Voucher;
import com.group2.ADN.repository.UserRepository;
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
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TicketControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VoucherRepository voucherRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        voucherRepository.deleteAll();
        userRepository.deleteAll();
        testUser = new User();
        testUser.setFullName("Test User");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("123456");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCreateTicketWithPercentVoucher() throws Exception {
        Voucher voucher = new Voucher();
        voucher.setName("SALE10");
        voucher.setType("percent");
        voucher.setValue(new BigDecimal("10"));
        voucher.setStart(LocalDateTime.now().minusDays(1));
        voucher.setEndDate(LocalDateTime.now().plusDays(5));
        voucher.setStatus("active");
        voucherRepository.save(voucher);

        Map<String, Object> ticketRequest = Map.of(
                "type", "CIVIL",
                "method", "SELF_TEST",
                "reason", "Test reason",
                "customerId", testUser.getId(),
                "amount", new BigDecimal("100000"),
                "voucherCode", "SALE10"
        );

        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticketRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountAmount").value(10000))
                .andExpect(jsonPath("$.finalAmount").value(90000));
    }

    @Test
    void testCreateTicketWithAmountVoucher() throws Exception {
        Voucher voucher = new Voucher();
        voucher.setName("FIXED20K");
        voucher.setType("amount");
        voucher.setValue(new BigDecimal("20000"));
        voucher.setStart(LocalDateTime.now().minusDays(1));
        voucher.setEndDate(LocalDateTime.now().plusDays(5));
        voucher.setStatus("active");
        voucherRepository.save(voucher);

        Map<String, Object> ticketRequest = Map.of(
                "type", "CIVIL",
                "method", "SELF_TEST",
                "reason", "Test reason",
                "customerId", testUser.getId(),
                "amount", new BigDecimal("100000"),
                "voucherCode", "FIXED20K"
        );

        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticketRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountAmount").value(20000))
                .andExpect(jsonPath("$.finalAmount").value(80000));
    }
} 