# Test Admin Reviews Endpoint
Write-Host "Testing Admin Reviews Endpoint..." -ForegroundColor Green

# Test without authentication (should return 403)
Write-Host "`n1. Testing without authentication (should return 403):" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/admin/reviews" -Method GET
    Write-Host "Response: $($response.Content)" -ForegroundColor Green
} catch {
    Write-Host "Expected 403 Forbidden: $($_.Exception.Message)" -ForegroundColor Red
}

# Test with admin authentication
Write-Host "`n2. Testing with admin authentication:" -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTczMDAwMDAwMCwiZXhwIjoxNzMwMDg2NDAwfQ.example"
    "Content-Type" = "application/json"
}

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/admin/reviews" -Method GET -Headers $headers
    Write-Host "Response Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response Content: $($response.Content)" -ForegroundColor Green
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nTest completed!" -ForegroundColor Green 