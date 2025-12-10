-- Enhanced schema.sql with RefreshToken table for JWT
DROP DATABASE IF EXISTS security_db;
CREATE DATABASE IF NOT EXISTS security_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE security_db;

-- Language table
CREATE TABLE Language(
  LanguageID CHAR(2) PRIMARY KEY,
  Language VARCHAR(20) NOT NULL
);

-- Product Category
CREATE TABLE ProductCategory(
  ProductCategoryID INT PRIMARY KEY AUTO_INCREMENT,
  CanBeShipped BIT NOT NULL DEFAULT b'1'
);

CREATE TABLE ProductCategoryTranslation(
  ProductCategoryID INT NOT NULL,
  LanguageID CHAR(2) NOT NULL,
  CategoryName VARCHAR(100) NOT NULL,
  PRIMARY KEY(ProductCategoryID, LanguageID),
  FOREIGN KEY (ProductCategoryID) REFERENCES ProductCategory(ProductCategoryID),
  FOREIGN KEY (LanguageID) REFERENCES Language(LanguageID)
);

-- Product
CREATE TABLE Product(
  ProductID INT PRIMARY KEY AUTO_INCREMENT,
  Price DECIMAL(10,2) NOT NULL,
  Weight DECIMAL(6,2),
  ProductCategoryID INT NOT NULL,
  StockQuantity INT DEFAULT 0,
  ImageUrl VARCHAR(500),  -- Increased from 255 to 500
  CreatedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
  UpdatedDate DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (ProductCategoryID) REFERENCES ProductCategory(ProductCategoryID)
);

CREATE TABLE ProductTranslation(
  ProductID INT NOT NULL,
  LanguageID CHAR(2) NOT NULL,
  ProductName VARCHAR(100) NOT NULL,
  ProductDescription VARCHAR(255),
  PRIMARY KEY(ProductID, LanguageID),
  FOREIGN KEY (ProductID) REFERENCES Product(ProductID),
  FOREIGN KEY (LanguageID) REFERENCES Language(LanguageID)
);

-- Role
CREATE TABLE Role(
  RoleID INT PRIMARY KEY,
  RoleName VARCHAR(50) NOT NULL UNIQUE,
  Description VARCHAR(255)
);

