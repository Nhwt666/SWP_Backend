package com.group2.ADN.service;

import com.group2.ADN.dto.PendingRegisterRequest;
import com.group2.ADN.entity.PasswordResetRequest;
import com.group2.ADN.entity.PendingRegister;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import com.group2.ADN.entity.UserRole;
import com.group2.ADN.repository.PasswordResetRequestRepository;
import com.group2.ADN.repository.PendingRegisterRepository;
import com.group2.ADN.security.JwtTokenProvider;
import com.group2.ADN.dto.RegisterRequest;
import com.group2.ADN.entity.User;
import com.group2.ADN.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private boolean used;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final PendingRegisterRepository pendingRegisterRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;

    public User register(RegisterRequest request) {
        // ❌ Chặn đăng ký nếu role không phải CUSTOMER
        if (request.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only CUSTOMER can register manually.");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        return userRepository.save(user);
    }

    public Optional<User> login(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()));
    }

    public String generateToken(User user) {
        return jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
    }

    public void requestRegister(PendingRegisterRequest request) {
        // ✅ Kiểm tra nếu email đã được dùng
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã được sử dụng");
        }

        if (pendingRegisterRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đang chờ xác nhận, vui lòng kiểm tra email");
        }

        // ✅ Sinh OTP và lưu pending register
        String otp = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expires = LocalDateTime.now().plusMinutes(5);
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        PendingRegister pending = PendingRegister.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(hashedPassword)
                .otp(otp)
                .expiresAt(expires)
                .verified(false)
                .build();

        pendingRegisterRepository.save(pending);
        mailService.sendOtpEmail(request.getEmail(), otp);
    }


    public void confirmRegister(String email, String otp) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã xác nhận và tồn tại");
        }
        PendingRegister pending = pendingRegisterRepository.findByEmailAndOtpAndVerifiedFalse(email, otp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP không đúng hoặc đã xác nhận"));

        if (pending.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP hết hạn");
        }

        User user = User.builder()
                .email(pending.getEmail())
                .fullName(pending.getFullName())
                .phone(pending.getPhone())
                .passwordHash(pending.getPasswordHash())
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.save(user);
        pending.setVerified(true);
        pendingRegisterRepository.save(pending);
    }
    // Gửi OTP đến email để reset mật khẩu
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email không tồn tại"));

        String otp = String.valueOf((int)(Math.random() * 900000 + 100000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setEmail(email);
        resetRequest.setOtp(otp);
        resetRequest.setExpiresAt(expiresAt);
        resetRequest.setVerified(false);

        passwordResetRequestRepository.save(resetRequest);
        mailService.sendOtpEmail(email, otp);
    }
    // Xác nhận OTP từ email
    public void confirmResetPassword(String email, String otp) {
        // Lấy danh sách các yêu cầu reset hợp lệ
        List<PasswordResetRequest> matches = passwordResetRequestRepository
                .findAllByEmailAndOtpAndVerifiedFalse(email, otp);

        if (matches.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP không hợp lệ hoặc đã xác nhận");
        }

        // Ưu tiên bản ghi mới nhất
        PasswordResetRequest request = matches.stream()
                .sorted(Comparator.comparing(PasswordResetRequest::getExpiresAt).reversed())
                .findFirst()
                .get();

        if (request.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP đã hết hạn");
        }

        request.setVerified(true);
        passwordResetRequestRepository.save(request);
    }
    public void updatePassword(String email, String newPassword) {
        // Lấy danh sách các yêu cầu reset mật khẩu theo email, sắp xếp theo thời gian giảm dần
        List<PasswordResetRequest> requests = passwordResetRequestRepository
                .findByEmailAndVerifiedTrueOrderByExpiresAtDesc(email);

        if (requests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy yêu cầu đặt lại mật khẩu");
        }

        // Lấy bản ghi mới nhất
        PasswordResetRequest request = requests.get(0);

        if (!request.isVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP chưa được xác nhận");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Xóa tất cả yêu cầu reset mật khẩu liên quan đến email này để đảm bảo an toàn
        passwordResetRequestRepository.deleteAll(requests);
    }
}