# SCAMSHIELD — Backend & AI Architecture

## Overview
The SCAMSHIELD backend is implemented in **Python 3.14** with **FastAPI**, **Pydantic v2**, and **Groq Cloud API** (`llama-3.3-70b-versatile`).

## AI Integration Strategy
- **Provider**: Groq Cloud
- **Model**: `llama-3.3-70b-versatile` (or configurable in `.env`)
- **Latency**: Sub-second response time suitable for real-time notification evaluation.
- **Structured JSON Mode**: The prompt enforces a strict JSON schema with zero extraneous tokens or Markdown fences.
- **Protective Context Distinctions**: The system prompt specifically trains the model to distinguish between:
  - An attacker demanding or requesting an OTP/PIN (High Risk Scam)
  - A bank or legitimate service warning the user never to share their OTP (Safe / Educational)

## Fallback Mechanism
If the `GROQ_API_KEY` is not provided or if the Groq API is temporarily unreachable, the backend automatically falls back to its internal Python **HeuristicEngine**, which runs the identical rule set and returns valid structured output.

## Deployment Options
- **Local Dev / Emulator**: `http://10.0.2.2:8000`
- **Local Dev / Physical Phone**: `http://YOUR_LOCAL_IP:8000` (e.g. `http://192.168.1.100:8000`)
- **Cloud Hosting**: Compatible with Render, Railway, Fly.io, or AWS EC2 / Google Cloud Run via standard `uvicorn` runner.
