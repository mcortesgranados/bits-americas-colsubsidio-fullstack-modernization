# Aplica las migraciones V1-V3 sobre un MySQL 8 local.
# Uso:  powershell -ExecutionPolicy Bypass -File tools\apply_local_db.ps1
# La contraseña se pide en pantalla y no se escribe en el historial ni en el repositorio.

param(
    [string]$RootUser = "root",
    [string]$Database = "colsubsidio_demo",
    [string]$AppUser = "colsubsidio",
    [string]$AppPassword = "colsubsidio",
    [string]$MySqlExe = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    [string]$MigrationDir = "backend\src\main\resources\db\migration"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $MySqlExe)) { throw "No se encontró el cliente MySQL en $MySqlExe" }
if (-not (Test-Path $MigrationDir)) { throw "No se encontró la carpeta de migraciones: $MigrationDir" }

$secure = Read-Host "Contraseña de '$RootUser' en MySQL local" -AsSecureString
$plain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure))
$env:MYSQL_PWD = $plain

function Invoke-MySql {
    param([string]$Sql)
    $out = & $MySqlExe --user=$RootUser --default-character-set=utf8mb4 --execute=$Sql 2>&1
    if ($LASTEXITCODE -ne 0) { throw "MySQL devolvió $LASTEXITCODE : $out" }
    return $out
}

try {
    Invoke-MySql "SELECT VERSION();" | Out-Null
    Write-Host "Conexión correcta." -ForegroundColor Green

    Invoke-MySql "CREATE DATABASE IF NOT EXISTS $Database CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
    Invoke-MySql "CREATE USER IF NOT EXISTS '$AppUser'@'localhost' IDENTIFIED BY '$AppPassword';"
    Invoke-MySql "GRANT ALL PRIVILEGES ON $Database.* TO '$AppUser'@'localhost'; FLUSH PRIVILEGES;"
    Write-Host "Base '$Database' y usuario '$AppUser' listos." -ForegroundColor Green

    $scripts = Get-ChildItem -Path $MigrationDir -Filter "V*.sql" | Sort-Object Name
    if (-not $scripts) { throw "No hay scripts V*.sql en $MigrationDir" }

    foreach ($script in $scripts) {
        $path = $script.FullName.Replace("\", "/")
        & $MySqlExe --user=$RootUser --default-character-set=utf8mb4 --database=$Database --execute="source $path" 2>&1 | Out-Null
        if ($LASTEXITCODE -ne 0) { throw "Falló $($script.Name)" }
        Write-Host "Aplicado: $($script.Name)" -ForegroundColor Green
    }

    $tables = Invoke-MySql "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$Database' AND table_type='BASE TABLE';"
    $requests = Invoke-MySql "SELECT COUNT(*) FROM $Database.subsidy_request;"
    Write-Host ""
    Write-Host "Tablas creadas: $($tables[-1])" -ForegroundColor Cyan
    Write-Host "Solicitudes sembradas: $($requests[-1])" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Siguiente paso: marcar el historial de Flyway en la versión 3 para no reintentar V1-V3." -ForegroundColor Yellow
    Write-Host "  mvn -f backend/pom.xml flyway:baseline -Dflyway.baselineVersion=3"
}
finally {
    Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
}
