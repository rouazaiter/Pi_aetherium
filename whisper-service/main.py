"""
Whisper Transcription Microservice
Uses faster-whisper (CTranslate2) — 100% free, runs locally.
Requires ffmpeg installed on the system.
"""

import os
import subprocess
import tempfile
import logging

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from faster_whisper import WhisperModel

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Whisper Transcription Service")

# Load model once at startup (use "base" for speed, "small" or "medium" for accuracy)
MODEL_SIZE = os.getenv("WHISPER_MODEL_SIZE", "base")
DEVICE = os.getenv("WHISPER_DEVICE", "cpu")  # "cuda" if you have a GPU
COMPUTE_TYPE = os.getenv("WHISPER_COMPUTE_TYPE", "int8")

logger.info(f"Loading faster-whisper model '{MODEL_SIZE}' on {DEVICE}...")
model = WhisperModel(MODEL_SIZE, device=DEVICE, compute_type=COMPUTE_TYPE)
logger.info("Model loaded successfully.")


class TranscribeRequest(BaseModel):
    file_path: str
    language: str | None = None  # auto-detect if None


class TranscribeResponse(BaseModel):
    text: str
    language: str
    duration: float


def extract_audio(video_path: str) -> str:
    """Extract audio from video using ffmpeg into a temp WAV file."""
    if not os.path.isfile(video_path):
        raise FileNotFoundError(f"File not found: {video_path}")

    tmp = tempfile.NamedTemporaryFile(suffix=".wav", delete=False)
    tmp.close()

    cmd = [
        "ffmpeg", "-y",
        "-i", video_path,
        "-vn",                  # no video
        "-acodec", "pcm_s16le", # 16-bit PCM
        "-ar", "16000",         # 16 kHz (whisper expects this)
        "-ac", "1",             # mono
        tmp.name,
    ]

    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=600)
        if result.returncode != 0:
            raise RuntimeError(f"ffmpeg error: {result.stderr[:500]}")
    except FileNotFoundError:
        raise RuntimeError("ffmpeg not found. Please install ffmpeg and add it to PATH.")

    return tmp.name


@app.post("/transcribe", response_model=TranscribeResponse)
def transcribe(req: TranscribeRequest):
    """Transcribe a video/audio file and return full text."""
    logger.info(f"Transcription request for: {req.file_path}")

    video_path = req.file_path

    # Determine if we need audio extraction (video files)
    video_extensions = {".mp4", ".mkv", ".avi", ".mov", ".webm", ".flv", ".wmv"}
    ext = os.path.splitext(video_path)[1].lower()

    audio_path = None
    try:
        if ext in video_extensions:
            logger.info("Extracting audio from video...")
            audio_path = extract_audio(video_path)
            transcribe_path = audio_path
        else:
            transcribe_path = video_path

        logger.info("Running transcription...")
        segments, info = model.transcribe(
            transcribe_path,
            language=req.language,
            beam_size=5,
            vad_filter=True,  # skip silence
        )

        # Collect all segments
        full_text_parts = []
        for segment in segments:
            full_text_parts.append(segment.text.strip())

        full_text = " ".join(full_text_parts)

        logger.info(f"Transcription complete. Language: {info.language}, Duration: {info.duration:.1f}s")

        return TranscribeResponse(
            text=full_text,
            language=info.language,
            duration=info.duration,
        )

    except FileNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        logger.error(f"Transcription failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if audio_path and os.path.exists(audio_path):
            os.unlink(audio_path)


@app.get("/health")
def health():
    return {"status": "ok", "model": MODEL_SIZE, "device": DEVICE}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)