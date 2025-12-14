# Social Media Microservices Setup

Hệ thống microservices đơn giản cho social media platform với API Gateway và Authentication Service.

## Kiến trúc

```
Frontend (React) → API Gateway → Auth Service
                                     ↓
                              PostgreSQL + Redis
                                     ↑
                    Config Server ← Discovery Server
```

## Services

**Hiện tại chỉ có infrastructure:**
- **PostgreSQL** (5432): Database chính
- **Redis** (6379): Cache và session storage

**Sẽ thêm sau:**
- **API Gateway** (8080): Điểm vào chính, routing requests
- **Auth Service** (8081): Xác thực JWT, OAuth2, quản lý user
- **Config Server** (8888): Quản lý cấu hình tập trung
- **Discovery Server** (8761): Service registry (Eureka)

## Quick Start

### 1. Khởi động services

```cmd
scripts\start-dev.bat
```

### 2. Kiểm tra infrastructure

- PostgreSQL: localhost:5432 (auth_user/auth_password)
- Redis: localhost:6379

### 3. Dừng services

```cmd
scripts\stop.bat
```

Hoặc dùng trực tiếp:
```cmd
docker-compose down
```

## Development

### Xem logs
```cmd
docker-compose logs -f [service-name]
```

### Restart service
```cmd
docker-compose restart [service-name]
```

### Database access
- Host: localhost:5432
- Database: social_media_auth
- User: auth_user
- Password: auth_password

### Redis access
- Host: localhost:6379
- No password

## Configuration

Cấu hình chính trong `.env`:
- Database credentials
- JWT settings
- OAuth2 settings
- CORS origins

## Next Steps

1. Tạo Config Server project
2. Tạo Discovery Server project  
3. Cập nhật Auth Service
4. Cập nhật API Gateway
5. Tạo Frontend React app

## Troubleshooting

**Services không start:**
- Kiểm tra Docker đang chạy
- Kiểm tra ports không bị conflict
- Xem logs: `docker-compose logs [service-name]`

**Database connection issues:**
- Đợi PostgreSQL khởi động hoàn toàn
- Kiểm tra credentials trong .env

**PostgreSQL version incompatible:**
- Nếu gặp lỗi "database files are incompatible with server"
- Chạy: `docker-compose down -v` để xóa volumes cũ
- Sau đó start lại: `scripts\start-dev.bat`

**Service discovery issues:**
- Đảm bảo Discovery Server đã sẵn sàng
- Kiểm tra network connectivity giữa containers