from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime, timezone
from enum import Enum

class ClassificationEnum(str, Enum):
    SAFE = "SAFE"
    SUSPICIOUS = "SUSPICIOUS"
    SCAM = "SCAM"

class ScamCategoryEnum(str, Enum):
    OTP_CREDENTIAL_PHISHING = "OTP & Credential Phishing"
    BANKING_SCAM = "Banking Scam"
    UPI_SCAM = "UPI Scam"
    PAYMENT_FRAUD = "Payment Fraud"
    PHISHING = "Phishing"
    FAKE_KYC = "Fake KYC"
    GOVERNMENT_IMPERSONATION = "Government Impersonation"
    LOTTERY_PRIZE_SCAM = "Lottery / Prize Scam"
    INVESTMENT_SCAM = "Investment Scam"
    JOB_SCAM = "Job Scam"
    DELIVERY_SCAM = "Delivery Scam"
    FAKE_CUSTOMER_SUPPORT = "Fake Customer Support"
    ACCOUNT_SUSPENSION_SCAM = "Account Suspension Scam"
    REMOTE_ACCESS_SCAM = "Remote Access Scam"
    IDENTITY_THEFT = "Identity Theft"
    MALICIOUS_LINK = "Malicious Link"
    SOCIAL_ENGINEERING = "Social Engineering"
    SAFE_NORMAL = "Safe / Normal"
    OTHER = "Other"

class AnalyzeRequest(BaseModel):
    content: str = Field(..., description="Message text or notification snippet to analyze", min_length=1)
    sender: Optional[str] = Field(None, description="Sender name, ID, or phone number")
    package_name: Optional[str] = Field(None, description="Source Android application package name")
    metadata: Optional[dict] = Field(default_factory=dict, description="Optional scan metadata")

class AnalyzeResponse(BaseModel):
    risk_score: int = Field(..., ge=0, le=100, description="Risk score from 0 (Safe) to 100 (Critical Scam)")
    classification: ClassificationEnum = Field(..., description="Classification: SAFE, SUSPICIOUS, or SCAM")
    category: str = Field(..., description="Specific threat category")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Confidence score from 0.0 to 1.0")
    indicators: List[str] = Field(default_factory=list, description="Specific threat signals detected")
    what_to_do: List[str] = Field(default_factory=list, description="Actionable safety actions to take")
    what_not_to_do: List[str] = Field(default_factory=list, description="Actions the user should avoid")
    recommendation: str = Field(..., description="Actionable plain-language instruction for user")
    is_safe: bool = Field(..., description="Convenience flag: True if risk_score < 35")
    analyzed_at: str = Field(default_factory=lambda: datetime.now(timezone.utc).isoformat())
    engine: str = Field(default="hybrid", description="Detection engine used: local_heuristic, groq_ai, or hybrid")

class BatchAnalyzeRequest(BaseModel):
    messages: List[AnalyzeRequest] = Field(..., min_length=1, max_length=25)

class BatchAnalyzeResponse(BaseModel):
    results: List[AnalyzeResponse]
    total_processed: int

class HealthResponse(BaseModel):
    status: str
    app: str
    version: str
    ai_available: bool
    ai_provider: str
    ai_model: str
    timestamp: str

class CategoryInfo(BaseModel):
    category: str
    description: str
    example: str
