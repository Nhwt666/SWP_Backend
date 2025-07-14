package com.group2.ADN.service;

import com.group2.ADN.dto.TicketRequest;
import com.group2.ADN.dto.FeedbackRequest;
import com.group2.ADN.entity.*;
import com.group2.ADN.repository.TicketRepository;
import com.group2.ADN.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import com.group2.ADN.dto.AdminUpdateTicketRequest;
import com.group2.ADN.dto.AdminCreateTicketRequest;
import com.group2.ADN.repository.TicketFeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group2.ADN.dto.TicketFeedbackRequest;

@Service
@Transactional
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketFeedbackRepository ticketFeedbackRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MailService mailService;

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Lấy ticket theo id
     */
    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    /**
     * Lấy danh sách ticket của customer
     */
    public List<Ticket> getTicketsByCustomer(User customer) {
        return ticketRepository.findByCustomer(customer);
    }

    /**
     * Lấy danh sách ticket theo trạng thái
     */
    public List<Ticket> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    /**
     * Tự động gán staff cho ticket (nếu staff chưa quá tải)
     */
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
                return ticketRepository.save(ticket);
            }
        }

        // All staff are at full capacity
        return ticket; // Optionally return unassigned or throw exception
    }

    /**
     * Đổi trạng thái ticket
     */
    public Ticket updateStatus(Long ticketId, TicketStatus newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketStatus oldStatus = ticket.getStatus();
        // Nếu chuyển sang COMPLETED thì set completedAt
        if (newStatus == TicketStatus.COMPLETED && ticket.getCompletedAt() == null) {
            ticket.setCompletedAt(java.time.LocalDateTime.now());
        }
        ticket.setStatus(newStatus);
        Ticket savedTicket = ticketRepository.save(ticket);

        // Tạo notification cho customer khi chuyển trạng thái quan trọng
        if (oldStatus != newStatus) {
            notificationService.createStatusChangeNotification(
                ticket.getCustomer(), 
                ticket.getId(), 
                oldStatus.name(), 
                newStatus.name()
            );
        }
        return savedTicket;
    }

    /**
     * Đánh dấu ticket đã hoàn thành và gửi thông báo cho customer
     */
    public Ticket completeTicket(Long ticketId, String result) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(TicketStatus.COMPLETED);
        ticket.setResultString(result);
        if (ticket.getCompletedAt() == null) {
            ticket.setCompletedAt(java.time.LocalDateTime.now());
        }
        Ticket savedTicket = ticketRepository.save(ticket);

        // Create notification for customer when test is completed
        notificationService.createNotification(
            ticket.getCustomer(),
            "Kết quả xét nghiệm cho ticket #" + ticketId + " đã hoàn thành.",
            ticketId,
            "INFO"
        );

        // Send email to customer to notify result is ready
        String customerEmail = ticket.getCustomer().getEmail();
        String subject = "Kết quả xét nghiệm ADN đã sẵn sàng";
        String text = "Kính gửi Quý khách,\n\nKết quả xét nghiệm cho ticket #" + ticket.getId() +
                      " đã hoàn thành. Vui lòng đăng nhập vào website để tải file PDF kết quả.\n\nTrân trọng,\nTrung tâm xét nghiệm ADN";
        mailService.sendResultNotification(customerEmail, subject, text);

        return savedTicket;
    }

    /**
     * Tìm user theo id
     */
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Tìm user theo email
     */
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Lưu ticket
     */
    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    /**
     * Lưu user
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Lấy danh sách ticket của staff
     */
    public List<Ticket> getTicketsByStaff(User staff) {
        return ticketRepository.findByStaff(staff);
    }

    /**
     * Staff nhập kết quả xét nghiệm cho ticket
     */

    /**
     * Staff hủy kết quả xét nghiệm của ticket
     */

    public List<Ticket> getUnassignedPendingTickets() {
        return ticketRepository.findByStatusAndStaffIsNull(TicketStatus.PENDING);
    }

    public List<Ticket> getPendingTicketsForStaff() {
        return ticketRepository.findByStatusAndStaffIsNull(TicketStatus.PENDING);
    }

    @Transactional
    public Ticket adminUpdateTicket(Long ticketId, AdminUpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (request.getStatus() != null) {
            if (request.getStatus() == TicketStatus.COMPLETED && ticket.getCompletedAt() == null) {
                ticket.setCompletedAt(java.time.LocalDateTime.now());
            }
            ticket.setStatus(request.getStatus());
        }

        if (request.getStaffId() != null) {
            User staff = userRepository.findById(request.getStaffId())
                    .orElseThrow(() -> new RuntimeException("Staff not found with id: " + request.getStaffId()));
            ticket.setStaff(staff);
        }

        if (request.getResultString() != null) {
            ticket.setResultString(request.getResultString());
        }

        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket createTicketByAdmin(AdminCreateTicketRequest request) {
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + request.getCustomerId()));

        Ticket ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setType(request.getType());
        ticket.setMethod(request.getMethod());
        ticket.setStatus(request.getStatus());
        ticket.setAmount(request.getAmount());
        ticket.setEmail(request.getEmail());
        ticket.setPhone(request.getPhone());
        ticket.setAddress(request.getAddress());
        ticket.setSample1Name(request.getSample1Name());
        ticket.setSample2Name(request.getSample2Name());
        ticket.setReason(request.getReason());
        
        // createdAt and updatedAt are handled by @PrePersist and @PreUpdate

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket assignTicketToStaff(Long ticketId, Long staffId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + staffId));

        if (staff.getRole() != UserRole.STAFF) {
            throw new IllegalArgumentException("User is not a staff member.");
        }

        ticket.setStaff(staff);
        // Optionally change status to IN_PROGRESS when assigning
        if(ticket.getStatus() == TicketStatus.PENDING) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket unassignTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setStaff(null);
        // Optionally change status back to PENDING when un-assigning
        if(ticket.getStatus() == TicketStatus.IN_PROGRESS) {
            ticket.setStatus(TicketStatus.PENDING);
        }
        return ticketRepository.save(ticket);
    }

    public TicketFeedback submitFeedback(Ticket ticket, User user, TicketFeedbackRequest request) {
        if (ticketFeedbackRepository.findByTicketAndUser(ticket, user).isPresent()) {
            throw new RuntimeException("Feedback already submitted for this ticket");
        }
        TicketFeedback feedback = new TicketFeedback();
        feedback.setTicket(ticket);
        feedback.setUser(user);
        feedback.setFeedback(request.getFeedback());
        feedback.setRating(request.getRating());
        return ticketFeedbackRepository.save(feedback);
    }

    public TicketFeedback updateFeedback(Ticket ticket, User user, TicketFeedbackRequest request) {
        TicketFeedback feedback = ticketFeedbackRepository.findByTicketAndUser(ticket, user)
            .orElseThrow(() -> new RuntimeException("No feedback to update. Please submit feedback first."));
        feedback.setFeedback(request.getFeedback());
        feedback.setRating(request.getRating());
        return ticketFeedbackRepository.save(feedback);
    }

    public TicketFeedback getFeedback(Ticket ticket, User user) {
        return ticketFeedbackRepository.findByTicketAndUser(ticket, user).orElse(null);
    }

    /**
     * Submit feedback directly to a ticket
     */
    @Transactional
    public Ticket submitTicketFeedback(Long ticketId, FeedbackRequest request, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Check if user is the ticket owner
        if (!ticket.getCustomer().getId().equals(userId)) {
            throw new RuntimeException("Not the ticket owner");
        }

        // Check if ticket is completed
        if (ticket.getStatus() != TicketStatus.COMPLETED) {
            throw new RuntimeException("Ticket not completed");
        }

        // Check if feedback already exists
        if (ticket.getFeedback() != null) {
            throw new RuntimeException("Feedback already exists");
        }

        // Validate rating
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1-5");
        }

        // Set feedback
        ticket.setRating(request.getRating());
        ticket.setFeedback(request.getFeedback());
        ticket.setFeedbackDate(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    /**
     * Customer confirms they have received the DNA testing kit
     */
    @Transactional
    public Ticket confirmKitReceived(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Check if user is the ticket owner
        if (!ticket.getCustomer().getId().equals(userId)) {
            throw new RuntimeException("Chỉ chủ ticket mới có thể thực hiện");
        }

        // Check if this is a CIVIL SELF_TEST ticket
        if (ticket.getType() != TicketType.CIVIL || ticket.getMethod() != TestMethod.SELF_TEST) {
            throw new RuntimeException("Chỉ áp dụng cho ticket Dân sự + Tự gửi mẫu");
        }

        // Check if ticket is in CONFIRMED status
        if (ticket.getStatus() != TicketStatus.CONFIRMED) {
            throw new RuntimeException("Ticket phải ở trạng thái CONFIRMED");
        }

        // Update status to RECEIVED
        ticket.setStatus(TicketStatus.RECEIVED);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        // Create notification for staff
        if (ticket.getStaff() != null) {
            notificationService.createNotification(
                ticket.getStaff(),
                "Khách hàng đã xác nhận nhận kit cho ticket #" + ticketId
            );
        }

        return savedTicket;
    }

    /**
     * Customer confirms they have sent the DNA testing kit back
     */
    @Transactional
    public Ticket confirmKitSent(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Check if user is the ticket owner
        if (!ticket.getCustomer().getId().equals(userId)) {
            throw new RuntimeException("Chỉ chủ ticket mới có thể thực hiện");
        }

        // Check if this is a CIVIL SELF_TEST ticket
        if (ticket.getType() != TicketType.CIVIL || ticket.getMethod() != TestMethod.SELF_TEST) {
            throw new RuntimeException("Chỉ áp dụng cho ticket Dân sự + Tự gửi mẫu");
        }

        // Check if ticket is in RECEIVED status
        if (ticket.getStatus() != TicketStatus.RECEIVED) {
            throw new RuntimeException("Ticket phải ở trạng thái RECEIVED");
        }

        // Update status to PENDING (kit sent back, waiting for staff to process)
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        // Create notification for staff
        if (ticket.getStaff() != null) {
            notificationService.createNotification(
                ticket.getStaff(),
                "Khách hàng đã gửi kit về cho ticket #" + ticketId
            );
        }

        return savedTicket;
    }

    /**
     * Tạo ticket sau khi thanh toán thành công (logic chuyển từ controller)
     */
    public Ticket createTicketAfterPayment(TicketRequest request, User user) {
        log.info("[TICKET] Creating ticket after payment for user: {}, amount: {}", user.getEmail(), request.getAmount());
        // Validate amount
        java.math.BigDecimal amount = request.getAmount();
        if (amount == null) {
            throw new RuntimeException("Amount is required");
        }
        java.math.BigDecimal min = new java.math.BigDecimal("100000");
        java.math.BigDecimal max = new java.math.BigDecimal("10000000000");
        if (amount.compareTo(min) < 0 || amount.compareTo(max) > 0) {
            throw new RuntimeException("[TICKET] Amount must be between 100,000 and 10,000,000,000");
        }
        java.math.BigDecimal currentBalance = user.getWalletBalance();
        if (currentBalance == null) currentBalance = java.math.BigDecimal.ZERO;
        if (currentBalance.compareTo(amount) < 0) {
            throw new RuntimeException("[TICKET] Wallet balance is not sufficient for this transaction");
        }
        // Trừ tiền
        user.setWalletBalance(currentBalance.subtract(amount));
        saveUser(user);
        log.info("[TICKET] Deducted {} from user wallet. New balance: {}", amount, user.getWalletBalance());

        Ticket ticket = new Ticket();
        try {
            ticket.setType(TicketType.valueOf(request.getType()));
            ticket.setMethod(TestMethod.valueOf(request.getMethod()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("[TICKET] Invalid ticket type or method");
        }
        ticket.setReason(request.getReason());
        // Logic mới: CIVIL SELF_TEST → CONFIRMED, các loại khác → PENDING
        if (request.getType().equals("CIVIL") && request.getMethod().equals("SELF_TEST")) {
            ticket.setStatus(TicketStatus.CONFIRMED);
        } else {
            ticket.setStatus(TicketStatus.PENDING);
        }
        ticket.setCustomer(user);
        ticket.setAddress(request.getAddress());
        ticket.setPhone(request.getPhone());
        ticket.setEmail(request.getEmail());
        ticket.setSample1Name(request.getSample1Name());
        ticket.setSample2Name(request.getSample2Name());
        ticket.setAmount(amount);
        if (ticket.getMethod() == TestMethod.AT_FACILITY) {
            ticket.setAppointmentDate(request.getAppointmentDate());
        } else {
            ticket.setAppointmentDate(null);
        }
        Ticket savedTicket = saveTicket(ticket);
        log.info("[TICKET] Ticket created successfully. Ticket ID: {}, Status: {}", savedTicket.getId(), savedTicket.getStatus());
        return savedTicket;
    }
}

