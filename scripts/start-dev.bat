@echo off
REM Start infrastructure only (PostgreSQL + Redis)

REM Check Docker
docker info >nul 2>&1
if errorlevel 1 (
    echo Docker is not running. Please start Docker first.
    exit /b 1
)

REM Clean up
docker-compose down --remove-orphans

REM Start infrastructure
docker-compose up -d

REM Show status
echo Infrastructure started:
echo - PostgreSQL: localhost:5432 (auth_user/auth_password)
echo - Redis: localhost:6379

docker-compose ps