-- User
CREATE TABLE `User`(
  UserID INT PRIMARY KEY AUTO_INCREMENT,
  Username VARCHAR(50) NOT NULL UNIQUE,
  `Password` VARCHAR(255) NOT NULL,
  Email VARCHAR(100) NOT NULL UNIQUE,
  FullName VARCHAR(100),
  Enabled BIT NOT NULL DEFAULT b'1',
  CreatedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
  UpdatedDate DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User-Role (Many-to-Many)
CREATE TABLE UserRole(
  UserID INT NOT NULL,
  RoleID INT NOT NULL,
  PRIMARY KEY(UserID, RoleID),
  FOREIGN KEY (UserID) REFERENCES `User`(UserID) ON DELETE CASCADE,
  FOREIGN KEY (RoleID) REFERENCES Role(RoleID) ON DELETE CASCADE
);

-- RefreshToken Table (NEW for JWT)
CREATE TABLE RefreshToken(
  TokenID INT PRIMARY KEY AUTO_INCREMENT,
  Token VARCHAR(500) NOT NULL UNIQUE,
  UserID INT NOT NULL,
  ExpiryDate DATETIME NOT NULL,
  CreatedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (UserID) REFERENCES `User`(UserID) ON DELETE CASCADE,
  INDEX idx_token (Token),
  INDEX idx_user (UserID)
);

-- Sample Data
INSERT INTO Language (LanguageID, Language) VALUES
('vi', 'Tiếng Việt'),
('en', 'English');

INSERT INTO Role (RoleID, RoleName, Description) VALUES
(0, 'ROLE_ADMIN', 'Administrator with full access'),
(1, 'ROLE_MANAGER', 'Manager with limited admin access'),
(2, 'ROLE_USER', 'Regular user with read-only access');

-- Password: admin123 (BCrypt encoded)
-- Password for all: 123123 (BCrypt hash: $2a$10$wH6QwQwQwQwQwQwQwQwQwOQwQwQwQwQwQwQwQwQwQwQwQwQwQ)
INSERT INTO `User` (Username, `Password`, Email, FullName, Enabled) VALUES
('admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa5uG1bY3l6h1gC5X0i1A6rF1g6h1g6e', 'admin@example.com', 'Administrator', b'1'),
('manager', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa5uG1bY3l6h1gC5X0i1A6rF1g6h1g6e', 'manager@example.com', 'Manager User', b'1'),
('user', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa5uG1bY3l6h1gC5X0i1A6rF1g6h1g6e', 'user@example.com', 'Regular User', b'1');

-- UserID 1: admin, UserID 2: manager, UserID 3: user
INSERT INTO UserRole (UserID, RoleID) VALUES
(1, 0), -- admin has ROLE_ADMIN (RoleID 0)
(2, 1), -- manager has ROLE_MANAGER (RoleID 1)
(3, 2); -- user has ROLE_USER (RoleID 2)

-- Create product categories (explicitly rely on auto-increment order)
INSERT INTO ProductCategory (CanBeShipped) VALUES (b'1'), (b'1'), (b'0');

INSERT INTO ProductCategoryTranslation (ProductCategoryID, LanguageID, CategoryName) VALUES
(1, 'vi', 'Điện thoại'),
(1, 'en', 'Phones'),
(2, 'vi', 'Laptop'),
(2, 'en', 'Laptops'),
(3, 'vi', 'Dịch vụ'),
(3, 'en', 'Services');

-- Insert products (initial category ids are assumed to match the above translation records)
INSERT INTO Product (Price, Weight, ProductCategoryID, StockQuantity, ImageUrl) VALUES
(15000000, 0.20, 1, 50, 'https://via.placeholder.com/300x300?text=iPhone+15'),
(25000000, 1.50, 2, 30, 'https://via.placeholder.com/300x300?text=MacBook+Pro'),
(5000000, NULL, 3, 100, 'https://via.placeholder.com/300x300?text=Warranty');

INSERT INTO ProductTranslation (ProductID, LanguageID, ProductName, ProductDescription) VALUES
(1, 'vi', 'iPhone 15', 'Điện thoại thông minh cao cấp'),
(1, 'en', 'iPhone 15', 'Premium smartphone'),
(2, 'vi', 'MacBook Pro', 'Laptop chuyên nghiệp'),
(2, 'en', 'MacBook Pro', 'Professional laptop'),
(3, 'vi', 'Bảo hành mở rộng', 'Dịch vụ bảo hành 2 năm'),
(3, 'en', 'Extended Warranty', '2-year warranty service');

-- Ensure mapping: explicitly set product categories based on Vietnamese category translations
-- This avoids accidental mismatches if ProductCategory auto-increment IDs changed earlier

-- Set iPhone products to category 'Điện thoại'
UPDATE Product p
JOIN ProductTranslation pt ON p.ProductID = pt.ProductID AND pt.LanguageID = 'vi'
JOIN ProductCategoryTranslation pct ON pct.LanguageID = 'vi' AND pct.CategoryName = 'Điện thoại'
SET p.ProductCategoryID = pct.ProductCategoryID
WHERE pt.ProductName LIKE 'iPhone%';

-- Set MacBook products to category 'Laptop'
UPDATE Product p
JOIN ProductTranslation pt ON p.ProductID = pt.ProductID AND pt.LanguageID = 'vi'
JOIN ProductCategoryTranslation pct ON pct.LanguageID = 'vi' AND pct.CategoryName = 'Laptop'
SET p.ProductCategoryID = pct.ProductCategoryID
WHERE pt.ProductName LIKE 'MacBook%';

-- Set warranty / service products to category 'Dịch vụ'
UPDATE Product p
JOIN ProductTranslation pt ON p.ProductID = pt.ProductID AND pt.LanguageID = 'vi'
JOIN ProductCategoryTranslation pct ON pct.LanguageID = 'vi' AND pct.CategoryName = 'Dịch vụ'
SET p.ProductCategoryID = pct.ProductCategoryID
WHERE pt.ProductName LIKE '%Bảo hành%' OR pt.ProductName LIKE '%Warranty%';

-- Done

-- Update translations for Vietnamese and English (ensure correct demo data)
UPDATE ProductTranslation SET ProductName = 'iPhone 15', ProductDescription = 'Điện thoại thông minh cao cấp' WHERE ProductID = 1 AND LanguageID = 'vi';
UPDATE ProductTranslation SET ProductName = 'MacBook Pro', ProductDescription = 'Laptop chuyên nghiệp' WHERE ProductID = 2 AND LanguageID = 'vi';
UPDATE ProductTranslation SET ProductName = 'Bảo hành mở rộng', ProductDescription = 'Dịch vụ bảo hành 2 năm' WHERE ProductID = 3 AND LanguageID = 'vi';
UPDATE ProductCategoryTranslation SET CategoryName = 'Điện thoại' WHERE ProductCategoryID = 1 AND LanguageID = 'vi';
UPDATE ProductCategoryTranslation SET CategoryName = 'Laptop' WHERE ProductCategoryID = 2 AND LanguageID = 'vi';
UPDATE ProductCategoryTranslation SET CategoryName = 'Dịch vụ' WHERE ProductCategoryID = 3 AND LanguageID = 'vi';
UPDATE ProductTranslation SET ProductName = 'iPhone 15', ProductDescription = 'Premium smartphone' WHERE ProductID = 1 AND LanguageID = 'en';
UPDATE ProductTranslation SET ProductName = 'MacBook Pro', ProductDescription = 'Professional laptop' WHERE ProductID = 2 AND LanguageID = 'en';
UPDATE ProductTranslation SET ProductName = 'Extended Warranty', ProductDescription = '2-year warranty service' WHERE ProductID = 3 AND LanguageID = 'en';
UPDATE ProductCategoryTranslation SET CategoryName = 'Phones' WHERE ProductCategoryID = 1 AND LanguageID = 'en';
UPDATE ProductCategoryTranslation SET CategoryName = 'Laptops' WHERE ProductCategoryID = 2 AND LanguageID = 'en';
UPDATE ProductCategoryTranslation SET CategoryName = 'Services' WHERE ProductCategoryID = 3 AND LanguageID = 'en';
