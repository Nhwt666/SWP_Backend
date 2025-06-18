package com.group2.ADN.dto;

public class MomoPaymentResponse {
    private int resultCode;
    private String message;
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
    private String requestId;
    private String orderId;

    // Getters and Setters
    public int getResultCode() { return resultCode; }
    public void setResultCode(int resultCode) { this.resultCode = resultCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
    public String getDeeplink() { return deeplink; }
    public void setDeeplink(String deeplink) { this.deeplink = deeplink; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
} 