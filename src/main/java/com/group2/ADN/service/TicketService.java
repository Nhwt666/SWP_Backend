package com.group2.ADN.service;

import com.group2.ADN.entity.Ticket;
import com.group2.ADN.entity.TicketStatus;
import com.group2.ADN.entity.User;
import com.group2.ADN.entity.UserRole;
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

    public Ticket createTicket(Ticket ticket) {
        ticket.setStatus(TicketStatus.PENDING);
        return ticketRepository.save(ticket);
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

        User selectedStaff = staffList.get(0); // Basic: pick first staff
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
}