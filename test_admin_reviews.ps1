# Test Admin Reviews Endpoint
Write-Host "=== Testing Admin Reviews Endpoint ===" -ForegroundColor Cyan

# Test 1: Without authentication (should return 403)
Write-Host "`n1. Testing without authentication (should return 403):" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/admin/reviews" -Method GET
    Write-Host "Unexpected success: $($response.StatusCode)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "✅ Expected 403 Forbidden - Authentication required" -ForegroundColor Green
    } else {
        Write-Host "❌ Unexpected error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 2: With admin authentication
Write-Host "`n2. Testing with admin authentication:" -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTczMDAwMDAwMCwiZXhwIjoxNzMwMDg2NDAwfQ.example"
    "Content-Type" = "application/json"
}

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/admin/reviews" -Method GET -Headers $headers
    Write-Host "✅ Response Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response Content:" -ForegroundColor Green
    Write-Host $response.Content -ForegroundColor White
} catch {
    Write-Host "❌ Error with authentication: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

# Test 3: Test tickets-with-feedback endpoint
Write-Host "`n3. Testing /admin/tickets-with-feedback endpoint:" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/admin/tickets-with-feedback" -Method GET -Headers $headers
    Write-Host "✅ Response Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response Content:" -ForegroundColor Green
    Write-Host $response.Content -ForegroundColor White
} catch {
    Write-Host "❌ Error with tickets-with-feedback: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test completed! ===" -ForegroundColor Cyan 