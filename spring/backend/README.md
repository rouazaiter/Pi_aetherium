# Room Session Backend - Recording Upload

This backend now supports screen recording upload from the Angular room UI.

## API

### Upload recording
- Method: `POST`
- URL: `/api/recordings/upload`
- Content-Type: `multipart/form-data`
- Form fields:
  - `video` (file, required)
  - `sessionId` (number, required)

The uploaded file is saved in `recordings/` using the naming pattern:
- `{sessionId}.webm`

## Configuration

See `src/main/resources/application.properties`:
- `app.recordings.directory=./recordings`
- `spring.servlet.multipart.max-file-size=500MB`
- `spring.servlet.multipart.max-request-size=500MB`

## Quick manual test (PowerShell)

```powershell
$sessionId = 1
$file = "C:\path\to\sample.webm"
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/recordings/upload" -Form @{ video = Get-Item $file; sessionId = $sessionId }
```

## Angular flow

- `RecordingService` handles browser screen capture + MediaRecorder chunks.
- `RecordingUploadService` sends the WebM blob to backend endpoint above.
- The room UI toggles a `Start Recording` / `Stop Recording` button and shows errors.

