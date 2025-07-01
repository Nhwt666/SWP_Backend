# Script: scripts/check_notification_log.ps1
# Mục đích: Kiểm tra log backend để xem notification đã được tạo, xóa hoặc cleanup chưa

$logPath = "log/backend.log"

if (-Not (Test-Path $logPath)) {
    Write-Host "Không tìm thấy file log: $logPath"
    exit 1
}

Write-Host "==== Các log liên quan đến notification ===="
Select-String -Path $logPath -Pattern "notification" | ForEach-Object { $_.Line }

Write-Host "==== Các log tạo notification ===="
Select-String -Path $logPath -Pattern "Tạo notification" | ForEach-Object { $_.Line }

Write-Host "==== Các log xóa notification cũ ===="
Select-String -Path $logPath -Pattern "Xóa .*notification cũ" | ForEach-Object { $_.Line }

Write-Host "==== Các log cleanup notification hết hạn ===="
Select-String -Path $logPath -Pattern "cleanup notification hết hạn" | ForEach-Object { $_.Line } 