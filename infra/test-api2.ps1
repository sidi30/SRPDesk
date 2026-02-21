[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

Write-Host "=== Getting Keycloak token ===" -ForegroundColor Cyan
$tokenResponse = Invoke-RestMethod -Method Post -Uri "http://localhost:8180/realms/lexsecura/protocol/openid-connect/token" -Body @{
    client_id = "frontend"
    grant_type = "password"
    username = "admin@lexsecura.com"
    password = "admin123"
    scope = "openid"
}
$token = $tokenResponse.access_token
Write-Host "Token obtained" -ForegroundColor Green

$headers = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }
$headersNoBody = @{ Authorization = "Bearer $token" }

# Use frontend proxy (port 3000 -> nginx -> backend:8080)
$base = "http://localhost:3000"

Write-Host ""
Write-Host "=== 1. GET /api/v1/products ===" -ForegroundColor Cyan
try {
    $r = Invoke-RestMethod -Uri "$base/api/v1/products" -Headers $headersNoBody
    Write-Host "OK (200) - $($r.Count) products" -ForegroundColor Green
    $r | ForEach-Object { Write-Host "  - $($_.name) [$($_.type)] [$($_.criticality)]" }
} catch {
    Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 2. POST /api/v1/products ===" -ForegroundColor Cyan
try {
    $body = '{"name":"Test CRA Product","type":"CLASS_I","criticality":"HIGH"}'
    $np = Invoke-RestMethod -Method Post -Uri "$base/api/v1/products" -Headers $headers -Body $body
    Write-Host "OK (201) - Created: $($np.name) (id: $($np.id))" -ForegroundColor Green
    $prodId = $np.id
} catch {
    Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
    $prodId = $null
}

if ($prodId) {
    Write-Host ""
    Write-Host "=== 3. GET /api/v1/products/$prodId ===" -ForegroundColor Cyan
    try {
        $p = Invoke-RestMethod -Uri "$base/api/v1/products/$prodId" -Headers $headersNoBody
        Write-Host "OK (200) - $($p.name), type=$($p.type)" -ForegroundColor Green
    } catch {
        Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }

    Write-Host ""
    Write-Host "=== 4. PUT /api/v1/products/$prodId ===" -ForegroundColor Cyan
    try {
        $ub = '{"name":"Updated CRA Product","type":"CLASS_II","criticality":"CRITICAL"}'
        $up = Invoke-RestMethod -Method Put -Uri "$base/api/v1/products/$prodId" -Headers $headers -Body $ub
        Write-Host "OK (200) - Updated: $($up.name), type=$($up.type), criticality=$($up.criticality)" -ForegroundColor Green
    } catch {
        Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }

    Write-Host ""
    Write-Host "=== 5. POST /api/v1/products/$prodId/releases ===" -ForegroundColor Cyan
    try {
        $rb = '{"version":"1.0.0","gitRef":"v1.0.0"}'
        $rel = Invoke-RestMethod -Method Post -Uri "$base/api/v1/products/$prodId/releases" -Headers $headers -Body $rb
        Write-Host "OK (201) - Release v$($rel.version) (id: $($rel.id))" -ForegroundColor Green
        $rid = $rel.id
    } catch {
        Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
        $rid = $null
    }

    Write-Host ""
    Write-Host "=== 6. GET /api/v1/products/$prodId/releases ===" -ForegroundColor Cyan
    try {
        $rels = Invoke-RestMethod -Uri "$base/api/v1/products/$prodId/releases" -Headers $headersNoBody
        Write-Host "OK (200) - $($rels.Count) release(s)" -ForegroundColor Green
    } catch {
        Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }

    if ($rid) {
        Write-Host ""
        Write-Host "=== 7. GET /api/v1/releases/$rid/evidences ===" -ForegroundColor Cyan
        try {
            $evs = Invoke-RestMethod -Uri "$base/api/v1/releases/$rid/evidences" -Headers $headersNoBody
            Write-Host "OK (200) - $($evs.Count) evidence(s)" -ForegroundColor Green
        } catch {
            Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        }

        Write-Host ""
        Write-Host "=== 8. GET /api/v1/releases/$rid/components ===" -ForegroundColor Cyan
        try {
            $comps = Invoke-RestMethod -Uri "$base/api/v1/releases/$rid/components" -Headers $headersNoBody
            Write-Host "OK (200) - $($comps.Count) component(s)" -ForegroundColor Green
        } catch {
            Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        }

        Write-Host ""
        Write-Host "=== 9. GET /api/v1/releases/$rid/findings ===" -ForegroundColor Cyan
        try {
            $fds = Invoke-RestMethod -Uri "$base/api/v1/releases/$rid/findings" -Headers $headersNoBody
            Write-Host "OK (200) - $($fds.Count) finding(s)" -ForegroundColor Green
        } catch {
            Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        }
    }

    Write-Host ""
    Write-Host "=== 10. GET /api/v1/products/$prodId/findings ===" -ForegroundColor Cyan
    try {
        $pf = Invoke-RestMethod -Uri "$base/api/v1/products/$prodId/findings" -Headers $headersNoBody
        Write-Host "OK (200) - $($pf.Count) finding(s)" -ForegroundColor Green
    } catch {
        Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== 11. GET /api/v1/audit/verify ===" -ForegroundColor Cyan
try {
    $av = Invoke-RestMethod -Uri "$base/api/v1/audit/verify" -Headers $headersNoBody
    Write-Host "OK (200) - valid=$($av.valid), totalEvents=$($av.totalEvents), verified=$($av.verifiedEvents)" -ForegroundColor Green
    Write-Host "  $($av.message)"
} catch {
    Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 12. GET /api/v1/audit/events ===" -ForegroundColor Cyan
try {
    $ae = Invoke-RestMethod -Uri "$base/api/v1/audit/events" -Headers $headersNoBody
    Write-Host "OK (200) - $($ae.Count) event(s)" -ForegroundColor Green
} catch {
    Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 13. GET /api/v1/me ===" -ForegroundColor Cyan
try {
    $me = Invoke-RestMethod -Uri "$base/api/v1/me" -Headers $headersNoBody
    Write-Host "OK (200) - email=$($me.email), org=$($me.currentOrgId)" -ForegroundColor Green
    Write-Host "  roles: $($me.roles -join ', ')"
} catch {
    Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 14. GET /api/v1/orgs ===" -ForegroundColor Cyan
try {
    $orgs = Invoke-RestMethod -Uri "$base/api/v1/orgs" -Headers $headersNoBody
    Write-Host "OK (200) - $($orgs.Count) org(s)" -ForegroundColor Green
} catch {
    Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
}

# Cleanup
if ($prodId) {
    Write-Host ""
    Write-Host "=== 15. DELETE /api/v1/products/$prodId (cleanup) ===" -ForegroundColor Cyan
    try {
        Invoke-RestMethod -Method Delete -Uri "$base/api/v1/products/$prodId" -Headers $headersNoBody
        Write-Host "OK (204) - Cleaned up" -ForegroundColor Green
    } catch {
        Write-Host "FAIL - $($_.Exception.Response.StatusCode.value__): $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  ALL TESTS COMPLETE" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
