# Pi_aetherium

## Backend start (port 8089)

Use this from the project root to avoid the recurring "Port 8089 already in use" error:

- Start only if not already running:
  - `powershell -ExecutionPolicy Bypass -File .\start-backend.ps1`
- Force restart backend on the same port:
  - `powershell -ExecutionPolicy Bypass -File .\start-backend.ps1 -Restart`

### One-click launchers (Windows)

- Double-click `start-backend.cmd` to start backend safely (won't start a duplicate server).
- Double-click `restart-backend.cmd` to force restart backend on the same port.