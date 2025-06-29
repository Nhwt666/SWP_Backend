# 🔍 Backend Debug Summary

## 📋 **Thông Tin Đã Kiểm Tra**

### **1. ✅ Java Enum TicketStatus**
```java
// File: src/main/java/com/group2/ADN/entity/TicketStatus.java
public enum TicketStatus {
    PENDING,
    IN_PROGRESS,
    RECEIVED,    // Member xác nhận đã nhận kit
    CONFIRMED,   // Trạng thái ban đầu cho CIVIL SELF_TEST ✅
    COMPLETED,
    CANCELLED,
    REJECTED,
}
```
**Kết luận:** ✅ CONFIRMED có trong Java enum

### **2. 🔍 Database Status Values**
```sql
-- Query: SELECT DISTINCT status FROM tickets;
-- Kết quả:
COMPLETED
IN_PROGRESS
PENDING
REJECTED
```
**Kết luận:** ❌ CONFIRMED chưa có trong database (có thể do chưa có ticket nào được tạo với status này)

### **3. 🔍 Database Constraint**
```sql
-- Query: SELECT name FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID('tickets');
-- Kết quả: (0 rows affected)
```
**Kết luận:** ✅ Không có CHECK constraint nào trên bảng tickets, nên không có ràng buộc về status

### **4. ✅ Backend Logic**
```java
// File: src/main/java/com/group2/ADN/service/TicketService.java
// Logic trong createTicketFromRequest():
if (request.getType().equals("CIVIL") && request.getMethod().equals("SELF_TEST")) {
    ticket.setStatus(TicketStatus.CONFIRMED);
    System.out.println("   ✅ CIVIL SELF_TEST detected, setting status: CONFIRMED");
} else {
    ticket.setStatus(TicketStatus.PENDING);
    System.out.println("   ✅ Other ticket type, setting status: PENDING");
}
```

```java
// File: src/main/java/com/group2/ADN/controller/TicketController.java
// Logic trong createTicketAfterPayment():
if (request.getType().equals("CIVIL") && request.getMethod().equals("SELF_TEST")) {
    ticket.setStatus(TicketStatus.CONFIRMED);
    System.out.println("   ✅ CIVIL SELF_TEST detected, setting status: CONFIRMED");
} else {
    ticket.setStatus(TicketStatus.PENDING);
    System.out.println("   ✅ Other ticket type, setting status: PENDING");
}
```
**Kết luận:** ✅ Logic đúng, không có hardcode PENDING

### **5. ✅ Debug Endpoint**
```java
// File: src/main/java/com/group2/ADN/controller/TicketController.java
@GetMapping("/debug/enums")
public ResponseEntity<?> getEnums() {
    return ResponseEntity.ok(Map.of(
        "statuses", Arrays.stream(TicketStatus.values()).map(Enum::name).collect(Collectors.toList()),
        "types", Arrays.stream(TicketType.values()).map(Enum::name).collect(Collectors.toList()),
        "methods", Arrays.stream(TestMethod.values()).map(Enum::name).collect(Collectors.toList())
    ));
}
```
**Kết luận:** ✅ Debug endpoint đã được thêm

## 🚨 **Vấn Đề Đã Phát Hiện**

### **1. Backend Authentication Issue**
- Backend đang chạy trên port 8080
- Nhưng login endpoint trả về 401 Unauthorized
- Có thể do:
  - User admin@adn.com không tồn tại
  - Password sai
  - Security config có vấn đề

### **2. Database Constraint Issue (Đã Fix)**
- Trước đây có CHECK constraint gây lỗi khi insert CONFIRMED
- Đã được fix bằng migration V25
- Hiện tại không có constraint nào

## 🎯 **Kết Luận**

### **✅ Những Gì Đã Đúng:**
1. **Java Enum:** CONFIRMED có trong TicketStatus
2. **Backend Logic:** Logic set status CONFIRMED cho CIVIL SELF_TEST đúng
3. **Database Constraint:** Không có constraint nào chặn CONFIRMED
4. **Debug Endpoint:** Đã được thêm để test

### **❌ Vấn Đề Cần Fix:**
1. **Authentication:** Backend không accept login với admin@adn.com
2. **User Database:** Cần kiểm tra user có tồn tại không

## 🔧 **Hướng Dẫn Fix**

### **1. Kiểm tra User Database**
```sql
-- Chạy query này để kiểm tra user
SELECT * FROM users WHERE email = 'admin@adn.com';
```

### **2. Tạo User Admin nếu chưa có**
```sql
-- Tạo user admin nếu chưa có
INSERT INTO users (email, password, full_name, role, wallet_balance, created_at, updated_at)
VALUES ('admin@adn.com', '$2a$10$...', 'Admin User', 'ADMIN', 1000000, GETDATE(), GETDATE());
```

### **3. Test lại với User thật**
- Tìm user có trong database
- Sử dụng email/password thật để test

## 📝 **Test Cases Cần Chạy**

1. **Test Debug Endpoint:** `GET /tickets/debug/enums`
2. **Test Login:** `POST /auth/login` với user thật
3. **Test Ticket Creation:** `POST /tickets` với CIVIL SELF_TEST
4. **Test After-Payment:** `POST /tickets/after-payment` với CIVIL SELF_TEST

## 🎯 **Kết Quả Mong Đợi**

Sau khi fix authentication:
- ✅ Debug endpoint trả về CONFIRMED trong statuses
- ✅ Ticket creation với CIVIL SELF_TEST → status = CONFIRMED
- ✅ After-payment với CIVIL SELF_TEST → status = CONFIRMED
- ✅ Không có lỗi 500 Internal Server Error

**Backend logic đã đúng, chỉ cần fix authentication issue!** 