# 🎯 Customer APIs Summary

## 📋 **API Overview**

### **1. API confirm-received**
- **Endpoint:** `PUT /customer/tickets/{ticketId}/confirm-received`
- **Purpose:** Customer xác nhận đã nhận kit
- **Status Change:** `CONFIRMED` → `RECEIVED`
- **Validation:**
  - Chỉ chủ ticket mới được thực hiện
  - Chỉ áp dụng cho CIVIL SELF_TEST
  - Ticket phải ở trạng thái CONFIRMED

### **2. API confirm-sent**
- **Endpoint:** `PUT /customer/tickets/{ticketId}/confirm-sent`
- **Purpose:** Customer xác nhận đã gửi kit về
- **Status Change:** `RECEIVED` → `PENDING`
- **Validation:**
  - Chỉ chủ ticket mới được thực hiện
  - Chỉ áp dụng cho CIVIL SELF_TEST
  - Ticket phải ở trạng thái RECEIVED

## 🔄 **Complete Flow**

```
1. Tạo ticket CIVIL SELF_TEST → Status: CONFIRMED
   ↓
2. Customer nhận kit → PUT /confirm-received → Status: RECEIVED
   ↓
3. Customer gửi kit → PUT /confirm-sent → Status: PENDING
   ↓
4. Staff xử lý → Status: IN_PROGRESS → COMPLETED
```

## 📝 **Implementation Details**

### **confirmKitReceived()**
```java
@Transactional
public Ticket confirmKitReceived(Long ticketId, Long userId) {
    // 1. Validate ticket exists
    // 2. Validate user is ticket owner
    // 3. Validate ticket is CIVIL SELF_TEST
    // 4. Validate status is CONFIRMED
    // 5. Update status to RECEIVED
    // 6. Create notification for staff
    // 7. Return updated ticket
}
```

### **confirmKitSent()**
```java
@Transactional
public Ticket confirmKitSent(Long ticketId, Long userId) {
    // 1. Validate ticket exists
    // 2. Validate user is ticket owner
    // 3. Validate ticket is CIVIL SELF_TEST
    // 4. Validate status is RECEIVED
    // 5. Update status to PENDING
    // 6. Create notification for staff
    // 7. Return updated ticket
}
```

## 🧪 **Test Cases**

### **✅ Success Cases**
1. **confirm-received:** CONFIRMED → RECEIVED
2. **confirm-sent:** RECEIVED → PENDING

### **❌ Error Cases**
1. **Wrong user:** Chỉ chủ ticket mới được thực hiện
2. **Wrong ticket type:** Chỉ áp dụng cho CIVIL SELF_TEST
3. **Wrong status:** 
   - confirm-received: Phải ở CONFIRMED
   - confirm-sent: Phải ở RECEIVED
4. **Invalid ticket ID:** Ticket không tồn tại

## 📊 **Response Format**

### **Success Response**
```json
{
    "message": "Đã xác nhận nhận kit thành công",
    "ticketId": 123,
    "status": "RECEIVED",
    "updatedAt": "2025-06-29T14:30:00"
}
```

### **Error Response**
```json
{
    "error": "Bad Request",
    "message": "Ticket phải ở trạng thái CONFIRMED"
}
```

## 🔐 **Security**

- **Authentication:** Required (JWT token)
- **Authorization:** Chỉ chủ ticket mới được thực hiện
- **Role:** CUSTOMER role required

## 📱 **Frontend Integration**

### **confirm-received**
```javascript
// Khi customer nhận kit
const response = await fetch(`/customer/tickets/${ticketId}/confirm-received`, {
    method: 'PUT',
    headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    }
});

if (response.ok) {
    // Đóng modal, refresh data
    closeModal();
    fetchHistory();
}
```

### **confirm-sent**
```javascript
// Khi customer gửi kit
const response = await fetch(`/customer/tickets/${ticketId}/confirm-sent`, {
    method: 'PUT',
    headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    }
});

if (response.ok) {
    // Đóng modal, refresh data
    closeModal();
    fetchHistory();
}
```

## 🎯 **Status Flow Validation**

| Step | Action | From Status | To Status | API |
|------|--------|-------------|-----------|-----|
| 1 | Tạo ticket | - | CONFIRMED | POST /tickets |
| 2 | Nhận kit | CONFIRMED | RECEIVED | PUT /confirm-received |
| 3 | Gửi kit | RECEIVED | PENDING | PUT /confirm-sent |
| 4 | Staff xử lý | PENDING | IN_PROGRESS | PUT /tickets/{id}/assign |
| 5 | Hoàn thành | IN_PROGRESS | COMPLETED | PUT /tickets/{id}/complete |

## ✅ **Conclusion**

**Backend đã implement đúng yêu cầu:**

1. ✅ **confirm-received:** CONFIRMED → RECEIVED
2. ✅ **confirm-sent:** RECEIVED → PENDING
3. ✅ **Validation:** Đầy đủ các điều kiện
4. ✅ **Security:** Authentication và Authorization
5. ✅ **Notifications:** Tự động tạo thông báo cho staff
6. ✅ **Error handling:** Xử lý lỗi chi tiết

**Frontend có thể sử dụng ngay! 🎉** 