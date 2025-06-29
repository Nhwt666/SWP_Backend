# Comprehensive Backend Test Script for Ticket Creation
# Tests all endpoints and validates status handling

$baseUrl = "http://localhost:8080"
$authToken = "YOUR_JWT_TOKEN_HERE"  # Replace with actual JWT token

Write-Host "🧪 Comprehensive Backend Testing for Ticket Creation" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green

# Function to test endpoint
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method,
        [object]$Body,
        [string]$ExpectedStatus
    )
    
    Write-Host "`n📋 Testing: $Name" -ForegroundColor Yellow
    Write-Host "   URL: $Method $Url" -ForegroundColor Gray
    
    if ($Body) {
        Write-Host "   Body: $($Body | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    }
    
    try {
        $headers = @{
            "Authorization" = "Bearer $authToken"
            "Content-Type" = "application/json"
        }
        
        if ($Body) {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -Body ($Body | ConvertTo-Json -Depth 3) -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -ErrorAction Stop
        }
        
        Write-Host "   ✅ Success" -ForegroundColor Green
        Write-Host "   Response: $($response | ConvertTo-Json -Depth 2)" -ForegroundColor Cyan
        
        if ($ExpectedStatus -and $response.status -eq $ExpectedStatus) {
            Write-Host "   🎉 Status match: $($response.status)" -ForegroundColor Green
        } elseif ($ExpectedStatus) {
            Write-Host "   ⚠️ Status mismatch: Expected $ExpectedStatus, got $($response.status)" -ForegroundColor Yellow
        }
        
        return $response
        
    } catch {
        Write-Host "   ❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "   Error Response: $responseBody" -ForegroundColor Red
        }
        return $null
    }
}

# Test 1: Create ticket CIVIL SELF_TEST with CONFIRMED status via /tickets
$test1Body = @{
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
}

$test1Result = Test-Endpoint -Name "Create CIVIL SELF_TEST with CONFIRMED via /tickets" `
    -Url "$baseUrl/tickets" -Method "POST" -Body $test1Body -ExpectedStatus "CONFIRMED"

# Test 2: Create ticket CIVIL SELF_TEST with CONFIRMED status via /after-payment
$test2Body = @{
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
}

$test2Result = Test-Endpoint -Name "Create CIVIL SELF_TEST with CONFIRMED via /after-payment" `
    -Url "$baseUrl/tickets/after-payment" -Method "POST" -Body $test2Body -ExpectedStatus "CONFIRMED"

# Test 3: Create ticket CIVIL AT_FACILITY with PENDING status (default)
$test3Body = @{
    type = "CIVIL"
    method = "AT_FACILITY"
    amount = 500000
    reason = "DNA testing at facility"
    address = "123 Test Street"
    phone = "0123456789"
    email = "test@example.com"
    sample1Name = "Sample A"
    sample2Name = "Sample B"
    appointmentDate = "2024-01-15"
}

$test3Result = Test-Endpoint -Name "Create CIVIL AT_FACILITY with default status" `
    -Url "$baseUrl/tickets/after-payment" -Method "POST" -Body $test3Body -ExpectedStatus "PENDING"

# Test 4: Create ticket ADMINISTRATIVE with PENDING status
$test4Body = @{
    type = "ADMINISTRATIVE"
    method = "AT_FACILITY"
    amount = 500000
    reason = "Administrative DNA testing"
    address = "123 Test Street"
    phone = "0123456789"
    email = "test@example.com"
    sample1Name = "Sample A"
    sample2Name = "Sample B"
    appointmentDate = "2024-01-15"
}

$test4Result = Test-Endpoint -Name "Create ADMINISTRATIVE with default status" `
    -Url "$baseUrl/tickets/after-payment" -Method "POST" -Body $test4Body -ExpectedStatus "PENDING"

# Test 5: Test invalid data (missing required fields)
$test5Body = @{
    type = "CIVIL"
    # Missing method, amount, etc.
}

$test5Result = Test-Endpoint -Name "Test invalid data (missing fields)" `
    -Url "$baseUrl/tickets/after-payment" -Method "POST" -Body $test5Body

# Test 6: Test invalid status for non-CIVIL SELF_TEST
$test6Body = @{
    type = "ADMINISTRATIVE"
    method = "AT_FACILITY"
    status = "CONFIRMED"  # Should be rejected or defaulted to PENDING
    amount = 500000
    reason = "Test invalid status"
    address = "123 Test Street"
    phone = "0123456789"
    email = "test@example.com"
    sample1Name = "Sample A"
    sample2Name = "Sample B"
}

$test6Result = Test-Endpoint -Name "Test invalid status for ADMINISTRATIVE" `
    -Url "$baseUrl/tickets/after-payment" -Method "POST" -Body $test6Body

# Test 7: Get ticket by ID (if created successfully)
if ($test1Result -and $test1Result.id) {
    $test7Result = Test-Endpoint -Name "Get ticket by ID" `
        -Url "$baseUrl/tickets/$($test1Result.id)" -Method "GET"
}

# Test 8: Get tickets by status
$test8Result = Test-Endpoint -Name "Get tickets by status CONFIRMED" `
    -Url "$baseUrl/tickets/status/CONFIRMED" -Method "GET"

$test9Result = Test-Endpoint -Name "Get tickets by status PENDING" `
    -Url "$baseUrl/tickets/status/PENDING" -Method "GET"

# Summary
Write-Host "`n📊 Test Summary" -ForegroundColor Green
Write-Host "==============" -ForegroundColor Green

$tests = @(
    @{Name="CIVIL SELF_TEST CONFIRMED via /tickets"; Result=$test1Result; Expected="CONFIRMED"},
    @{Name="CIVIL SELF_TEST CONFIRMED via /after-payment"; Result=$test2Result; Expected="CONFIRMED"},
    @{Name="CIVIL AT_FACILITY default"; Result=$test3Result; Expected="PENDING"},
    @{Name="ADMINISTRATIVE default"; Result=$test4Result; Expected="PENDING"},
    @{Name="Invalid data"; Result=$test5Result; Expected="Error"},
    @{Name="Invalid status"; Result=$test6Result; Expected="Error or PENDING"}
)

foreach ($test in $tests) {
    if ($test.Result) {
        $status = $test.Result.status
        if ($status -eq $test.Expected) {
            Write-Host "   ✅ $($test.Name): $status" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️ $($test.Name): Expected $($test.Expected), got $status" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   ❌ $($test.Name): Failed" -ForegroundColor Red
    }
}

Write-Host "`n🎯 Expected Results:" -ForegroundColor Cyan
Write-Host "   - CIVIL SELF_TEST should have status CONFIRMED" -ForegroundColor White
Write-Host "   - Other ticket types should have status PENDING" -ForegroundColor White
Write-Host "   - Invalid data should return error" -ForegroundColor White

Write-Host "`n🔍 Check backend logs for debug information:" -ForegroundColor Yellow
Write-Host "   - Look for '🔍 DEBUG:' messages" -ForegroundColor Gray
Write-Host "   - Check 'Final ticket status:' messages" -ForegroundColor Gray
Write-Host "   - Verify 'Using status from request:' messages" -ForegroundColor Gray 