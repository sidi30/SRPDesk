#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "=== LexSecura Local Setup ==="
echo ""

# Check prerequisites
echo "[1/5] Checking prerequisites..."
command -v docker >/dev/null 2>&1 || { echo "ERROR: docker is required but not installed."; exit 1; }
command -v java >/dev/null 2>&1 || { echo "ERROR: java is required but not installed."; exit 1; }
command -v node >/dev/null 2>&1 || { echo "ERROR: node is required but not installed."; exit 1; }

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "ERROR: Java 21+ required, found Java $JAVA_VERSION"
    exit 1
fi

echo "  docker: OK"
echo "  java $JAVA_VERSION: OK"
echo "  node $(node --version): OK"
echo ""

# Install frontend deps
echo "[2/5] Installing frontend dependencies..."
cd "$PROJECT_ROOT/frontend" && npm install --silent
echo "  Done."
echo ""

# Start infrastructure
echo "[3/5] Starting infrastructure (postgres, keycloak, minio)..."
cd "$PROJECT_ROOT/infra" && docker compose up -d postgres keycloak minio minio-init
echo "  Done."
echo ""

# Wait for services
echo "[4/5] Waiting for services to be healthy..."
echo -n "  Postgres: "
until docker exec lexsecura-postgres pg_isready -U lexsecura > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo " OK"

echo -n "  MinIO: "
until curl -sf http://localhost:9000/minio/health/live > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo " OK"

echo -n "  Keycloak: "
for i in $(seq 1 60); do
    if curl -sf http://localhost:8180/health/ready > /dev/null 2>&1; then
        echo " OK"
        break
    fi
    echo -n "."
    sleep 3
done
echo ""

# Summary
echo "[5/5] Setup complete!"
echo ""
echo "=== Services ==="
echo "  PostgreSQL:     localhost:5432 (lexsecura/lexsecura)"
echo "  Keycloak:       http://localhost:8180 (admin/admin)"
echo "  MinIO:          http://localhost:9000 (minioadmin/minioadmin)"
echo "  MinIO Console:  http://localhost:9001"
echo ""
echo "=== Next Steps ==="
echo "  Terminal 1: cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local"
echo "  Terminal 2: cd frontend && npm run dev"
echo ""
echo "  Backend API:    http://localhost:8080"
echo "  Swagger UI:     http://localhost:8080/swagger-ui.html"
echo "  Frontend:       http://localhost:5173"
echo ""
echo "  Keycloak admin user: admin / admin"
echo "  Realm: lexsecura"
echo "  Test user: see realm-export.json for pre-configured users"
