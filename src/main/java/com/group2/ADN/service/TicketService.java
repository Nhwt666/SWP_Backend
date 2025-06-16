package com.group2.ADN.service;

import com.group2.ADN.dto.TicketRequest;
import com.group2.ADN.entity.*;
import com.group2.ADN.repository.TicketRepository;
import com.group2.ADN.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        return ticketRepository.save(ticket);
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

        User selectedStaff = staffList.get(0); // Basic selection
        ticket.setStaff(selectedStaff);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        return ticketRepository.save(ticket);
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
}
