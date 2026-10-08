import pytest
from app.detection.heuristics import heuristic_engine
from app.schemas.analysis import ClassificationEnum

def test_safe_normal_conversation():
    messages = [
        "Hey, are we meeting at 5 today?",
        "Good morning Dad, did you take your morning medicines?",
        "Call me when you're free.",
        "The milk delivery will arrive by 7am."
    ]
    for msg in messages:
        res = heuristic_engine.evaluate(msg)
        assert res["risk_score"] < 35, f"False positive on safe message: '{msg}', got score {res['risk_score']}"
        assert res["classification"] == ClassificationEnum.SAFE
        assert res["is_safe"] is True

def test_protective_bank_warning_false_positive_prevention():
    # Legitimate bank warning that mentions OTP or fraud, but is WARNING the user, NOT asking for OTP!
    messages = [
        "Dear Customer, do not share your OTP or PIN with anyone. SBI never asks for your credentials.",
        "Beware of fraud calls. Never share your confidential code with anyone claiming to be bank manager.",
        "Your Amazon order is dispatched. Never disclose your card details to the delivery agent."
    ]
    for msg in messages:
        res = heuristic_engine.evaluate(msg)
        assert res["risk_score"] < 35, f"Legitimate warning wrongly flagged! '{msg}' scored {res['risk_score']}"
        assert res["classification"] == ClassificationEnum.SAFE

def test_otp_phishing_scam():
    scam = "Your SBI bank account is blocked. Click http://sbi-unblock.xyz and send your OTP to verify KYC immediately."
    res = heuristic_engine.evaluate(scam)
    assert res["risk_score"] >= 70, f"Expected high risk, got {res['risk_score']}"
    assert res["classification"] == ClassificationEnum.SCAM
    assert "OTP" in res["category"] or "KYC" in res["category"]
    assert len(res["indicators"]) >= 2
    assert "Do NOT" in res["recommendation"]

def test_upi_collect_fraud():
    scam = "Approve collect request of Rs 5000 on Google Pay or enter UPI PIN to receive cashback reward."
    res = heuristic_engine.evaluate(scam)
    assert res["risk_score"] >= 70
    assert res["classification"] == ClassificationEnum.SCAM
    assert res["category"] == "UPI Scam"
    assert "UPI PIN" in res["recommendation"]

def test_electricity_cutoff_scam():
    scam = "Dear Consumer, your electricity power will be disconnected tonight at 9:30 PM due to unpaid bill. Immediately call our officer at 9876543210."
    res = heuristic_engine.evaluate(scam)
    assert res["risk_score"] >= 70
    assert res["classification"] == ClassificationEnum.SCAM
    assert "Government Impersonation" in res["category"] or "utility" in res["indicators"][0].lower()

def test_lottery_kbc_scam():
    scam = "Congratulations! You won Rs 25,00,000 in KBC Lucky Draw. Pay Rs 5,000 processing fee to claim your prize."
    res = heuristic_engine.evaluate(scam)
    assert res["risk_score"] >= 70
    assert res["classification"] == ClassificationEnum.SCAM
    assert res["category"] == "Lottery / Prize Scam"

def test_remote_access_anydesk_scam():
    scam = "Urgent: Customer support team needs you to install AnyDesk app to verify your phone banking."
    res = heuristic_engine.evaluate(scam)
    assert res["risk_score"] >= 70
    assert res["classification"] == ClassificationEnum.SCAM
    assert res["category"] == "Remote Access Scam"

def test_suspicious_package_delivery():
    suspicious = "Your parcel delivery failed. Update address link at http://tinyurl.com/track-pkt"
    res = heuristic_engine.evaluate(suspicious)
    assert res["risk_score"] >= 35, f"Expected at least suspicious, got {res['risk_score']}"
