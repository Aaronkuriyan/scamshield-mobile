# SCAMSHIELD — AI Cybersecurity Backend

Production-ready FastAPI backend for SCAMSHIELD Android application. Analyzes incoming messages for scams, fraud, phishing, and social engineering in real time using Groq LLaMA 3.3 70B and a local weighted heuristic engine.

## Features
- **High-Speed Inference**: Sub-second analysis powered by Groq LLaMA 3.3 70B.
- **Offline / Local Fallback**: Heuristic engine protects users even when internet is unavailable or backend API key is unset.
- **False-Positive Prevention**: Context-aware protective rules distinguish between attackers asking for credentials vs. bank advisories warning users not to share OTPs.
- **Privacy-First Audit**: Only threat metadata (timestamp, risk score, classification, package) is logged; sensitive message text is **never** retained.
- **Structured Schema**: Clean Pydantic v2 validation adhering to the SCAMSHIELD Android contract.

## Quickstart

### 1. Set Up Environment
```bash
python -m venv .venv
# Windows:
.venv\Scripts\activate
# Linux/macOS:
source .venv/bin/activate

pip install -r requirements.txt
```

### 2. Configure Environment Variables
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Add your Groq API key (optional for AI, rule engine functions without it):
```env
GROQ_API_KEY=gsk_...
GROQ_MODEL=llama-3.3-70b-versatile
```

### 3. Run Development Server
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

The API will be available at:
- Swagger Docs: `http://localhost:8000/docs`
- Health Status: `http://localhost:8000/api/health`
- Analysis Endpoint: `http://localhost:8000/api/analyze`

### 4. Run Test Suite
```bash
pytest -v
```
