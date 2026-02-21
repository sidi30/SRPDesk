$ErrorActionPreference = "Stop"

Write-Host "=== Getting Keycloak token ===" -ForegroundColor Cyan
$tokenResponse = Invoke-RestMethod -Method Post -Uri "http://localhost:8180/realms/lexsecura/protocol/openid-connect/token" -Body @{
    client_id = "frontend"
    grant_type = "password"
    username = "admin@lexsecura.com"
    password = "admin123"
    scope = "openid"
}
$token = $tokenResponse.access_token
Write-Host "Token obtained successfully" -ForegroundColor Green

$headers = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }
$headersNoBody = @{ Authorization = "Bearer $token" }

Write-Host ""
Write-Host "=== 1. GET /api/v1/products ===" -ForegroundColor Cyan
try {
    $products = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/products" -Headers $headersNoBody
    Write-Host "OK - $($products.Count) products found" -ForegroundColor Green
    $products | ForEach-Object { Write-Host "  - $($_.name) [$($_.type)] [$($_.criticality)]" }
} catch {
    Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 2. POST /api/v1/products ===" -ForegroundColor Cyan
try {
    $body = '{"name":"Test CRA Product","type":"CLASS_I","criticality":"HIGH"}'
    $newProduct = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/products" -Headers $headers -Body $body
    Write-Host "OK - Created product: $($newProduct.name) (id: $($newProduct.id))" -ForegroundColor Green
    $productId = $newProduct.id
} catch {
    Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
    $productId = $null
}

if ($productId) {
    Write-Host ""
    Write-Host "=== 3. GET /api/v1/products/$productId ===" -ForegroundColor Cyan
    try {
        $p = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/products/$productId" -Headers $headersNoBody
        Write-Host "OK - $($p.name), type=$($p.type), criticality=$($p.criticality)" -ForegroundColor Green
    } catch {
        Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }

    Write-Host ""
    Write-Host "=== 4. POST /api/v1/products/$productId/releases ===" -ForegroundColor Cyan
    try {
        $releaseBody = '{"version":"1.0.0","gitRef":"v1.0.0"}'
        $release = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/products/$productId/releases" -Headers $headers -Body $releaseBody
        Write-Host "OK - Created release: v$($release.version) (id: $($release.id))" -ForegroundColor Green
        $releaseId = $release.id
    } catch {
        Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
        $releaseId = $null
    }

    Write-Host ""
    Write-Host "=== 5. GET /api/v1/products/$productId/releases ===" -ForegroundColor Cyan
    try {
        $releases = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/products/$productId/releases" -Headers $headersNoBody
        Write-Host "OK - $($releases.Count) release(s)" -ForegroundColor Green
    } catch {
        Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }

    Write-Host ""
    Write-Host "=== 6. GET /api/v1/products/$productId/findings ===" -ForegroundColor Cyan
    try {
        $findings = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/products/$productId/findings" -Headers $headersNoBody
        Write-Host "OK - $($findings.Count) finding(s)" -ForegroundColor Green
    } catch {
        Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== 7. GET /api/v1/audit/verify ===" -ForegroundColor Cyan
try {
    $audit = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/audit/verify" -Headers $headersNoBody
    Write-Host "OK - valid=$($audit.valid), totalEvents=$($audit.totalEvents), verified=$($audit.verifiedEvents)" -ForegroundColor Green
    Write-Host "  message: $($audit.message)"
} catch {
    Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 8. GET /api/v1/audit/events ===" -ForegroundColor Cyan
try {
    $events = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/audit/events" -Headers $headersNoBody
    Write-Host "OK - $($events.Count) audit event(s)" -ForegroundColor Green
} catch {
    Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 9. GET /api/v1/me ===" -ForegroundColor Cyan
try {
    $me = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/me" -Headers $headersNoBody
    Write-Host "OK - userId=$($me.userId), email=$($me.email), org=$($me.currentOrgId)" -ForegroundColor Green
    Write-Host "  roles: $($me.roles -join ', ')"
} catch {
    Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 10. GET /api/v1/orgs ===" -ForegroundColor Cyan
try {
    $orgs = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/orgs" -Headers $headersNoBody
    Write-Host "OK - $($orgs.Count) org(s)" -ForegroundColor Green
} catch {
    Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 11. Frontend (http://localhost:3000) ===" -ForegroundColor Cyan
try {
    $frontend = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing
    Write-Host "OK - HTTP $($frontend.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

if ($productId) {
    Write-Host ""
    Write-Host "=== 12. DELETE /api/v1/products/$productId (cleanup) ===" -ForegroundColor Cyan
    try {
        Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/v1/products/$productId" -Headers $headersNoBody
        Write-Host "OK - Product deleted" -ForegroundColor Green
    } catch {
        Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== DONE ===" -ForegroundColor Cyan
