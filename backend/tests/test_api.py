import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_api_root():
    response = client.get("/")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "online"
    assert data["app"] == "SCAMSHIELD Backend"

def test_health_check():
    response = client.get("/api/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "operational"
    assert "ai_available" in data
    assert "version" in data

def test_categories_endpoint():
    response = client.get("/api/categories")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) >= 8
    categories = [cat["category"] for cat in data]
    assert "OTP & Credential Phishing" in categories
    assert "UPI Scam" in categories
    assert "Fake KYC" in categories

def test_analyze_scam_endpoint():
    payload = {
        "content": "Your bank account has been blocked today. Click this link http://bit.ly/bank-fix immediately and enter your OTP to verify KYC.",
        "sender": "+919876543210",
        "package_name": "com.google.android.apps.messaging"
    }
    response = client.post("/api/analyze", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["risk_score"] >= 70
    assert data["classification"] == "SCAM"
    assert data["is_safe"] is False
    assert len(data["indicators"]) >= 2
    assert "Do NOT" in data["recommendation"]

def test_analyze_safe_endpoint():
    payload = {
        "content": "Hey Grandma, dinner is ready at 7. See you soon!",
        "sender": "Granddaughter",
        "package_name": "com.whatsapp"
    }
    response = client.post("/api/analyze", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["risk_score"] < 35
    assert data["classification"] == "SAFE"
    assert data["is_safe"] is True

def test_batch_analyze_endpoint():
    payload = {
        "messages": [
            {"content": "Hello how are you?"},
            {"content": "Congratulations you won Rs 1000000. Send Rs 2000 processing fee now."}
        ]
    }
    response = client.post("/api/analyze/batch", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["total_processed"] == 2
    assert data["results"][0]["classification"] == "SAFE"
    assert data["results"][1]["classification"] == "SCAM"

def test_history_audit_endpoint():
    response = client.get("/api/history")
    assert response.status_code == 200
    data = response.json()
    assert "total_scanned" in data
    assert "history" in data
    assert data["total_scanned"] >= 1
