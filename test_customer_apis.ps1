# Test Customer APIs
Write-Host "Testing Customer APIs..." -ForegroundColor Green

# 1. Login với customer
Write-Host "`n1. Login với customer..." -ForegroundColor Yellow
$loginBody = @{
    email = "keuthuy81@gmail.com"
    password = "password"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "Login successful, token obtained" -ForegroundColor Green
} catch {
    Write-Host "Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# 2. Tạo ticket CIVIL SELF_TEST để test
Write-Host "`n2. Tạo ticket CIVIL SELF_TEST..." -ForegroundColor Yellow
$ticketBody = @{
    type = "CIVIL"
    method = "SELF_TEST"
    reason = "Test customer APIs"
    customerId = 1
    address = "Test address"
    phone = "0123456789"
    email = "test@example.com"
    sample1Name = "Sample 1"
    sample2Name = "Sample 2"
    amount = 500000
} | ConvertTo-Json

try {
    $ticketResponse = Invoke-RestMethod -Uri "http://localhost:8080/tickets" -Method POST -Body $ticketBody -Headers $headers
    $ticketId = $ticketResponse.ticketId
    Write-Host "Ticket created successfully" -ForegroundColor Green
    Write-Host "Ticket ID: $ticketId" -ForegroundColor Cyan
    Write-Host "Initial Status: $($ticketResponse.status)" -ForegroundColor Cyan
} catch {
    Write-Host "Ticket creation failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 3. Test confirm-received (CONFIRMED -> RECEIVED)
Write-Host "`n3. Testing confirm-received (CONFIRMED -> RECEIVED)..." -ForegroundColor Yellow
try {
    $confirmReceivedResponse = Invoke-RestMethod -Uri "http://localhost:8080/customer/tickets/$ticketId/confirm-received" -Method PUT -Headers $headers
    Write-Host "Confirm received successful" -ForegroundColor Green
    Write-Host "New Status: $($confirmReceivedResponse.status)" -ForegroundColor Cyan
    Write-Host "Message: $($confirmReceivedResponse.message)" -ForegroundColor Cyan
    
    if ($confirmReceivedResponse.status -eq "RECEIVED") {
        Write-Host "✅ Status correctly changed to RECEIVED" -ForegroundColor Green
    } else {
        Write-Host "❌ Status should be RECEIVED but got: $($confirmReceivedResponse.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "Confirm received failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Error details: $($_.Exception.Response)" -ForegroundColor Red
}

# 4. Test confirm-sent (RECEIVED -> PENDING)
Write-Host "`n4. Testing confirm-sent (RECEIVED -> PENDING)..." -ForegroundColor Yellow
try {
    $confirmSentResponse = Invoke-RestMethod -Uri "http://localhost:8080/customer/tickets/$ticketId/confirm-sent" -Method PUT -Headers $headers
    Write-Host "Confirm sent successful" -ForegroundColor Green
    Write-Host "New Status: $($confirmSentResponse.status)" -ForegroundColor Cyan
    Write-Host "Message: $($confirmSentResponse.message)" -ForegroundColor Cyan
    
    if ($confirmSentResponse.status -eq "PENDING") {
        Write-Host "✅ Status correctly changed to PENDING" -ForegroundColor Green
    } else {
        Write-Host "❌ Status should be PENDING but got: $($confirmSentResponse.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "Confirm sent failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Error details: $($_.Exception.Response)" -ForegroundColor Red
}

# 5. Test error cases
Write-Host "`n5. Testing error cases..." -ForegroundColor Yellow

# Test confirm-sent again (should fail because status is now PENDING, not RECEIVED)
Write-Host "`n5.1. Testing confirm-sent again (should fail)..." -ForegroundColor Yellow
try {
    $confirmSentResponse2 = Invoke-RestMethod -Uri "http://localhost:8080/customer/tickets/$ticketId/confirm-sent" -Method PUT -Headers $headers
    Write-Host "❌ Should have failed but succeeded" -ForegroundColor Red
} catch {
    Write-Host "✅ Correctly failed: $($_.Exception.Message)" -ForegroundColor Green
}

# Test with different user (should fail)
Write-Host "`n5.2. Testing with different user (should fail)..." -ForegroundColor Yellow
$loginBody2 = @{
    email = "kenfileague1234@gmail.com"
    password = "password"
} | ConvertTo-Json

try {
    $loginResponse2 = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -Body $loginBody2 -ContentType "application/json"
    $token2 = $loginResponse2.token
    
    $headers2 = @{
        "Authorization" = "Bearer $token2"
        "Content-Type" = "application/json"
    }
    
    $confirmSentResponse3 = Invoke-RestMethod -Uri "http://localhost:8080/customer/tickets/$ticketId/confirm-sent" -Method PUT -Headers $headers2
    Write-Host "❌ Should have failed but succeeded" -ForegroundColor Red
} catch {
    Write-Host "✅ Correctly failed: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`nTest completed!" -ForegroundColor Green 