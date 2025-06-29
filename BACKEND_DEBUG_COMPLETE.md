# 🔍 Backend Debug Complete Analysis

## 📋 **Vấn Đề Đã Phát Hiện và Fix**

### **🚨 Root Cause: CHECK Constraint Conflict**

**Vấn đề:** Có **2 CHECK constraints** trên cột `status` của bảng `tickets`:

1. **CK__tickets__status__403A8C7D** (CŨ): 
   ```sql
   ([status]='COMPLETED' OR [status]='IN_PROGRESS' OR [status]='PENDING' OR [status]='REJECTED')
   ```
   **❌ KHÔNG có CONFIRMED**

2. **CK_tickets_status** (MỚI từ migration V25):
   ```sql
   ([status]='REJECTED' OR [status]='CANCELLED' OR [status]='COMPLETED' OR [status]='CONFIRMED' OR [status]='RECEIVED' OR [status]='IN_PROGRESS' OR [status]='PENDING')
   ```
   **✅ CÓ CONFIRMED**

**Lỗi:** Constraint cũ vẫn tồn tại và chặn việc insert status `CONFIRMED`

### **🔧 Fix Đã Thực Hiện**

```sql
-- Xóa constraint cũ
ALTER TABLE tickets DROP CONSTRAINT CK__tickets__status__403A8C7D;
```

## 📊 **Thông Tin Database**

### **1. Users Available**
```sql
SELECT TOP 5 user_id, email, full_name, role, wallet_balance FROM users
```

**Kết quả:**
- `user_id=1`: keuthuy81@gmail.com (CUSTOMER, balance: 3,900,000)
- `user_id=2`: kenfileague1234@gmail.com (CUSTOMER, balance: 2,000,000)
- `user_id=3`: oxq65485@toaik.com (CUSTOMER, balance: NULL)
- `user_id=4`: ttt86874@toaik.com (CUSTOMER, balance: 7,300,000)
- `user_id=5`: admin@gmail.com (ADMIN, balance: 0.00)

### **2. Ticket Status Values**
```sql
SELECT DISTINCT status FROM tickets;
```

**Kết quả:**
- COMPLETED
- IN_PROGRESS
- PENDING
- REJECTED

**❌ CONFIRMED chưa có** (do chưa có ticket nào được tạo với status này)

### **3. Constraints After Fix**
```sql
sp_helpconstraint tickets
```

**Kết quả:** Chỉ còn 1 constraint status:
- **CK_tickets_status**: Bao gồm tất cả status values (PENDING, IN_PROGRESS, RECEIVED, CONFIRMED, COMPLETED, CANCELLED, REJECTED)

## 🔍 **Backend Code Analysis**

### **1. ✅ Java Enum TicketStatus**
```java
public enum TicketStatus {
    PENDING,
    IN_PROGRESS,
    RECEIVED,
    CONFIRMED,   // ✅ Có CONFIRMED
    COMPLETED,
    CANCELLED,
    REJECTED,
}
```

### **2. ✅ Backend Logic**
```java
// Logic trong createTicketAfterPayment():
if (request.getType().equals("CIVIL") && request.getMethod().equals("SELF_TEST")) {
    ticket.setStatus(TicketStatus.CONFIRMED);
    System.out.println("   ✅ CIVIL SELF_TEST detected, setting status: CONFIRMED");
} else {
    ticket.setStatus(TicketStatus.PENDING);
    System.out.println("   ✅ Other ticket type, setting status: PENDING");
}
```

### **3. ✅ Debug Endpoint**
```java
@GetMapping("/debug/enums")
public ResponseEntity<?> getEnums() {
    return ResponseEntity.ok(Map.of(
        "statuses", Arrays.stream(TicketStatus.values()).map(Enum::name).collect(Collectors.toList()),
        "types", Arrays.stream(TicketType.values()).map(Enum::name).collect(Collectors.toList()),
        "methods", Arrays.stream(TestMethod.values()).map(Enum::name).collect(Collectors.toList())
    ));
}
```

## 📝 **Enhanced Logging**

### **Logging Chi Tiết Đã Thêm**
```java
// Trong createTicketAfterPayment():
System.out.println("🔍 DEBUG: createTicketAfterPayment - START");
System.out.println("   Request type: " + request.getType());
System.out.println("   Request method: " + request.getMethod());
System.out.println("   Request amount: " + request.getAmount());
System.out.println("   Request customerId: " + request.getCustomerId());
// ... và nhiều log khác
```

## 🧪 **Test Cases**

### **1. Test User Authentication**
```powershell
# Login với user thật
$loginBody = @{
    email = "admin@gmail.com"
    password = "admin123"  # hoặc "password"
} | ConvertTo-Json
```

### **2. Test Debug Endpoint**
```powershell
# GET /tickets/debug/enums
# Expected: CONFIRMED trong statuses array
```

### **3. Test Ticket Creation**
```powershell
# POST /tickets với CIVIL SELF_TEST
# Expected: status = CONFIRMED
```

### **4. Test After-Payment**
```powershell
# POST /tickets/after-payment với CIVIL SELF_TEST
# Expected: status = CONFIRMED
```

## 🎯 **Kết Luận**

### **✅ Đã Fix:**
1. **CHECK Constraint:** Đã xóa constraint cũ không có CONFIRMED
2. **Backend Logic:** Logic set status CONFIRMED đúng
3. **Java Enum:** CONFIRMED có trong enum
4. **Logging:** Đã thêm logging chi tiết để debug
5. **Debug Endpoint:** Đã thêm endpoint để test enum values

### **📋 Cần Test:**
1. **Authentication:** Test với user thật (admin@gmail.com)
2. **Ticket Creation:** Test tạo ticket CIVIL SELF_TEST
3. **After-Payment:** Test endpoint after-payment
4. **Logs:** Kiểm tra logs chi tiết khi có lỗi

### **🚀 Expected Results:**
- ✅ Debug endpoint trả về CONFIRMED trong statuses
- ✅ Ticket creation với CIVIL SELF_TEST → status = CONFIRMED
- ✅ After-payment với CIVIL SELF_TEST → status = CONFIRMED
- ✅ Không có lỗi 500 Internal Server Error
- ✅ Logs chi tiết hiển thị đầy đủ thông tin

## 📁 **Files Modified**
1. `src/main/java/com/group2/ADN/controller/TicketController.java` - Thêm logging và debug endpoint
2. `test_fixed_backend.ps1` - Test script với user thật
3. `BACKEND_DEBUG_COMPLETE.md` - Documentation này

**Backend đã sẵn sàng để test! 🎉** 