-- Tạo bảng voucher
CREATE TABLE voucher (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    type NVARCHAR(20) NOT NULL, -- 'percent' hoặc 'amount'
    value DECIMAL(18,2) NOT NULL,
    start DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    max_usage INT NULL,
    used_count INT DEFAULT 0,
    status NVARCHAR(20) DEFAULT 'active'
);

-- Cập nhật bảng tickets
ALTER TABLE tickets ADD voucher_id INT NULL;
ALTER TABLE tickets ADD discount_amount DECIMAL(18,2) NULL;
ALTER TABLE tickets ADD final_amount DECIMAL(18,2) NULL;

-- Thêm khóa ngoại
ALTER TABLE tickets ADD CONSTRAINT FK_ticket_voucher FOREIGN KEY (voucher_id) REFERENCES voucher(id); 