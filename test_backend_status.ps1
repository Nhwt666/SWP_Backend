# Test script để kiểm tra backend có xử lý đúng status CONFIRMED cho CIVIL SELF_TEST không

$baseUrl = "http://localhost:8080"
$authToken = "YOUR_JWT_TOKEN_HERE"  # Replace with actual JWT token

Write-Host "🧪 Testing Backend Status Handling for CIVIL SELF_TEST" -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Green

# Test 1: Tạo ticket CIVIL SELF_TEST với status CONFIRMED
Write-Host "`n1. Testing ticket creation with CONFIRMED status..." -ForegroundColor Yellow

$ticketData = @{
    type = "CIVIL"
    method = "SELF_TEST"
    status = "CONFIRMED"
    customerId = 1
    amount = 500000
    reason = "DNA testing for civil case"
    address = "123 Test Street"
    phone = "0123456789"
    email = "test@example.com"
    sample1Name = "Sample A"
    sample2Name = "Sample B"
} | ConvertTo-Json

Write-Host "Sending request data:" -ForegroundColor Cyan
Write-Host $ticketData -ForegroundColor Gray

# Test endpoint /tickets (createTicketFromRequest)
$createResponse = Invoke-RestMethod -Uri "$baseUrl/tickets" -Method POST -Headers @{
    "Authorization" = "Bearer $authToken"
    "Content-Type" = "application/json"
} -Body $ticketData -ErrorAction SilentlyContinue

if ($createResponse) {
    Write-Host "✅ Ticket created successfully via /tickets endpoint" -ForegroundColor Green
    Write-Host "   Ticket ID: $($createResponse.id)" -ForegroundColor Cyan
    Write-Host "   Status: $($createResponse.status)" -ForegroundColor Cyan
    Write-Host "   Type: $($createResponse.type)" -ForegroundColor Cyan
    Write-Host "   Method: $($createResponse.method)" -ForegroundColor Cyan
    
    if ($createResponse.status -eq "CONFIRMED") {
        Write-Host "   🎉 SUCCESS: Status is CONFIRMED as expected!" -ForegroundColor Green
    } else {
        Write-Host "   ❌ FAILED: Status is $($createResponse.status), expected CONFIRMED" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Failed to create ticket via /tickets endpoint" -ForegroundColor Red
}

# Test 2: Tạo ticket CIVIL SELF_TEST với status CONFIRMED qua endpoint /after-payment
Write-Host "`n2. Testing ticket creation with CONFIRMED status via /after-payment..." -ForegroundColor Yellow

$afterPaymentData = @{
    type = "CIVIL"
    method = "SELF_TEST"
    status = "CONFIRMED"
    amount = 500000
    reason = "DNA testing for civil case"
    address = "123 Test Street"
    phone = "0123456789"
    email = "test@example.com"
    sample1Name = "Sample A"
    sample2Name = "Sample B"
} | ConvertTo-Json

Write-Host "Sending request data:" -ForegroundColor Cyan
Write-Host $afterPaymentData -ForegroundColor Gray

$afterPaymentResponse = Invoke-RestMethod -Uri "$baseUrl/tickets/after-payment" -Method POST -Headers @{
    "Authorization" = "Bearer $authToken"
    "Content-Type" = "application/json"
} -Body $afterPaymentData -ErrorAction SilentlyContinue

if ($afterPaymentResponse) {
    Write-Host "✅ Ticket created successfully via /after-payment endpoint" -ForegroundColor Green
    Write-Host "   Ticket ID: $($afterPaymentResponse.id)" -ForegroundColor Cyan
    Write-Host "   Status: $($afterPaymentResponse.status)" -ForegroundColor Cyan
    Write-Host "   Type: $($afterPaymentResponse.type)" -ForegroundColor Cyan
    Write-Host "   Method: $($afterPaymentResponse.method)" -ForegroundColor Cyan
    
    if ($afterPaymentResponse.status -eq "CONFIRMED") {
        Write-Host "   🎉 SUCCESS: Status is CONFIRMED as expected!" -ForegroundColor Green
    } else {
        Write-Host "   ❌ FAILED: Status is $($afterPaymentResponse.status), expected CONFIRMED" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Failed to create ticket via /after-payment endpoint" -ForegroundColor Red
}

# Test 3: Kiểm tra database để xem tickets mới nhất
Write-Host "`n3. Checking recent tickets in database..." -ForegroundColor Yellow
Write-Host "   (Check backend console logs for debug information)" -ForegroundColor Gray

Write-Host "`n🎉 Backend status testing completed!" -ForegroundColor Green
Write-Host "Expected: CIVIL SELF_TEST tickets should have status CONFIRMED" -ForegroundColor Cyan
Write-Host "Check backend console for debug logs to see status processing" -ForegroundColor Cyan 