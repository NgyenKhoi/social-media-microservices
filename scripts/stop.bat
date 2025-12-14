@echo off
REM Stop infrastructure

docker-compose down --remove-orphans

echo Infrastructure stopped.
echo To remove data volumes: docker-compose down -v