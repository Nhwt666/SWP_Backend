# Backend Checklist - Ticket Creation & Status Handling

## ✅ **Pre-Development Checklist**

### 1. **Enum Validation**
- [ ] `TicketStatus` enum có đầy đủ các giá trị cần thiết
- [ ] `TicketType` enum có giá trị `CIVIL`
- [ ] `TestMethod` enum có giá trị `SELF_TEST`
- [ ] Không có validation nào reject status `CONFIRMED`

### 2. **DTO Structure**
- [ ] `TicketRequest` có field `status` với type `TicketStatus`
- [ ] Tất cả fields required đều có validation
- [ ] Mapping DTO → Entity đầy đủ, không bỏ sót trường

### 3. **Database Schema**
- [ ] Migration đã chạy thành công
- [ ] Column `status` có thể nhận giá trị `CONFIRMED`
- [ ] Không có constraint nào reject `CONFIRMED`

## ✅ **Development Checklist**

### 1. **Controller Layer**
```java
@PostMapping("/tickets/after-payment")
public ResponseEntity<?> createTicketAfterPayment(@RequestBody TicketRequest request, Authentication authentication) {
    try {
        // ✅ Log dữ liệu đầu vào
        log.info("Received ticket creation request: type={}, method={}, status={}", 
                request.getType(), request.getMethod(), request.getStatus());
        
        // ✅ Validation cơ bản
        if (request.getType() == null || request.getMethod() == null) {
            return ResponseEntity.badRequest().body("Thiếu thông tin type hoặc method");
        }
        
        // ✅ Logic tạo ticket
        Ticket ticket = ticketService.createTicketFromRequest(request);
        
        // ✅ Log kết quả
        log.info("Ticket created successfully: id={}, status={}", ticket.getId(), ticket.getStatus());
        
        return ResponseEntity.ok(ticket);
        
    } catch (IllegalArgumentException e) {
        log.error("Invalid request data: {}", e.getMessage());
        return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ: " + e.getMessage());
        
    } catch (Exception e) {
        log.error("Error creating ticket", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Lỗi tạo ticket: " + e.getMessage());
    }
}
```

### 2. **Service Layer**
```java
@Transactional
public Ticket createTicketFromRequest(TicketRequest request) {
    // ✅ Debug logs
    log.debug("Creating ticket with status: {}", request.getStatus());
    
    Ticket ticket = new Ticket();
    
    // ✅ Mapping đầy đủ
    ticket.setType(TicketType.valueOf(request.getType()));
    ticket.setMethod(TestMethod.valueOf(request.getMethod()));
    ticket.setReason(request.getReason());
    
    // ✅ Status handling - KHÔNG hardcode
    if (request.getStatus() != null) {
        ticket.setStatus(request.getStatus());
        log.debug("Using status from request: {}", request.getStatus());
    } else {
        ticket.setStatus(TicketStatus.PENDING);
        log.debug("No status in request, using default: PENDING");
    }
    
    // ✅ Các trường khác
    ticket.setCustomer(userRepository.findById(request.getCustomerId())
        .orElseThrow(() -> new RuntimeException("Customer not found")));
    ticket.setAmount(request.getAmount());
    // ... các trường khác
    
    // ✅ Lưu và log kết quả
    Ticket savedTicket = ticketRepository.save(ticket);
    log.info("Ticket created: id={}, status={}, type={}, method={}", 
            savedTicket.getId(), savedTicket.getStatus(), 
            savedTicket.getType(), savedTicket.getMethod());
    
    return savedTicket;
}
```

### 3. **Validation Rules**
```java
// ✅ Business logic validation
private void validateTicketRequest(TicketRequest request) {
    // Kiểm tra CIVIL SELF_TEST có thể có status CONFIRMED
    if (request.getType().equals("CIVIL") && 
        request.getMethod().equals("SELF_TEST") && 
        request.getStatus() == TicketStatus.CONFIRMED) {
        log.debug("Valid CIVIL SELF_TEST with CONFIRMED status");
        return; // ✅ Hợp lệ
    }
    
    // Các validation khác...
}
```

## ✅ **Testing Checklist**

### 1. **Unit Tests**
- [ ] Test tạo ticket CIVIL SELF_TEST với status CONFIRMED
- [ ] Test tạo ticket khác với status PENDING
- [ ] Test validation khi thiếu dữ liệu
- [ ] Test error handling

### 2. **Integration Tests**
- [ ] Test endpoint `/tickets/after-payment` với Postman
- [ ] Test endpoint `/tickets` với Postman
- [ ] Verify database lưu đúng status
- [ ] Verify logs hiển thị đúng thông tin

### 3. **Manual Testing**
```bash
# Test với curl
curl -X POST http://localhost:8080/tickets/after-payment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "type": "CIVIL",
    "method": "SELF_TEST", 
    "status": "CONFIRMED",
    "amount": 500000,
    "customerId": 1,
    "reason": "Test ticket"
  }'
```

## ✅ **Deployment Checklist**

### 1. **Pre-Deployment**
- [ ] Tất cả tests pass
- [ ] Code review completed
- [ ] Database migration ready
- [ ] Logs configured properly

### 2. **Deployment**
- [ ] Deploy backend code
- [ ] Run database migration
- [ ] Restart application
- [ ] Verify application starts successfully

### 3. **Post-Deployment**
- [ ] Test tạo ticket CIVIL SELF_TEST
- [ ] Verify status được lưu đúng
- [ ] Check logs không có error
- [ ] Monitor application performance

## ✅ **Monitoring Checklist**

### 1. **Log Monitoring**
- [ ] Log level set to INFO/DEBUG
- [ ] Log format consistent
- [ ] Error logs captured
- [ ] Performance logs enabled

### 2. **Database Monitoring**
- [ ] Check ticket status in database
- [ ] Monitor query performance
- [ ] Check for constraint violations
- [ ] Verify data integrity

### 3. **Application Monitoring**
- [ ] Health check endpoint working
- [ ] Memory usage normal
- [ ] Response time acceptable
- [ ] No memory leaks

## 🚨 **Common Issues & Solutions**

### Issue 1: Status vẫn là PENDING
**Cause**: Hardcode status trong code
**Solution**: Sử dụng status từ request, không hardcode

### Issue 2: Validation reject CONFIRMED
**Cause**: Business logic validation sai
**Solution**: Cập nhật validation cho phép CONFIRMED cho CIVIL SELF_TEST

### Issue 3: Mapping bỏ sót status
**Cause**: DTO mapping không đầy đủ
**Solution**: Kiểm tra và sửa mapping

### Issue 4: Database constraint
**Cause**: Migration chưa chạy hoặc constraint sai
**Solution**: Chạy migration và kiểm tra schema

## 📝 **Documentation**

### 1. **API Documentation**
- [ ] Swagger/OpenAPI updated
- [ ] Request/Response examples
- [ ] Error codes documented
- [ ] Status codes documented

### 2. **Code Documentation**
- [ ] JavaDoc comments
- [ ] README updated
- [ ] Architecture diagrams
- [ ] Deployment guide

---

**🎯 Mục tiêu: Đảm bảo ticket CIVIL SELF_TEST luôn được tạo với status CONFIRMED thay vì PENDING** 