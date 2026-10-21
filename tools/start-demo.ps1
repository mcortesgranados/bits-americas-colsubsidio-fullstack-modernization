# Levanta la demo completa en tres ventanas: API, remoto y shell.
# Uso:  powershell -ExecutionPolicy Bypass -File tools\start-demo.ps1
# Requiere que la base colsubsidio_demo exista (tools\apply_local_db.ps1) y que el jar esté compilado.

param(
    [string]$Database = "colsubsidio_demo",
    [string]$DbUser = "colsubsidio",
    [string]$DbPassword = "colsubsidio",
    [int]$ApiPort = 8080
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

$jar = Join-Path $root "backend\target\colsubsidio-platform-api.jar"
if (-not (Test-Path $jar)) {
    throw "No existe $jar. Compílalo con: mvn -f backend/pom.xml -DskipTests package"
}

$dist = Join-Path $root "frontend\dist\mfe-affiliates"
if (-not (Test-Path $dist)) {
    throw "No existe $dist. Compílalo con: cd frontend; npm run build:lib; npm run build:mfe-affiliates"
}

# Las variables SPRING_DATASOURCE_* de la máquina pueden apuntar a otra base; se fuerzan aquí.
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://127.0.0.1:3306/$Database`?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8"
$env:SPRING_DATASOURCE_USERNAME = $DbUser
$env:SPRING_DATASOURCE_PASSWORD = $DbPassword

Write-Host "Arrancando API en http://localhost:$ApiPort ..." -ForegroundColor Cyan
Start-Process -WindowStyle Hidden -FilePath "java" -ArgumentList @(
    "-jar", $jar, "--server.port=$ApiPort"
) -WorkingDirectory $root -RedirectStandardOutput "$root\backend\target\demo-api.log" -RedirectStandardError "$root\backend\target\demo-api.err"

Write-Host "Arrancando remoto en http://localhost:4201 ..." -ForegroundColor Cyan
Start-Process -WindowStyle Hidden -FilePath "python" -ArgumentList @(
    (Join-Path $PSScriptRoot "serve_static_cors.py"), "`"$dist`"", "4201"
) -WorkingDirectory $root

Write-Host "Arrancando shell en http://localhost:4200 ..." -ForegroundColor Cyan
Start-Process -WindowStyle Hidden -FilePath "cmd.exe" -ArgumentList @(
    "/c", "npx ng serve shell --port 4200"
) -WorkingDirectory (Join-Path $root "frontend")

Write-Host ""
Write-Host "Esperando a que la API responda..." -ForegroundColor Yellow
$up = $false
for ($i = 0; $i -lt 40; $i++) {
    Start-Sleep -Seconds 3
    try {
        $health = Invoke-RestMethod "http://localhost:$ApiPort/actuator/health" -TimeoutSec 3
        if ($health.status -eq "UP") { $up = $true; break }
    } catch { }
}

if (-not $up) {
    Write-Host "La API no respondió. Revisa backend\target\demo-api.log" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Demo lista:" -ForegroundColor Green
Write-Host "  Portal   http://localhost:4200"
Write-Host "  Remoto   http://localhost:4201/remoteEntry.js"
Write-Host "  API      http://localhost:$ApiPort/actuator/health"
Write-Host ""
Write-Host "Credenciales: afiliado.demo / Demo123!" -ForegroundColor Green
Write-Host "El shell tarda unos segundos más en compilar; si el portal no carga, espera y recarga."
