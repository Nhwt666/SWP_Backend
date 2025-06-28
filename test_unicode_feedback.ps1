# Test Unicode Feedback Support
Write-Host "=== Testing Unicode Feedback Support ===" -ForegroundColor Cyan

# Wait for backend to start
Write-Host "Waiting for backend to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Test admin reviews endpoint
Write-Host "`nTesting /admin/reviews endpoint:" -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTczMDAwMDAwMCwiZXhwIjoxNzMwMDg2NDAwfQ.example"
    "Content-Type" = "application/json"
}

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/admin/reviews" -Method GET -Headers $headers
    Write-Host "✅ Response Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response Content:" -ForegroundColor Green
    Write-Host $response.Content -ForegroundColor White
    
    # Check if Vietnamese characters are displayed correctly
    if ($response.Content -match "tiếng việt") {
        Write-Host "✅ Vietnamese characters displayed correctly!" -ForegroundColor Green
    } else {
        Write-Host "❌ Vietnamese characters still have encoding issues" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test completed! ===" -ForegroundColor Cyan 