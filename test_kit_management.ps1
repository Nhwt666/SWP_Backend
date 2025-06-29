# Test script for DNA testing kit management endpoints
# This script tests the new CONFIRMED -> RECEIVED -> PENDING workflow for CIVIL SELF_TEST

$baseUrl = "http://localhost:8080"
$authToken = "YOUR_JWT_TOKEN_HERE"  # Replace with actual JWT token

Write-Host "🧪 Testing DNA Testing Kit Management Endpoints" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green

# Test 1: Create ticket with CONFIRMED status (CIVIL SELF_TEST)
Write-Host "`n1. Testing ticket creation with CONFIRMED status..." -ForegroundColor Yellow
$createTicketBody = @{
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

$createResponse = Invoke-RestMethod -Uri "$baseUrl/tickets" -Method POST -Headers @{
    "Authorization" = "Bearer $authToken"
    "Content-Type" = "application/json"
} -Body $createTicketBody -ErrorAction SilentlyContinue

if ($createResponse) {
    Write-Host "✅ Ticket created successfully with CONFIRMED status" -ForegroundColor Green
    Write-Host "   Ticket ID: $($createResponse.id)" -ForegroundColor Cyan
    Write-Host "   Status: $($createResponse.status)" -ForegroundColor Cyan
    $ticketId = $createResponse.id
} else {
    Write-Host "❌ Failed to create ticket" -ForegroundColor Red
    $ticketId = 1  # Use default for testing
}

# Test 2: Customer confirms receiving kit (CONFIRMED -> RECEIVED)
Write-Host "`n2. Testing customer confirming kit received..." -ForegroundColor Yellow
$confirmReceivedResponse = Invoke-RestMethod -Uri "$baseUrl/customer/tickets/$ticketId/confirm-received" -Method PUT -Headers @{
    "Authorization" = "Bearer $authToken"
    "Content-Type" = "application/json"
} -ErrorAction SilentlyContinue

if ($confirmReceivedResponse) {
    Write-Host "✅ Kit received confirmation successful" -ForegroundColor Green
    Write-Host "   Status: $($confirmReceivedResponse.status)" -ForegroundColor Cyan
    Write-Host "   Message: $($confirmReceivedResponse.message)" -ForegroundColor Cyan
} else {
    Write-Host "❌ Failed to confirm kit received" -ForegroundColor Red
}

# Test 3: Customer confirms sending kit back (RECEIVED -> PENDING)
Write-Host "`n3. Testing customer confirming kit sent back..." -ForegroundColor Yellow
$confirmSentResponse = Invoke-RestMethod -Uri "$baseUrl/customer/tickets/$ticketId/confirm-sent" -Method PUT -Headers @{
    "Authorization" = "Bearer $authToken"
    "Content-Type" = "application/json"
} -ErrorAction SilentlyContinue

if ($confirmSentResponse) {
    Write-Host "✅ Kit sent confirmation successful" -ForegroundColor Green
    Write-Host "   Status: $($confirmSentResponse.status)" -ForegroundColor Cyan
    Write-Host "   Message: $($confirmSentResponse.message)" -ForegroundColor Cyan
} else {
    Write-Host "❌ Failed to confirm kit sent" -ForegroundColor Red
}

# Test 4: Get ticket details to see final status
Write-Host "`n4. Getting ticket details..." -ForegroundColor Yellow
$ticketResponse = Invoke-RestMethod -Uri "$baseUrl/tickets/$ticketId" -Method GET -Headers @{
    "Authorization" = "Bearer $authToken"
    "Content-Type" = "application/json"
} -ErrorAction SilentlyContinue

if ($ticketResponse) {
    Write-Host "✅ Ticket details retrieved" -ForegroundColor Green
    Write-Host "   Ticket ID: $($ticketResponse.id)" -ForegroundColor Cyan
    Write-Host "   Status: $($ticketResponse.status)" -ForegroundColor Cyan
    Write-Host "   Type: $($ticketResponse.type)" -ForegroundColor Cyan
    Write-Host "   Method: $($ticketResponse.method)" -ForegroundColor Cyan
    Write-Host "   Updated At: $($ticketResponse.updatedAt)" -ForegroundColor Cyan
} else {
    Write-Host "❌ Failed to get ticket details" -ForegroundColor Red
}

Write-Host "`n🎉 Kit management testing completed!" -ForegroundColor Green
Write-Host "Expected flow for CIVIL SELF_TEST: CONFIRMED -> RECEIVED -> PENDING -> IN_PROGRESS -> COMPLETED" -ForegroundColor Cyan 