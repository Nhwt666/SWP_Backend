# Test script sau khi fix backend
# Kiểm tra 403 Forbidden và 500 Internal Server Error đã được fix

$baseUrl = "http://localhost:8080"

Write-Host "🧪 Testing Backend Fixes" -ForegroundColor Green
Write-Host "=========================" -ForegroundColor Green

# Function to test endpoint
function Test-Endpoint {
    param(
        [string]$TestName,
        [string]$Endpoint,
        [object]$RequestBody,
        [string]$AuthToken,
        [string]$ExpectedStatus,
        [string]$ExpectedResponse
    )
    
    Write-Host "`n📋 $TestName" -ForegroundColor Yellow
    
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    if ($AuthToken) {
        $headers["Authorization"] = "Bearer $AuthToken"
    }
    
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl$Endpoint" -Method POST -Headers $headers -Body ($RequestBody | ConvertTo-Json) -ErrorAction Stop
        
        Write-Host "   ✅ Success" -ForegroundColor Green
        Write-Host "   Status Code: $($response.StatusCode)" -ForegroundColor Cyan
        
        # Parse response
        $responseBody = $response.Content | ConvertFrom-Json
        Write-Host "   Response Status: $($responseBody.status)" -ForegroundColor Cyan
        Write-Host "   Response Type: $($responseBody.type)" -ForegroundColor Cyan
        Write-Host "   Response Method: $($responseBody.method)" -ForegroundColor Cyan
        
        if ($responseBody.status -eq $ExpectedStatus) {
            Write-Host "   ✅ Status matches expected: $ExpectedStatus" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️ Status mismatch. Expected: $ExpectedStatus, Got: $($responseBody.status)" -ForegroundColor Yellow
        }
        
        return $true
    }
    catch {
        $errorResponse = $_.Exception.Response
        if ($errorResponse) {
            $statusCode = [int]$errorResponse.StatusCode
            $errorBody = $_.ErrorDetails.Message
            
            Write-Host "   ❌ Error: $statusCode" -ForegroundColor Red
            Write-Host "   Message: $errorBody" -ForegroundColor Red
            
            if ($ExpectedResponse -and $errorBody -like "*$ExpectedResponse*") {
                Write-Host "   ✅ Error message matches expected" -ForegroundColor Green
                return $true
            }
        } else {
            Write-Host "   ❌ Unexpected error: $($_.Exception.Message)" -ForegroundColor Red
        }
        return $false
    }
}

# Test 1: CIVIL SELF_TEST should be CONFIRMED
Write-Host "`n1️⃣ Testing CIVIL SELF_TEST → CONFIRMED..." -ForegroundColor Cyan
$civilSelfTestBody = @{
    type = "CIVIL"
    method = "SELF_TEST"
    customerId = 1
    amount = 500000
    reason = "DNA testing for civil case"
    address = "123 Test Street"
    phone = "0123456789"
    email = "test@example.com"
    sample1Name = "Sample A"
    sample2Name = "Sample B"
}

# Note: Replace with actual valid token
$validToken = "YOUR_VALID_JWT_TOKEN_HERE"

Test-Endpoint -TestName "CIVIL SELF_TEST via /tickets" -Endpoint "/tickets" -RequestBody $civilSelfTestBody -AuthToken $validToken -ExpectedStatus "CONFIRMED"
Test-Endpoint -TestName "CIVIL SELF_TEST via /after-payment" -Endpoint "/tickets/after-payment" -RequestBody $civilSelfTestBody -AuthToken $validToken -ExpectedStatus "CONFIRMED"

# Test 2: Other ticket types should be PENDING
Write-Host "`n2️⃣ Testing other ticket types → PENDING..." -ForegroundColor Cyan

$civilAtFacilityBody = @{
    type = "CIVIL"
    method = "AT_FACILITY"
    customerId = 1
    amount = 600000
    reason = "DNA testing at facility"
    address = "456 Facility Street"
    phone = "0987654321"
    email = "facility@example.com"
    appointmentDate = "2024-01-15"
}

$administrativeBody = @{
    type = "ADMINISTRATIVE"
    method = "AT_FACILITY"
    customerId = 1
    amount = 400000
    reason = "Administrative DNA testing"
    address = "789 Admin Street"
    phone = "0555666777"
    email = "admin@example.com"
    appointmentDate = "2024-01-20"
}

Test-Endpoint -TestName "CIVIL AT_FACILITY" -Endpoint "/tickets" -RequestBody $civilAtFacilityBody -AuthToken $validToken -ExpectedStatus "PENDING"
Test-Endpoint -TestName "ADMINISTRATIVE" -Endpoint "/tickets" -RequestBody $administrativeBody -AuthToken $validToken -ExpectedStatus "PENDING"

# Test 3: Authentication errors (should return 401/403 with clear messages)
Write-Host "`n3️⃣ Testing authentication errors..." -ForegroundColor Cyan

Test-Endpoint -TestName "No token" -Endpoint "/tickets" -RequestBody $civilSelfTestBody -AuthToken $null -ExpectedResponse "Access denied"
Test-Endpoint -TestName "Invalid token" -Endpoint "/tickets" -RequestBody $civilSelfTestBody -AuthToken "invalid.token.here" -ExpectedResponse "Access denied"

# Test 4: Validation errors (should return 400 with clear messages)
Write-Host "`n4️⃣ Testing validation errors..." -ForegroundColor Cyan

$invalidAmountBody = @{
    type = "CIVIL"
    method = "SELF_TEST"
    customerId = 1
    amount = 50000  # Too low
    reason = "DNA testing"
}

$missingAmountBody = @{
    type = "CIVIL"
    method = "SELF_TEST"
    customerId = 1
    reason = "DNA testing"
}

Test-Endpoint -TestName "Invalid amount" -Endpoint "/tickets/after-payment" -RequestBody $invalidAmountBody -AuthToken $validToken -ExpectedResponse "Amount must be between"
Test-Endpoint -TestName "Missing amount" -Endpoint "/tickets/after-payment" -RequestBody $missingAmountBody -AuthToken $validToken -ExpectedResponse "Amount is required"

Write-Host "`n🎉 Testing completed!" -ForegroundColor Green
Write-Host "Check backend console for debug logs:" -ForegroundColor Yellow
Write-Host "   - Look for '🔍 DEBUG:' messages" -ForegroundColor Gray
Write-Host "   - Check '🎯 Final ticket status:' messages" -ForegroundColor Gray
Write-Host "   - Verify user role and permissions" -ForegroundColor Gray

Write-Host "`n📊 Expected Results Summary:" -ForegroundColor Cyan
Write-Host "   ✅ CIVIL SELF_TEST → CONFIRMED status" -ForegroundColor Green
Write-Host "   ✅ Other ticket types → PENDING status" -ForegroundColor Green
Write-Host "   ✅ 403 Forbidden → Fixed (clear error messages)" -ForegroundColor Green
Write-Host "   ✅ 500 Internal Server Error → Fixed (constraint updated)" -ForegroundColor Green
Write-Host "   ✅ Debug logging → Enhanced" -ForegroundColor Green 