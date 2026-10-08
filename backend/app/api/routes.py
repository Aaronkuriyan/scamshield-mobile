from fastapi import APIRouter, HTTPException, status
from typing import List
from datetime import datetime, timezone
from ..schemas.analysis import (
    AnalyzeRequest,
    AnalyzeResponse,
    BatchAnalyzeRequest,
    BatchAnalyzeResponse,
    HealthResponse,
    CategoryInfo,
    ScamCategoryEnum
)
from ..services.ai_service import ai_service
from ..config.settings import settings

router = APIRouter(prefix="/api", tags=["Detection & Analysis"])

# Lightweight in-memory audit log of scans (stores only safe metadata, privacy-first)
_scan_history_audit: List[dict] = []

CATEGORY_CATALOG: List[CategoryInfo] = [
    CategoryInfo(
        category=ScamCategoryEnum.OTP_CREDENTIAL_PHISHING.value,
        description="Fraudulent attempts to extract one-time passwords, net-banking passwords, or CVVs.",
        example="Your account has suspicious activity. Send the 6-digit OTP received right now to avoid blocking."
    ),
    CategoryInfo(
        category=ScamCategoryEnum.UPI_SCAM.value,
        description="Tricking victims into entering UPI PINs or approving collect requests to 'receive' money.",
        example="Click approve on PhonePe collect request of Rs 5000 to receive your refund."
    ),
    CategoryInfo(
        category=ScamCategoryEnum.FAKE_KYC.value,
        description="Impersonating banks or telecom providers demanding urgent KYC updates via malicious links.",
        example="Dear customer, your SBI Netbanking KYC expired. Update Aadhaar & PAN within 24 hours at http://sbi-kyc-verify.xyz"
    ),
    CategoryInfo(
        category=ScamCategoryEnum.GOVERNMENT_IMPERSONATION.value,
        description="Posing as electricity boards, traffic police (Challan), or government tax agencies.",
        example="Dear customer, your electricity power will be disconnected at 9:30 PM tonight due to unpaid bill. Call 9876543210 immediately."
    ),
    CategoryInfo(
        category=ScamCategoryEnum.LOTTERY_PRIZE_SCAM.value,
        description="Fabricated lottery or gift card winnings demanding advance processing or tax payments.",
        example="Congratulations! You won Rs 25,00,000 in KBC Lucky Draw. Pay Rs 5,000 processing fee to release prize."
    ),
    CategoryInfo(
        category=ScamCategoryEnum.JOB_SCAM.value,
        description="Fake work-from-home or YouTube task offers that require upfront deposits.",
        example="Part time job: Earn Rs 3,000 to Rs 8,000 daily by liking YouTube videos. Contact HR on WhatsApp now."
    ),
    CategoryInfo(
        category=ScamCategoryEnum.DELIVERY_SCAM.value,
        description="Fake package delivery issues asking for small payments or clicking spoofed tracking links.",
        example="IndiaPost: Your package could not be delivered due to wrong address. Pay Rs 48 redelivery fee at http://post-track.click"
    ),
    CategoryInfo(
        category=ScamCategoryEnum.REMOTE_ACCESS_SCAM.value,
        description="Trickery asking victim to install remote control apps like AnyDesk or TeamViewer.",
        example="Bank customer support: Please install AnyDesk app and share your 9-digit code to resolve card block."
    ),
    CategoryInfo(
        category=ScamCategoryEnum.ACCOUNT_SUSPENSION_SCAM.value,
        description="Creating acute urgency by claiming cards or bank accounts will be deactivated.",
        example="Urgent: Your HDFC Debit Card is blocked. Click here immediately to reactivate."
    ),
]

@router.get("/health", response_model=HealthResponse)
async def get_health():
    """Health check endpoint exposing system status and AI readiness."""
    return HealthResponse(
        status="operational",
        app=settings.APP_NAME,
        version=settings.APP_VERSION,
        ai_available=ai_service.is_ai_available(),
        ai_provider="Groq (LLaMA 3.3 70B)" if ai_service.is_ai_available() else "Local Heuristics (Offline-Ready)",
        ai_model=settings.GROQ_MODEL if ai_service.is_ai_available() else "Rule-based Weighted Engine",
        timestamp=datetime.now(timezone.utc).isoformat()
    )

@router.post("/analyze", response_model=AnalyzeResponse)
async def analyze_message(request: AnalyzeRequest):
    """
    Main detection endpoint:
    Analyzes message content using Groq AI and/or local heuristic rules.
    Returns structured threat evaluation with 0-100 score, classification, and advice.
    """
    if not request.content or not request.content.strip():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Message content cannot be empty.")

    result = await ai_service.analyze(
        content=request.content,
        sender=request.sender,
        package_name=request.package_name
    )

    # Privacy-conscious metadata logging (NO message plaintext is stored)
    log_entry = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "sender_source": request.sender or "Unknown",
        "package_name": request.package_name or "SMS",
        "risk_score": result["risk_score"],
        "classification": result["classification"],
        "category": result["category"],
        "engine": result["engine"]
    }
    _scan_history_audit.append(log_entry)
    # Cap in-memory audit log to last 100 entries
    if len(_scan_history_audit) > 100:
        _scan_history_audit.pop(0)

    return AnalyzeResponse(
        risk_score=result["risk_score"],
        classification=result["classification"],
        category=result["category"],
        confidence=result["confidence"],
        indicators=result["indicators"],
        recommendation=result["recommendation"],
        is_safe=result["is_safe"],
        analyzed_at=datetime.now(timezone.utc).isoformat(),
        engine=result["engine"]
    )

@router.post("/analyze/batch", response_model=BatchAnalyzeResponse)
async def analyze_batch_messages(request: BatchAnalyzeRequest):
    """Batch analysis endpoint for processing multiple message snippets."""
    results: List[AnalyzeResponse] = []
    for item in request.messages:
        res = await ai_service.analyze(item.content, item.sender, item.package_name)
        results.append(AnalyzeResponse(
            risk_score=res["risk_score"],
            classification=res["classification"],
            category=res["category"],
            confidence=res["confidence"],
            indicators=res["indicators"],
            recommendation=res["recommendation"],
            is_safe=res["is_safe"],
            analyzed_at=datetime.now(timezone.utc).isoformat(),
            engine=res["engine"]
        ))
    return BatchAnalyzeResponse(results=results, total_processed=len(results))

@router.get("/categories", response_model=List[CategoryInfo])
async def get_categories():
    """Returns all supported threat categories with plain descriptions and examples."""
    return CATEGORY_CATALOG

@router.get("/history")
async def get_scan_history():
    """
    Returns anonymized scan metadata history (privacy-preserving: no raw message text).
    """
    return {
        "total_scanned": len(_scan_history_audit),
        "history": list(reversed(_scan_history_audit[-20:]))
    }
