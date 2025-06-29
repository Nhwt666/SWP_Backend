# Simple test to trigger backend logs
$baseUrl = "http://localhost:8080"

Write-Host "Testing backend endpoints..." -ForegroundColor Green

# Test 1: No authentication
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/tickets" -Method POST -ContentType "application/json" -Body '{"type":"CIVIL","method":"SELF_TEST","customerId":1}' -ErrorAction Stop
    Write-Host "Unexpected success: $($response.StatusCode)" -ForegroundColor Yellow
} catch {
    Write-Host "Expected error: $($_.Exception.Response.StatusCode)" -ForegroundColor Green
    Write-Host "Message: $($_.ErrorDetails.Message)" -ForegroundColor Cyan
}

# Test 2: Invalid token
try {
    $headers = @{"Authorization" = "Bearer invalid.token.here"}
    $response = Invoke-WebRequest -Uri "$baseUrl/tickets" -Method POST -Headers $headers -ContentType "application/json" -Body '{"type":"CIVIL","method":"SELF_TEST","customerId":1}' -ErrorAction Stop
    Write-Host "Unexpected success: $($response.StatusCode)" -ForegroundColor Yellow
} catch {
    Write-Host "Expected error: $($_.Exception.Response.StatusCode)" -ForegroundColor Green
    Write-Host "Message: $($_.ErrorDetails.Message)" -ForegroundColor Cyan
}

Write-Host "Tests completed. Check backend console for logs." -ForegroundColor Yellow 