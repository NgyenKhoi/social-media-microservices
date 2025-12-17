# Quick Database Setup

## 🚀 Quick Start

1. **Tạo database trong pgAdmin local:**
   ```sql
   CREATE DATABASE auth_db;
   ```

2. **Chạy schema script:**
   ```bash
   psql -U postgres -d auth_db -f auth-service/schema-auth-service.sql
   ```

3. **Test connection:**
   ```bash
   psql -U postgres -d auth_db -f scripts/test-db-connection.sql
   ```

4. **Start services:**
   ```bash
   docker-compose up -d
   ```

## 📋 Connection Details
- **Host:** localhost:5432
- **Database:** auth_db  
- **Username:** postgres
- **Password:** eqfleqrd1

## 📖 Detailed Guide
Xem chi tiết tại: `.kiro/specs/api-gateway-auth-system/database-setup-guide.md`