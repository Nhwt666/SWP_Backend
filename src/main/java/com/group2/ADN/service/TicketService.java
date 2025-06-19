package com.group2.ADN.service;

import com.group2.ADN.dto.TicketRequest;
import com.group2.ADN.entity.*;
import com.group2.ADN.repository.TicketRepository;
import com.group2.ADN.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

        public Ticket createTicketFromRequest(TicketRequest request) {
            Ticket ticket = new Ticket();

            try {
                ticket.setType(TicketType.valueOf(request.getType()));
                ticket.setMethod(TestMethod.valueOf(request.getMethod()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid ticket type or method");
            }

            ticket.setReason(request.getReason());
            ticket.setStatus(TicketStatus.PENDING);

            // Fetch customer
            User customer = userRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            ticket.setCustomer(customer);

            // Xử lý 3 trường address/phone/email (convert "" về null nếu cần)
            ticket.setAddress(isEmpty(request.getAddress()) ? null : request.getAddress());
            ticket.setPhone(isEmpty(request.getPhone()) ? null : request.getPhone());
            ticket.setEmail(isEmpty(request.getEmail()) ? null : request.getEmail());

            Ticket savedTicket = ticketRepository.save(ticket);
            return assignStaffAutomatically(savedTicket.getId());

        }
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> getTicketsByCustomer(User customer) {
        return ticketRepository.findByCustomer(customer);
    }

    public List<Ticket> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    public Ticket assignStaffAutomatically(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        List<User> staffList = userRepository.findByRole(UserRole.STAFF);

        if (staffList.isEmpty()) {
            throw new RuntimeException("No staff available");
        }

        // Define the statuses considered "active"
        List<TicketStatus> activeStatuses = List.of(
                TicketStatus.PENDING,
                TicketStatus.IN_PROGRESS
        );

        // Find a staff member with < 5 active tickets
        for (User staff : staffList) {
            int activeCount = ticketRepository.countByStaffAndStatusIn(staff, activeStatuses);
            if (activeCount < 5) {
                ticket.setStaff(staff);
                ticket.setStatus(TicketStatus.IN_PROGRESS);
                System.out.println("✅ Assigned staff: " + staff.getFullName() + " to ticket ID: " + ticketId);
                return ticketRepository.save(ticket);
            }
        }

        // All staff are at full capacity
        System.out.println("❌ All staff are currently handling 5 or more tickets");
        return ticket; // Optionally return unassigned or throw exception
    }


    public Ticket updateStatus(Long ticketId, TicketStatus newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(newStatus);
        return ticketRepository.save(ticket);

    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<Ticket> getTicketsByStaff(User staff) {
        return ticketRepository.findByStaff(staff);
    }

//    public org.springframework.http.ResponseEntity<?> uploadResult(Long id, org.springframework.web.multipart.MultipartFile file) {
//        Ticket ticket = ticketRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Ticket not found"));
//        try {
//            // Lưu file vào thư mục uploads/results (tạo nếu chưa có)
//            java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads/results");
//            if (!java.nio.file.Files.exists(uploadDir)) {
//                java.nio.file.Files.createDirectories(uploadDir);
//            }
//            String fileName = "ticket_" + id + "_" + System.currentTimeMillis() + ".pdf";
//            java.nio.file.Path filePath = uploadDir.resolve(fileName);
//            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
//            ticket.setResult(fileName);
//            ticket.setStatus(TicketStatus.COMPLETED);
//            ticketRepository.save(ticket);
//            return org.springframework.http.ResponseEntity.ok("Upload thành công");
//        } catch (Exception e) {
//            return org.springframework.http.ResponseEntity.status(500).body("Lỗi upload file: " + e.getMessage());
//        }
    }

