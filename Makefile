.PHONY: up down logs test build clean help

INFRA_DIR=infra

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

up: ## Start all services (postgres, keycloak, minio)
	cd $(INFRA_DIR) && docker compose up -d postgres keycloak minio minio-init

up-all: ## Start all services including backend and frontend
	cd $(INFRA_DIR) && docker compose up -d --build

down: ## Stop all services
	cd $(INFRA_DIR) && docker compose down

down-volumes: ## Stop all services and remove volumes
	cd $(INFRA_DIR) && docker compose down -v

logs: ## Show logs for all services
	cd $(INFRA_DIR) && docker compose logs -f

logs-backend: ## Show backend logs
	cd $(INFRA_DIR) && docker compose logs -f backend

ps: ## Show running services
	cd $(INFRA_DIR) && docker compose ps

backend-run: ## Run backend locally (requires infra up)
	cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

backend-test: ## Run backend tests
	cd backend && ./mvnw test

backend-verify: ## Run backend tests + integration tests
	cd backend && ./mvnw verify

backend-compile: ## Compile backend
	cd backend && ./mvnw compile -q

frontend-dev: ## Run frontend dev server
	cd frontend && npm run dev

frontend-build: ## Build frontend
	cd frontend && npm run build

frontend-lint: ## Lint frontend
	cd frontend && npx tsc --noEmit

test: backend-test frontend-lint ## Run all tests

build: ## Build all Docker images
	docker build -t lexsecura-backend:latest ./backend
	docker build -t lexsecura-frontend:latest ./frontend

clean: ## Clean build artifacts
	cd backend && ./mvnw clean
	cd frontend && rm -rf dist node_modules/.cache

init: ## First-time setup (install deps, start infra)
	cd frontend && npm install
	$(MAKE) up
	@echo ""
	@echo "Infrastructure started. Wait for services to be healthy, then run:"
	@echo "  make backend-run   (in terminal 1)"
	@echo "  make frontend-dev  (in terminal 2)"
