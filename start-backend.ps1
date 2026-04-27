param(
    [switch]$Restart
)

$ErrorActionPreference = "Stop"
$port = 8089
$backendDir = Join-Path $PSScriptRoot "spring\backend"

function Get-ListeningPids([int]$Port) {
    $listeners = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $listeners) {
        return @()
    }
    return $listeners | Select-Object -ExpandProperty OwningProcess -Unique
}

$pids = Get-ListeningPids -Port $port

if ($pids.Count -gt 0 -and -not $Restart) {
    Write-Host "Backend already running on port $port (PID(s): $($pids -join ', '))."
    Write-Host "Use -Restart if you want to stop it and start a fresh instance."
    exit 0
}

if ($pids.Count -gt 0 -and $Restart) {
    foreach ($processId in $pids) {
        Write-Host "Stopping process on port ${port}: PID $processId"
        taskkill /PID $processId /F | Out-Null
    }
    Start-Sleep -Seconds 1
}

if (-not (Test-Path (Join-Path $backendDir "mvnw.cmd"))) {
    throw "Cannot find Maven wrapper in $backendDir"
}

Set-Location $backendDir
Write-Host "Starting backend on port $port from $backendDir"
& ".\mvnw.cmd" "spring-boot:run"
