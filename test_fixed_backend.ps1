# Test Backend After Fix
Write-Host "Testing Backend After Fix..." -ForegroundColor Green

# 1. Test basic connectivity
Write-Host "`n1. Testing basic connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/tickets/debug/enums" -Method GET -Headers @{"Authorization"="Bearer test"}
    Write-Host "Backend is running" -ForegroundColor Green
} catch {
    Write-Host "Backend is not responding or needs auth" -ForegroundColor Yellow
}

# 2. Login to get token with real user
Write-Host "`n2. Getting authentication token..." -ForegroundColor Yellow
$loginBody = @{
    email = "admin@gmail.com"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "Login successful, token obtained" -ForegroundColor Green
} catch {
    Write-Host "Login failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Trying with different password..." -ForegroundColor Yellow
    
    # Try with different password
    $loginBody2 = @{
        email = "admin@gmail.com"
        password = "password"
    } | ConvertTo-Json
    
    try {
        $loginResponse2 = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -Body $loginBody2 -ContentType "application/json"
        $token = $loginResponse2.token
        Write-Host "Login successful with password 'password'" -ForegroundColor Green
    } catch {
        Write-Host "Login failed with password 'password': $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

# 3. Test debug endpoint
Write-Host "`n3. Testing debug endpoint..." -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $debugResponse = Invoke-RestMethod -Uri "http://localhost:8080/tickets/debug/enums" -Method GET -Headers $headers
    Write-Host "Debug endpoint working" -ForegroundColor Green
    Write-Host "Status values: $($debugResponse.statuses -join ', ')" -ForegroundColor Cyan
    Write-Host "Type values: $($debugResponse.types -join ', ')" -ForegroundColor Cyan
    Write-Host "Method values: $($debugResponse.methods -join ', ')" -ForegroundColor Cyan
    
    # Check if CONFIRMED is in statuses
    if ($debugResponse.statuses -contains "CONFIRMED") {
        Write-Host "CONFIRMED status is available in enum" -ForegroundColor Green
    } else {
        Write-Host "CONFIRMED status is NOT available in enum" -ForegroundColor Red
    }
} catch {
    Write-Host "Debug endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Test ticket creation with CIVIL SELF_TEST
Write-Host "`n4. Testing ticket creation with CIVIL SELF_TEST..." -ForegroundColor Yellow
$ticketBody = @{
    type = "CIVIL"
    method = "SELF_TEST"
    reason = "Test reason"
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
    Write-Host "Ticket creation successful" -ForegroundColor Green
    Write-Host "Ticket ID: $($ticketResponse.ticketId)" -ForegroundColor Cyan
    Write-Host "Ticket Status: $($ticketResponse.status)" -ForegroundColor Cyan
    Write-Host "Ticket Type: $($ticketResponse.type)" -ForegroundColor Cyan
    Write-Host "Ticket Method: $($ticketResponse.method)" -ForegroundColor Cyan
    
    if ($ticketResponse.status -eq "CONFIRMED") {
        Write-Host "Status correctly set to CONFIRMED for CIVIL SELF_TEST" -ForegroundColor Green
    } else {
        Write-Host "Status should be CONFIRMED but got: $($ticketResponse.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "Ticket creation failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Error details: $($_.Exception.Response)" -ForegroundColor Red
}

# 5. Test after-payment endpoint
Write-Host "`n5. Testing after-payment endpoint..." -ForegroundColor Yellow
$paymentBody = @{
    type = "CIVIL"
    method = "SELF_TEST"
    reason = "Test payment reason"
    address = "Test payment address"
    phone = "0123456789"
    email = "test@example.com"
    sample1Name = "Payment Sample 1"
    sample2Name = "Payment Sample 2"
    amount = 500000
} | ConvertTo-Json

try {
    $paymentResponse = Invoke-RestMethod -Uri "http://localhost:8080/tickets/after-payment" -Method POST -Body $paymentBody -Headers $headers
    Write-Host "After-payment ticket creation successful" -ForegroundColor Green
    Write-Host "Ticket ID: $($paymentResponse.ticketId)" -ForegroundColor Cyan
    Write-Host "Ticket Status: $($paymentResponse.status)" -ForegroundColor Cyan
    Write-Host "Ticket Type: $($paymentResponse.type)" -ForegroundColor Cyan
    Write-Host "Ticket Method: $($paymentResponse.method)" -ForegroundColor Cyan
    Write-Host "Amount: $($paymentResponse.amount)" -ForegroundColor Cyan
    
    if ($paymentResponse.status -eq "CONFIRMED") {
        Write-Host "After-payment status correctly set to CONFIRMED for CIVIL SELF_TEST" -ForegroundColor Green
    } else {
        Write-Host "After-payment status should be CONFIRMED but got: $($paymentResponse.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "After-payment ticket creation failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Error details: $($_.Exception.Response)" -ForegroundColor Red
}

Write-Host "`nTest completed!" -ForegroundColor Green 