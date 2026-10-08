import json
import logging
from typing import Dict, Any, Optional
from groq import AsyncGroq
from ..config.settings import settings
from ..detection.heuristics import heuristic_engine
from ..schemas.analysis import ClassificationEnum

logger = logging.getLogger("scamshield.ai")

SYSTEM_PROMPT = """You are SCAMSHIELD's elite AI cybersecurity analysis engine.
Your task is to analyze incoming smartphone messages (SMS, WhatsApp, Telegram, etc.) for scams, fraud, phishing, and social engineering targeting elderly and everyday users.

You MUST respond ONLY with a valid JSON object with NO extra text, NO markdown fences, and NO code blocks.
The JSON must adhere to this exact schema:
{
  "risk_score": <integer from 0 to 100>,
  "classification": "SAFE" | "SUSPICIOUS" | "SCAM",
  "category": "<one of: OTP & Credential Phishing, Banking Scam, UPI Scam, Payment Fraud, Phishing, Fake KYC, Government Impersonation, Lottery / Prize Scam, Investment Scam, Job Scam, Delivery Scam, Fake Customer Support, Account Suspension Scam, Remote Access Scam, Identity Theft, Malicious Link, Social Engineering, Safe / Normal, Other>",
  "confidence": <float between 0.0 and 1.0>,
  "indicators": ["<concise threat indicator 1>", "<concise threat indicator 2>"],
  "recommendation": "<short, clear, actionable elderly-friendly guidance starting with Do NOT...>"
}

CRITICAL RULES:
1. FALSE POSITIVE PREVENTION: Distinguish between an attacker DEMANDING or ASKING for an OTP/PIN vs a legitimate service advisory saying "Never share your OTP with anyone" or transactional "Your OTP is 123456 for login at Amazon. Do not share." Normal messages like "Good morning", "Call me", "Where are you?" must score 0-10 (SAFE).
2. HIGH-RISK THREATS: Messages threatening account suspension, electricity disconnection, demanding UPI collect approval, fake lottery winnings, or asking to install AnyDesk/TeamViewer must score >= 80 (SCAM).
3. ELDERLY-FRIENDLY ADVICE: Recommendations must be crystal clear (e.g., "Do NOT enter your UPI PIN. You never enter a PIN to receive money.").
"""

class AIService:
    def __init__(self):
        self.groq_client: Optional[AsyncGroq] = None
        if settings.GROQ_API_KEY and settings.GROQ_API_KEY.strip():
            try:
                self.groq_client = AsyncGroq(api_key=settings.GROQ_API_KEY.strip())
            except Exception as e:
                logger.error(f"Failed to initialize Groq client: {e}")

    def is_ai_available(self) -> bool:
        return self.groq_client is not None

    async def analyze(self, content: str, sender: Optional[str] = None, package_name: Optional[str] = None) -> Dict[str, Any]:
        """
        Analyzes message content using Groq LLaMA 3.3 70B if available,
        with seamless fallback to the local Heuristic Engine.
        """
        # Always run local heuristics first for base signals and rapid baseline
        local_result = heuristic_engine.evaluate(content, sender)

        # If Groq client is configured, call Groq AI for deeper contextual understanding
        if self.groq_client:
            try:
                user_prompt = f"Sender: {sender or 'Unknown'}\nSource App: {package_name or 'SMS'}\nMessage Content:\n\"\"\"{content}\"\"\""
                
                response = await self.groq_client.chat.completions.create(
                    model=settings.GROQ_MODEL,
                    messages=[
                        {"role": "system", "content": SYSTEM_PROMPT},
                        {"role": "user", "content": user_prompt}
                    ],
                    temperature=0.1,
                    max_tokens=500,
                    response_format={"type": "json_object"}
                )

                raw_json = response.choices[0].message.content.strip()
                parsed = json.loads(raw_json)

                # Validate and extract
                risk_score = max(0, min(100, int(parsed.get("risk_score", local_result["risk_score"]))))
                classification_str = parsed.get("classification", "").upper()
                if classification_str not in ["SAFE", "SUSPICIOUS", "SCAM"]:
                    if risk_score >= settings.SCAM_THRESHOLD:
                        classification_str = ClassificationEnum.SCAM.value
                    elif risk_score >= settings.SUSPICIOUS_THRESHOLD:
                        classification_str = ClassificationEnum.SUSPICIOUS.value
                    else:
                        classification_str = ClassificationEnum.SAFE.value

                return {
                    "risk_score": risk_score,
                    "classification": classification_str,
                    "category": parsed.get("category", local_result["category"]),
                    "confidence": float(parsed.get("confidence", 0.90)),
                    "indicators": parsed.get("indicators", local_result["indicators"]),
                    "recommendation": parsed.get("recommendation", local_result["recommendation"]),
                    "is_safe": risk_score <= settings.SAFE_MAX_SCORE,
                    "engine": "groq_ai"
                }
            except Exception as e:
                logger.warning(f"Groq AI inference failed or timed out: {e}. Falling back to local heuristic engine.")
                # Fall through to local heuristic result with hybrid note
                local_result["engine"] = "local_heuristic_fallback"
                return local_result

        # Fallback to local heuristic engine
        return local_result

ai_service = AIService()
