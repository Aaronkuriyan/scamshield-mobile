package com.scamshield.app.util

import android.content.Context
import com.scamshield.app.R

object LocalizationHelper {

    fun getLocalizedCategory(context: Context, category: String): String {
        val clean = category.trim()
        val resId = when {
            clean.contains("UPI", ignoreCase = true) -> R.string.category_upi_scam
            clean.contains("KYC", ignoreCase = true) -> R.string.category_fake_kyc
            clean.contains("OTP", ignoreCase = true) || clean.contains("Phishing", ignoreCase = true) -> R.string.category_phishing
            clean.contains("Suspension", ignoreCase = true) || clean.contains("Blocked", ignoreCase = true) -> R.string.category_account_suspension
            clean.contains("Government", ignoreCase = true) || clean.contains("Electricity", ignoreCase = true) -> R.string.category_gov_impersonation
            clean.contains("Lottery", ignoreCase = true) || clean.contains("Prize", ignoreCase = true) -> R.string.category_lottery_prize
            clean.contains("Remote", ignoreCase = true) || clean.contains("AnyDesk", ignoreCase = true) -> R.string.category_remote_access
            clean.contains("Delivery", ignoreCase = true) || clean.contains("Courier", ignoreCase = true) -> R.string.category_delivery_scam
            clean.contains("Link", ignoreCase = true) -> R.string.category_malicious_link
            clean.contains("Social", ignoreCase = true) || clean.contains("Urgency", ignoreCase = true) -> R.string.category_social_engineering
            clean.contains("Safe", ignoreCase = true) -> R.string.category_safe_normal
            else -> R.string.category_general_suspicious
        }
        return context.getString(resId)
    }

    fun getLocalizedRecommendation(context: Context, recommendation: String, category: String, classification: String): String {
        val lang = LocaleHelper.getLanguage(context)
        if (lang == "en") return recommendation

        val cleanCat = category.trim().lowercase()
        return when (lang) {
            "kn" -> when {
                classification == "SAFE" || cleanCat.contains("safe") ->
                    "ಯಾವುದೇ ಗಮನಾರ್ಹ ವಂಚನೆ ಲಕ್ಷಣಗಳು ಕಂಡುಬಂದಿಲ್ಲ. ನೀವು ಸಾಮಾನ್ಯವಾಗಿ ಮುಂದುವರಿಯಬಹುದು."
                cleanCat.contains("upi") ->
                    "ನಿಮ್ಮ UPI PIN ಅನ್ನು ನಮೂದಿಸಬೇಡಿ. ಹಣವನ್ನು ಸ್ವೀಕರಿಸಲು ನೀವು ಎಂದಿಗೂ PIN ನಮೂದಿಸುವ ಅಗತ್ಯವಿಲ್ಲ."
                cleanCat.contains("kyc") || cleanCat.contains("suspension") ->
                    "ಈ ಸಂದೇಶದಲ್ಲಿರುವ ಯಾವುದೇ ಲಿಂಕ್ ಅನ್ನು ಕ್ಲಿಕ್ ಮಾಡಬೇಡಿ. ನಿಮ್ಮ ಬ್ಯಾಂಕ್ ಶಾಖೆಗೆ ನೇರವಾಗಿ ಕರೆ ಮಾಡಿ."
                cleanCat.contains("otp") || cleanCat.contains("phishing") ->
                    "ನಿಮ್ಮ OTP ಅಥವಾ ಪಾಸ್‌ವರ್ಡ್ ಅನ್ನು ಯಾರೊಂದಿಗೂ ಹಂಚಿಕೊಳ್ಳಬೇಡಿ. ಅಧಿಕೃತ ಬ್ಯಾಂಕುಗಳು ಎಂದಿಗೂ ನಿಮ್ಮ ಕೋಡ್ ಕೇಳುವುದಿಲ್ಲ."
                cleanCat.contains("electricity") || cleanCat.contains("government") ->
                    "ಈ ಸಂದೇಶದಲ್ಲಿರುವ ಸಂಖ್ಯೆಗಳಿಗೆ ಹಣ ಪಾವತಿಸಬೇಡಿ ಅಥವಾ ಕರೆ ಮಾಡಬೇಡಿ. ವಿದ್ಯುತ್ ಇಲಾಖೆಯು SMS ಮೂಲಕ ಸಂಪರ್ಕ ಕಡಿತಗೊಳಿಸುವುದಿಲ್ಲ."
                cleanCat.contains("lottery") || cleanCat.contains("prize") ->
                    "ಯಾವುದೇ ಪ್ರೊಸೆಸಿಂಗ್ ಶುಲ್ಕವನ್ನು ಪಾವತಿಸಬೇಡಿ. ನೈಜ ಲಾಟರಿಗಳು ಎಂದಿಗೂ ಮುಂಗಡ ಹಣವನ್ನು ಕೇಳುವುದಿಲ್ಲ."
                cleanCat.contains("remote") || cleanCat.contains("anydesk") ->
                    "AnyDesk ಅಥವಾ ಸ್ಕ್ರೀನ್-ಹಂಚಿಕೆ ಆಪ್‌ಗಳನ್ನು ಇನ್‌ಸ್ಟಾಲ್ ಮಾಡಬೇಡಿ. ಕಳುಹಿಸಿದವರು ನಿಮ್ಮ ಬ್ಯಾಂಕಿನಿಂದ ಹಣ ಕದಿಯಬಹುದು."
                cleanCat.contains("delivery") ->
                    "ಲಿಂಕ್ ಕ್ಲಿಕ್ ಮಾಡಬೇಡಿ ಅಥವಾ ಮರು-ವಿತರಣಾ ಶುಲ್ಕವನ್ನು ಪಾವತಿಸಬೇಡಿ. ಅಧಿಕೃತ ಶಾಪಿಂಗ್ ಆ್ಯಪ್‌ನಲ್ಲಿ ನಿಮ್ಮ ಆರ್ಡರ್ ಪರಿಶೀಲಿಸಿ."
                else ->
                    "ಎಚ್ಚರಿಕೆಯಿಂದಿರಿ. ಯಾವುದೇ ಕ್ರಮ ತೆಗೆದುಕೊಳ್ಳುವ ಮುನ್ನ ಅಧಿಕೃತ ಮೂಲಗಳಿಂದ ಪರಿಶೀಲಿಸಿ."
            }
            "hi" -> when {
                classification == "SAFE" || cleanCat.contains("safe") ->
                    "कोई खतरा नहीं मिला। आप सामान्य रूप से बातचीत जारी रख सकते हैं।"
                cleanCat.contains("upi") ->
                    "अपना UPI पिन दर्ज न करें। पैसे प्राप्त करने के लिए आपको कभी भी पिन दर्ज करने की आवश्यकता नहीं होती है।"
                cleanCat.contains("kyc") || cleanCat.contains("suspension") ->
                    "इस संदेश के किसी भी लिंक पर क्लिक न करें। सीधे अपनी बैंक शाखा से संपर्क करें।"
                cleanCat.contains("otp") || cleanCat.contains("phishing") ->
                    "अपना OTP या पासवर्ड किसी के साथ साझा न करें। बैंक अधिकारी कभी भी आपका कोड नहीं मांगते हैं।"
                cleanCat.contains("electricity") || cleanCat.contains("government") ->
                    "इस संदेश में दिए गए नंबरों पर भुगतान न करें या कॉल न करें। बिजली विभाग SMS द्वारा बिजली नहीं काटता है।"
                cleanCat.contains("lottery") || cleanCat.contains("prize") ->
                    "कोई भी प्रोसेसिंग शुल्क न दें। वैध लॉटरी कभी भी अग्रिम भुगतान नहीं मांगती हैं।"
                cleanCat.contains("remote") || cleanCat.contains("anydesk") ->
                    "AnyDesk या स्क्रीन-शेयरिंग ऐप इंस्टॉल न करें। भेजने वाला आपके बैंक खाते से पैसे चुरा सकता है।"
                cleanCat.contains("delivery") ->
                    "लिंक पर क्लिक न करें या दोबारा डिलीवरी का शुल्क न दें। आधिकारिक शॉपिंग ऐप में अपना ऑर्डर जांचें।"
                else ->
                    "सावधान रहें। कोई भी कदम उठाने से पहले आधिकारिक माध्यमों से पुष्टि करें।"
            }
            else -> recommendation
        }
    }

    fun getLocalizedIndicators(context: Context, indicators: List<String>): List<String> {
        val lang = LocaleHelper.getLanguage(context)
        if (lang == "en" || indicators.isEmpty()) return indicators

        return indicators.map { indicator ->
            val clean = indicator.lowercase()
            when (lang) {
                "kn" -> when {
                    clean.contains("otp") || clean.contains("verification code") ->
                        "OTP ಅಥವಾ ಪರಿಶೀಲನಾ ಕೋಡ್ ಹಂಚಿಕೊಳ್ಳಲು ಒತ್ತಾಯಿಸುತ್ತದೆ"
                    clean.contains("upi") || clean.contains("collect request") ->
                        "ಹಣ ಸ್ವೀಕರಿಸಲು UPI PIN ನಮೂದಿಸಲು ಅಥವಾ ರಿಕ್ವೆಸ್ಟ್ ಅನುಮೋದಿಸಲು ಮೋಸ ಮಾಡುತ್ತದೆ"
                    clean.contains("block") || clean.contains("suspend") ->
                        "ಬ್ಯಾಂಕ್ ಖಾತೆ ಅಥವಾ ಕಾರ್ಡ್ ತಕ್ಷಣವೇ ಬ್ಲಾಕ್ ಆಗುತ್ತದೆ ಎಂದು ಬೆದರಿಸುತ್ತದೆ"
                    clean.contains("kyc") || clean.contains("aadhaar") ->
                        "ತುರ್ತು KYC ಅಪ್‌ಡೇಟ್ ಅಥವಾ ಆಧಾರ್/PAN ಲಿಂಕ್ ಮಾಡಲು ಒತ್ತಾಯಿಸುತ್ತದೆ"
                    clean.contains("electricity") || clean.contains("power") ->
                        "ಬಿಲ್ ಬಾಕಿ ಇದೆ ಎಂದು ನಕಲಿ ವಿದ್ಯುತ್ ಸಂಪರ್ಕ ಕಡಿತದ ಬೆದರಿಕೆ ಹಾಕುತ್ತದೆ"
                    clean.contains("lottery") || clean.contains("prize") ->
                        "ಮುಂಗಡ ಶುಲ್ಕ ಕೇಳಿ ನಕಲಿ ಲಾಟರಿ ಬಹುಮಾನ ಬಂದಿದೆ ಎಂದು ಹೇಳುತ್ತದೆ"
                    clean.contains("screen") || clean.contains("anydesk") || clean.contains("remote") ->
                        "AnyDesk ಅಥವಾ TeamViewer ನಂತಹ ಸ್ಕ್ರೀನ್-ಹಂಚಿಕೆ ಆಪ್ ಇನ್‌ಸ್ಟಾಲ್ ಮಾಡಲು ಒತ್ತಾಯಿಸುತ್ತದೆ"
                    clean.contains("delivery") || clean.contains("parcel") ->
                        "ಪಾರ್ಸೆಲ್ ವಿತರಣಾ ದೋಷದ ನೆಪದಲ್ಲಿ ಹಣ ಅಥವಾ ಲಿಂಕ್ ಕ್ಲಿಕ್ ಮಾಡಲು ಕೇಳುತ್ತದೆ"
                    clean.contains("link") || clean.contains("url") ->
                        "ಅನುಮಾನಾಸ್ಪದ ಅಥವಾ ಸಂಕ್ಷಿಪ್ತಗೊಳಿಸಿದ ಅಸುರಕ್ಷಿತ ಲಿಂಕ್ ಹೊಂದಿದೆ"
                    clean.contains("urgency") || clean.contains("pressure") ->
                        "ಕೃತಕ ತುರ್ತು ಅಥವಾ ಒತ್ತಡವನ್ನು ಸೃಷ್ಟಿಸುತ್ತದೆ"
                    else -> indicator
                }
                "hi" -> when {
                    clean.contains("otp") || clean.contains("verification code") ->
                        "OTP या वेरिफिकेशन कोड साझा करने की मांग करता है"
                    clean.contains("upi") || clean.contains("collect request") ->
                        "पैसे प्राप्त करने के लिए UPI पिन डालने या रिक्वेस्ट स्वीकार करने का झांसा देता है"
                    clean.contains("block") || clean.contains("suspend") ->
                        "बैंक खाता या कार्ड तुरंत ब्लॉक करने की धमकी देता है"
                    clean.contains("kyc") || clean.contains("aadhaar") ->
                        "तुरंत KYC अपडेट या आधार/PAN लिंक करने की मांग करता है"
                    clean.contains("electricity") || clean.contains("power") ->
                        "फर्जी बिल के नाम पर बिजली काटने की धमकी देता है"
                    clean.contains("lottery") || clean.contains("prize") ->
                        "अग्रिम शुल्क मांगकर फर्जी लॉटरी जीतने का दावा करता है"
                    clean.contains("screen") || clean.contains("anydesk") || clean.contains("remote") ->
                        "AnyDesk या TeamViewer जैसे स्क्रीन-शेयरिंग ऐप डाउनलोड करने का दबाव बनाता है"
                    clean.contains("delivery") || clean.contains("parcel") ->
                        "पार्सल डिलीवरी में समस्या बताकर भुगतान या लिंक पर क्लिक करने को कहता है"
                    clean.contains("link") || clean.contains("url") ->
                        "संदिग्ध या छोटा किया गया असुरक्षित लिंक शामिल है"
                    clean.contains("urgency") || clean.contains("pressure") ->
                        "दबाव या तुरंत कार्रवाई करने की जल्दी पैदा करता है"
                    else -> indicator
                }
                else -> indicator
            }
        }
    }

    fun getLocalizedWhatToDo(context: Context, category: String, classification: String, defaultList: List<String>): List<String> {
        val lang = LocaleHelper.getLanguage(context)
        if (lang == "en") return defaultList

        val cleanCat = category.trim().lowercase()
        return when (lang) {
            "kn" -> when {
                classification == "SAFE" || cleanCat.contains("safe") -> listOf(
                    "ನೀವು ಸಾಮಾನ್ಯವಾಗಿ ಮುಂದುವರಿಯಬಹುದು.",
                    "ಯಾವುದೇ ವಿಶೇಷ ಕ್ರಮದ ಅಗತ್ಯವಿಲ್ಲ."
                )
                cleanCat.contains("upi") -> listOf(
                    "ಕಲೆಕ್ಟ್ ರಿಕ್ವೆಸ್ಟ್ ಅನ್ನು ತಕ್ಷಣ ತಿರಸ್ಕರಿಸಿ ಅಥವಾ ನಿರ್ಲಕ್ಷಿಸಿ.",
                    "ನಿಮ್ಮ ಬ್ಯಾಂಕ್ ಆಪ್ ಮೂಲಕ ಖಾತೆಯ ಬ್ಯಾಲೆನ್ಸ್ ಪರಿಶೀಲಿಸಿ.",
                    "1930 ಸೈಬರ್ ಸಹಾಯವಾಣಿ ಅಥವಾ cybercrime.gov.in ಗೆ ದೂರು ನೀಡಿ."
                )
                cleanCat.contains("kyc") || cleanCat.contains("suspension") -> listOf(
                    "ನಿಮ್ಮ ಪಾಸ್‌ಬುಕ್‌ನಲ್ಲಿರುವ ಅಧಿಕೃತ ಸಂಖ್ಯೆಯಿಂದ ಬ್ಯಾಂಕ್ ಸಂಪರ್ಕಿಸಿ.",
                    "KYC ಪರಿಶೀಲನೆಗಾಗಿ ಹತ್ತಿರದ ಅಧಿಕೃತ ಬ್ಯಾಂಕ್ ಶಾಖೆಗೆ ಭೇಟಿ ನೀಡಿ.",
                    "ಯಾವುದೇ ಕ್ರಮ ಕೈಗೊಳ್ಳುವ ಮುನ್ನ ಕುಟುಂಬದ ವಿಶ್ವಾಸಾರ್ಹ ಸದಸ್ಯರನ್ನು ಕೇಳಿ."
                )
                cleanCat.contains("otp") || cleanCat.contains("phishing") -> listOf(
                    "ನಿಮ್ಮ OTP ಯನ್ನು ರಹಸ್ಯವಾಗಿಡಿ; ಇದು ಕಟ್ಟುನಿಟ್ಟಾಗಿ ಖಾಸಗಿಯಾಗಿದೆ.",
                    "ಆಕಸ್ಮಿಕವಾಗಿ ಹಂಚಿಕೊಂಡಿದ್ದರೆ ತಕ್ಷಣ ಬ್ಯಾಂಕ್ ಗ್ರಾಹಕ ಸೇವೆಗೆ ಕರೆ ಮಾಡಿ.",
                    "ಕಳುಹಿಸಿದವರನ್ನು ಬ್ಲಾಕ್ ಮಾಡಿ ಮತ್ತು ವರದಿ ಮಾಡಿ."
                )
                cleanCat.contains("electricity") || cleanCat.contains("government") -> listOf(
                    "ನಿಮ್ಮ ಜಿಲ್ಲೆಯ ಅಧಿಕೃತ ವಿದ್ಯುತ್ ಅಥವಾ ಪಾಲಿಕೆ ಸಹಾಯವಾಣಿಗೆ ಕರೆ ಮಾಡಿ.",
                    "ಅಧಿಕೃತ ಕೌಂಟರ್‌ಗಳು ಅಥವಾ ಅಧಿಕೃತ ಪೋರ್ಟಲ್ ಮೂಲಕ ಮಾತ್ರ ಬಿಲ್ ಪಾವತಿಸಿ.",
                    "ನಕಲಿ ಸಂದೇಶಗಳ ಬಗ್ಗೆ ಸ್ಥಳೀಯ ಅಧಿಕಾರಿಗಳಿಗೆ ವರದಿ ಮಾಡಿ."
                )
                cleanCat.contains("lottery") || cleanCat.contains("prize") -> listOf(
                    "ಈ ಸಂದೇಶವನ್ನು ತಕ್ಷಣ ಅಳಿಸಿ ಮತ್ತು ನಿರ್ಲಕ್ಷಿಸಿ.",
                    "ಕಳುಹಿಸಿದವರ ಫೋನ್ ಸಂಖ್ಯೆಯನ್ನು ಬ್ಲಾಕ್ ಮಾಡಿ.",
                    "ನೆನಪಿಡಿ: ನೀವು ಸ್ಪರ್ಧೆಯಲ್ಲಿ ಭಾಗವಹಿಸದಿದ್ದರೆ, ನಿಮಗೆ ಬಹುಮಾನ ಬರುವುದಿಲ್ಲ."
                )
                cleanCat.contains("delivery") -> listOf(
                    "ಅಧಿಕೃತ ಶಾಪಿಂಗ್ ಆಪ್‌ನಲ್ಲಿ ಪಾರ್ಸೆಲ್ ವಿವರಗಳನ್ನು ನೇರವಾಗಿ ಪರಿಶೀಲಿಸಿ.",
                    "ಅನುಮಾನವಿದ್ದರೆ ಅಧಿಕೃತ ಕೊರಿಯರ್ ಸಹಾಯವಾಣಿಗೆ ಕರೆ ಮಾಡಿ.",
                    "ಅಪರಿಚಿತ SMS ಮೂಲಕ ಬಂದ ಟ್ರ್ಯಾಕಿಂಗ್ ಲಿಂಕ್‌ಗಳನ್ನು ನಿರ್ಲಕ್ಷಿಸಿ."
                )
                cleanCat.contains("remote") || cleanCat.contains("anydesk") -> listOf(
                    "ಕರೆಯನ್ನು ತಕ್ಷಣ ಕಟ್ ಮಾಡಿ.",
                    "ಆಪ್ ಇನ್‌ಸ್ಟಾಲ್ ಆಗಿರಬಹುದು ಎಂದು ಸಂಶಯವಿದ್ದರೆ ಇಂಟರ್ನೆಟ್ ಆಫ್ ಮಾಡಿ.",
                    "ಕುಟುಂಬದ ಸದಸ್ಯರು ಅಥವಾ ತಂತ್ರಜ್ಞರಿಗೆ ಫೋನ್ ಪರಿಶೀಲಿಸಲು ಹೇಳಿ."
                )
                else -> listOf(
                    "ಅಧಿಕೃತ ಸಾರ್ವಜನಿಕ ಮೂಲಗಳ ಮೂಲಕ ಕಳುಹಿಸಿದವರನ್ನು ಪರಿಶೀಲಿಸಿ.",
                    "ಕ್ರಮ ಕೈಗೊಳ್ಳುವ ಮುನ್ನ ಕುಟುಂಬದ ಸದಸ್ಯರನ್ನು ಕೇಳಿ ಅಥವಾ 1930 ಗೆ ಕರೆ ಮಾಡಿ.",
                    "ಸಂಶಯಾಸ್ಪದ ಸಂದೇಶವನ್ನು ವರದಿ ಮಾಡಿ."
                )
            }
            "hi" -> when {
                classification == "SAFE" || cleanCat.contains("safe") -> listOf(
                    "आप सामान्य रूप से बातचीत जारी रख सकते हैं।",
                    "किसी विशेष कार्रवाई की आवश्यकता नहीं है।"
                )
                cleanCat.contains("upi") -> listOf(
                    "कलेक्ट रिक्वेस्ट को तुरंत अस्वीकार या अनदेखा करें।",
                    "अपने बैंक ऐप से सीधे खाते का बैलेंस जांचें।",
                    "1930 साइबर हेल्पलाइन या cybercrime.gov.in पर रिपोर्ट करें।"
                )
                cleanCat.contains("kyc") || cleanCat.contains("suspension") -> listOf(
                    "पासबुक पर दिए आधिकारिक नंबर से बैंक से संपर्क करें।",
                    "KYC सत्यापन के लिए अपनी नजदीकी बैंक शाखा जाएं।",
                    "कोई भी कदम उठाने से पहले परिवार के भरोसेमंद सदस्य से पूछें।"
                )
                cleanCat.contains("otp") || cleanCat.contains("phishing") -> listOf(
                    "अपना OTP पूरी तरह गोपनीय रखें; यह केवल आपके लिए है।",
                    "गलती से साझा होने पर तुरंत बैंक कस्टमर केयर से संपर्क करें।",
                    "संदेश भेजने वाले को ब्लॉक करें और रिपोर्ट करें।"
                )
                cleanCat.contains("electricity") || cleanCat.contains("government") -> listOf(
                    "अपने क्षेत्र के आधिकारिक बिजली या सरकारी हेल्पलाइन पर कॉल करें।",
                    "बिल का भुगतान केवल अधिकृत काउंटर या आधिकारिक पोर्टल से करें।",
                    "धोखाधड़ी वाले संदेश की सूचना स्थानीय अधिकारियों को दें।"
                )
                cleanCat.contains("lottery") || cleanCat.contains("prize") -> listOf(
                    "इस संदेश को तुरंत मिटाएं और अनदेखा करें।",
                    "संदेश भेजने वाले का फोन नंबर ब्लॉक करें।",
                    "याद रखें: यदि आपने लॉटरी नहीं खरीदी, तो आप जीत नहीं सकते।"
                )
                cleanCat.contains("delivery") -> listOf(
                    "आधिकारिक शॉपिंग ऐप में सीधे पार्सल की स्थिति जांचें।",
                    "संदेह होने पर आधिकारिक कूरियर हेल्पलाइन पर कॉल करें।",
                    "अनजान SMS द्वारा भेजे गए ट्रैकिंग लिंक को अनदेखा करें।"
                )
                cleanCat.contains("remote") || cleanCat.contains("anydesk") -> listOf(
                    "फोन कॉल तुरंत काट दें।",
                    "यदि ऐप इंस्टॉल होने का संदेह हो, तो तुरंत इंटरनेट बंद करें।",
                    "परिवार के जानकार सदस्य या तकनीशियन से अपना फोन चेक करवाएं।"
                )
                else -> listOf(
                    "आधिकारिक माध्यमों से संदेश भेजने वाले की पुष्टि करें।",
                    "कोई भी कदम उठाने से पहले परिवार के सदस्य से सलाह लें या 1930 पर कॉल करें।",
                    "संदिग्ध संदेश की रिपोर्ट करें।"
                )
            }
            else -> defaultList
        }
    }

    fun getLocalizedWhatNotToDo(context: Context, category: String, classification: String, defaultList: List<String>): List<String> {
        val lang = LocaleHelper.getLanguage(context)
        if (lang == "en") return defaultList

        val cleanCat = category.trim().lowercase()
        return when (lang) {
            "kn" -> when {
                classification == "SAFE" || cleanCat.contains("safe") -> listOf(
                    "ಸಾಮಾನ್ಯ ಸಂಭಾಷಣೆಯಲ್ಲೂ ಅಪರಿಚಿತರೊಂದಿಗೆ ಪಾಸ್‌ವರ್ಡ್ ಅಥವಾ OTP ಹಂಚಿಕೊಳ್ಳಬೇಡಿ."
                )
                cleanCat.contains("upi") -> listOf(
                    "ಹಣ ಸ್ವೀಕರಿಸಲು ಎಂದಿಗೂ UPI PIN ನಮೂದಿಸಬೇಡಿ.",
                    "ಅಪರಿಚಿತ QR ಕೋಡ್‌ಗಳನ್ನು ಸ್ಕ್ಯಾನ್ ಮಾಡಬೇಡಿ.",
                    "PhonePe, GPay ಅಥವಾ Paytm ನಲ್ಲಿ ಕಲೆಕ್ಟ್ ರಿಕ್ವೆಸ್ಟ್‌ಗಳನ್ನು ಒಪ್ಪಬೇಡಿ."
                )
                cleanCat.contains("kyc") || cleanCat.contains("suspension") -> listOf(
                    "ಈ ಸಂದೇಶದಲ್ಲಿರುವ ಯಾವುದೇ ವೆಬ್ ಲಿಂಕ್ ಅನ್ನು ಕ್ಲಿಕ್ ಮಾಡಬೇಡಿ.",
                    "ನಿಮ್ಮ ನೆಟ್‌ಬ್ಯಾಂಕಿಂಗ್ ಪಾಸ್‌ವರ್ಡ್ ಅಥವಾ ಕಾರ್ಡ್ ವಿವರಗಳನ್ನು ನಮೂದಿಸಬೇಡಿ.",
                    "SMS ನಲ್ಲಿ ನೀಡಲಾದ ಫೋನ್ ಸಂಖ್ಯೆಗಳಿಗೆ ಕರೆ ಮಾಡಬೇಡಿ."
                )
                cleanCat.contains("otp") || cleanCat.contains("phishing") -> listOf(
                    "ಯಾವುದೇ ಸಂದರ್ಭದಲ್ಲೂ ನಿಮ್ಮ OTP, PIN ಅಥವಾ CVV ಹಂಚಿಕೊಳ್ಳಬೇಡಿ.",
                    "ಫೋನ್ ಕರೆಯಲ್ಲಿ ಪರಿಶೀಲನಾ ಕೋಡ್‌ಗಳನ್ನು ಓದಿ ಹೇಳಬೇಡಿ.",
                    "ಈ ಸಂದೇಶವನ್ನು ಇತರರಿಗೆ ಫಾರ್ವರ್ಡ್ ಮಾಡಬೇಡಿ."
                )
                cleanCat.contains("electricity") || cleanCat.contains("government") -> listOf(
                    "ವೈಯಕ್ತಿಕ UPI ಖಾತೆಗಳಿಗೆ ವಿದ್ಯುತ್ ಬಿಲ್ ಪಾವತಿಸಬೇಡಿ.",
                    "SMS ಬೆದರಿಕೆಗಳನ್ನು ನೋಡಿ ಭಯಪಡಬೇಡಿ ಅಥವಾ ಆತಂಕಗೊಳ್ಳಬೇಡಿ.",
                    "ಸಂದೇಶದಲ್ಲಿ ನೀಡಲಾದ ವೈಯಕ್ತಿಕ ಮೊಬೈಲ್ ಸಂಖ್ಯೆಗಳಿಗೆ ಕರೆ ಮಾಡಬೇಡಿ."
                )
                cleanCat.contains("lottery") || cleanCat.contains("prize") -> listOf(
                    "ಯಾವುದೇ ರಿಜಿಸ್ಟ್ರೇಷನ್, ಕಸ್ಟಮ್ಸ್ ಅಥವಾ ಪ್ರೊಸೆಸಿಂಗ್ ಶುಲ್ಕ ಪಾವತಿಸಬೇಡಿ.",
                    "ಬಹುಮಾನ ಪಡೆಯಲು ಬ್ಯಾಂಕ್ ಖಾತೆ ಅಥವಾ ಕಾರ್ಡ್ ವಿವರಗಳನ್ನು ಹಂಚಿಕೊಳ್ಳಬೇಡಿ.",
                    "ಅಪರಿಚಿತ ವ್ಯಕ್ತಿಗಳಿಗೆ ಹಣ ವರ್ಗಾಯಿಸಬೇಡಿ."
                )
                cleanCat.contains("delivery") -> listOf(
                    "ಡೆಲಿವರಿ ಮರುಹೊಂದಿಸಲು ಲಿಂಕ್‌ಗಳನ್ನು ಕ್ಲಿಕ್ ಮಾಡಬೇಡಿ.",
                    "ಅಪರಿಚಿತ ಲಿಂಕ್‌ಗಳ ಮೂಲಕ ₹5 ಅಥವಾ ₹10 ಮರು-ವಿತರಣಾ ಶುಲ್ಕವನ್ನು ಪಾವತಿಸಬೇಡಿ.",
                    "ಡೆಲಿವರಿ ಫಾರ್ಮ್‌ಗಳಲ್ಲಿ ಕಾರ್ಡ್ ವಿವರಗಳನ್ನು ನೀಡಬೇಡಿ."
                )
                cleanCat.contains("remote") || cleanCat.contains("anydesk") -> listOf(
                    "AnyDesk, TeamViewer, RustDesk ಅಥವಾ QuickSupport ಇನ್‌ಸ್ಟಾಲ್ ಮಾಡಬೇಡಿ.",
                    "ಸ್ಕ್ರೀನ್-ಹಂಚಿಕೆ ಆಪ್‌ನಲ್ಲಿ ಕಾಣಿಸುವ 9-ಅಂಕಿಯ ಕೋಡ್ ಅನ್ನು ಹಂಚಿಕೊಳ್ಳಬೇಡಿ.",
                    "ಅಪರಿಚಿತ ಫೋನ್ ಕರೆಯಲ್ಲಿರುವಾಗ ಬ್ಯಾಂಕಿಂಗ್ ಆಪ್‌ಗಳನ್ನು ತೆರೆಯಬೇಡಿ."
                )
                else -> listOf(
                    "ಈ ಸಂದೇಶದಲ್ಲಿರುವ ಯಾವುದೇ ಸಂಶಯಾಸ್ಪದ ಲಿಂಕ್‌ಗಳನ್ನು ಕ್ಲಿಕ್ ಮಾಡಬೇಡಿ.",
                    "ನಿಮ್ಮ OTP, ಪಾಸ್‌ವರ್ಡ್ ಅಥವಾ ಬ್ಯಾಂಕಿಂಗ್ ವಿವರಗಳನ್ನು ಹಂಚಿಕೊಳ್ಳಬೇಡಿ.",
                    "ಹಣ ಕಳುಹಿಸಬೇಡಿ ಅಥವಾ ಪಾವತಿ ವಿನಂತಿಗಳನ್ನು ಒಪ್ಪಬೇಡಿ."
                )
            }
            "hi" -> when {
                classification == "SAFE" || cleanCat.contains("safe") -> listOf(
                    "सामान्य बातचीत में भी अजनबियों के साथ पासवर्ड या OTP साझा न करें।"
                )
                cleanCat.contains("upi") -> listOf(
                    "पैसे प्राप्त करने के लिए कभी भी UPI पिन दर्ज न करें।",
                    "अनजान QR कोड स्कैन न करें।",
                    "PhonePe, GPay या Paytm पर कलेक्ट रिक्वेस्ट स्वीकार न करें।"
                )
                cleanCat.contains("kyc") || cleanCat.contains("suspension") -> listOf(
                    "इस संदेश के किसी भी वेब लिंक पर क्लिक न करें।",
                    "अपना नेटबैंकिंग पासवर्ड या कार्ड विवरण दर्ज न करें।",
                    "SMS में दिए गए फोन नंबरों पर कॉल न करें।"
                )
                cleanCat.contains("otp") || cleanCat.contains("phishing") -> listOf(
                    "किसी भी परिस्थिति में अपना OTP, पिन या CVV साझा न करें।",
                    "फोन कॉल पर वेरिफिकेशन कोड बोलकर न बताएं।",
                    "इस संदेश को आगे फॉरवर्ड न करें।"
                )
                cleanCat.contains("electricity") || cleanCat.contains("government") -> listOf(
                    "निजी UPI हैंडल पर बिजली बिल का भुगतान न करें।",
                    "SMS पर बिजली काटने की धमकी से घबराएं नहीं।",
                    "संदेश में दिए गए व्यक्तिगत फोन नंबरों पर कॉल न करें।"
                )
                cleanCat.contains("lottery") || cleanCat.contains("prize") -> listOf(
                    "कोई पंजीकरण, कस्टम या प्रोसेसिंग शुल्क न दें।",
                    "इनाम पाने के लिए बैंक खाता या कार्ड विवरण साझा न करें।",
                    "अज्ञात व्यक्तियों को पैसे ट्रांसफर न करें।"
                )
                cleanCat.contains("delivery") -> listOf(
                    "डिलीवरी दोबारा शेड्यूल करने के लिए लिंक पर क्लिक न करें।",
                    "अनजान लिंक से ₹5 या ₹10 का दोबारा डिलीवरी शुल्क न दें।",
                    "डिलीवरी फॉर्म में कार्ड की जानकारी न भरें।"
                )
                cleanCat.contains("remote") || cleanCat.contains("anydesk") -> listOf(
                    "AnyDesk, TeamViewer, RustDesk या QuickSupport डाउनलोड न करें।",
                    "स्क्रीन-शेयरिंग ऐप में दिखने वाला 9 अंकों का कोड साझा न करें।",
                    "अज्ञात फोन कॉल के दौरान बैंकिंग ऐप न खोलें।"
                )
                else -> listOf(
                    "इस संदेश के किसी भी संदिग्ध लिंक पर क्लिक न करें।",
                    "अपना OTP, पासवर्ड या बैंकिंग विवरण साझा न करें।",
                    "पैसे न भेजें और भुगतान अनुरोध स्वीकार न करें।"
                )
            }
            else -> defaultList
        }
    }

    fun getSafetyTopics(context: Context): List<com.scamshield.app.ui.screens.SafetyTopic> {
        val lang = LocaleHelper.getLanguage(context)
        return when (lang) {
            "kn" -> listOf(
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "OTP ಮತ್ತು ಪಾಸ್‌ವರ್ಡ್ ಕಳ್ಳತನ",
                    rule = "ಕರೆ ಅಥವಾ SMS ಮಾಡುವ ಯಾರಿಗೂ ನಿಮ್ಮ OTP ನೀಡಬೇಡಿ.",
                    fakeExample = "\"ಗ್ರಾಹಕರೇ, ಕಾರ್ಡ್ ಬ್ಲಾಕ್ ಆಗುವುದನ್ನು ತಪ್ಪಿಸಲು 6-ಅಂಕಿಯ OTP ಕೋಡ್ ಹಂಚಿಕೊಳ್ಳಿ.\"",
                    whyDangerous = "ಬ್ಯಾಂಕ್ ಅಧಿಕಾರಿಗಳು ಎಂದಿಗೂ ನಿಮ್ಮ OTP ಕೇಳುವುದಿಲ್ಲ. ಯಾರಾದರೂ ಕೇಳಿದರೆ, ಅವರು ನಿಮ್ಮ ಖಾತೆಯಿಂದ ಹಣ ಕದಿಯಲು ಯತ್ನಿಸುತ್ತಿದ್ದಾರೆ."
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "UPI PIN / ಕಲೆಕ್ಟ್ ರಿಕ್ವೆಸ್ಟ್ ವಂಚನೆ",
                    rule = "ಹಣವನ್ನು ಸ್ವೀಕರಿಸಲು ನೀವು ಎಂದಿಗೂ PIN ನಮೂದಿಸಬೇಕಾಗಿಲ್ಲ.",
                    fakeExample = "\"ರೂ. 5,000 ಕ್ಯಾಶ್‌ಬ್ಯಾಕ್ ಪಡೆಯಲು Google Pay ನಲ್ಲಿ ಅಪ್ರೂವ್ ಒತ್ತಿ ಅಥವಾ UPI PIN ಹಾಕಿ.\"",
                    whyDangerous = "ನಿಮ್ಮ UPI PIN ನಮೂದಿಸಿದರೆ ಹಣ ನಿಮ್ಮ ಖಾತೆಯಿಂದ ಕಡಿತಗೊಳ್ಳುತ್ತದೆ, ಬರುವುದಿಲ್ಲ."
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "ವಿದ್ಯುತ್ ಬಿಲ್ / ಸಂಪರ್ಕ ಕಡಿತದ ಬೆದರಿಕೆ",
                    rule = "ವಿದ್ಯುತ್ ಮಂಡಳಿಯು ಕೆಲವೇ ಗಂಟೆಗಳಲ್ಲಿ SMS ಮೂಲಕ ಪವರ್ ಕಟ್ ಮಾಡುವುದಿಲ್ಲ.",
                    fakeExample = "\"ಬಿಲ್ ಬಾಕಿ ಇರುವ ಕಾರಣ ಇಂದು ರಾತ್ರಿ 9:30 ಕ್ಕೆ ವಿದ್ಯುತ್ ಸಂಪರ್ಕ ಕಡಿತಗೊಳ್ಳುತ್ತದೆ. ಈಗಲೇ ಕರೆ ಮಾಡಿ.\"",
                    whyDangerous = "ವಂಚಕರು ಸುಳ್ಳು ಆತಂಕ ಹುಟ್ಟಿಸಿ ಹಣ ವಸೂಲಿ ಮಾಡಲು ಅಥವಾ ಅಪಾಯಕಾರಿ ಆಪ್ ಹಾಕಿಸಲು ನೋಡುತ್ತಾರೆ."
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "ನಕಲಿ KYC & ಖಾತೆ ಬ್ಲಾಕ್ ವಂಚನೆ",
                    rule = "SMS ಲಿಂಕ್‌ಗಳ ಮೂಲಕ ಎಂದಿಗೂ KYC ಅಪ್‌ಡೇಟ್ ಮಾಡಬೇಡಿ.",
                    fakeExample = "\"ನಿಮ್ಮ SBI ಖಾತೆ ಬ್ಲಾಕ್ ಆಗಿದೆ. ಆಧಾರ್ & PAN ನವೀಕರಿಸಲು http://sbi-kyc.xyz ಕ್ಲಿಕ್ ಮಾಡಿ.\"",
                    whyDangerous = "ಈ ಲಿಂಕ್ ನಕಲಿ ವೆಬ್‌ಸೈಟ್ ತೆರೆದು ನಿಮ್ಮ ನೆಟ್‌ಬ್ಯಾಂಕಿಂಗ್ ಪಾಸ್‌ವರ್ಡ್ ಮತ್ತು PAN ವಿವರಗಳನ್ನು ಕದಿಯುತ್ತದೆ."
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "ಲಾಟರಿ & ಲಕ್ಕಿ ಡ್ರಾ ವಂಚನೆ",
                    rule = "ನೀವು ಟಿಕೆಟ್ ಖರೀದಿಸದಿದ್ದರೆ, ನೀವು ಗೆಲ್ಲಲು ಸಾಧ್ಯವಿಲ್ಲ.",
                    fakeExample = "\"ಅಭಿನಂದನೆಗಳು! ನೀವು 25 ಲಕ್ಷ ಗೆದ್ದಿದ್ದೀರಿ. ಕ್ಲೈಮ್ ಮಾಡಲು ರೂ. 5,000 ನೋಂದಣಿ ಶುಲ್ಕ ಕಟ್ಟಿ.\"",
                    whyDangerous = "ವಂಚಕರು ಮುಂಗಡ ಶುಲ್ಕ ಅಥವಾ ತೆರಿಗೆ ಹೆಸರಲ್ಲಿ ಹಣ ಪಡೆದು ಪರಾರಿಯಾಗುತ್ತಾರೆ."
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "ರಿಮೋಟ್ ಕಂಟ್ರೋಲ್ ಆಪ್‌ಗಳು (AnyDesk / TeamViewer)",
                    rule = "ಅಪರಿಚಿತರ ಸೂಚನೆಯಂತೆ ಎಂದಿಗೂ ಸ್ಕ್ರೀನ್-ಹಂಚಿಕೆ ಆಪ್ ಇನ್‌ಸ್ಟಾಲ್ ಮಾಡಬೇಡಿ.",
                    fakeExample = "\"ಬ್ಯಾಂಕ್ ಗ್ರಾಹಕ ಸೇವೆ ಸಹಾಯ ಮಾಡಲು Play Store ನಿಂದ AnyDesk ಆಪ್ ಇನ್‌ಸ್ಟಾಲ್ ಮಾಡಿ.\"",
                    whyDangerous = "ಈ ಆಪ್‌ಗಳು ವಂಚಕರಿಗೆ ನಿಮ್ಮ ಫೋನ್ ಸ್ಕ್ರೀನ್ ನೋಡಲು ಮತ್ತು ಬ್ಯಾಂಕಿಂಗ್ ಆಪ್‌ಗಳನ್ನು ನಿಯಂತ್ರಿಸಲು ಅನುವು ಮಾಡಿಕೊಡುತ್ತವೆ."
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "ನಕಲಿ ಕೊರಿಯರ್ / ಪಾರ್ಸೆಲ್ ವಂಚನೆ",
                    rule = "ಅಧಿಕೃತ ಆಪ್‌ಗಳ ಒಳಗೆ ಮಾತ್ರ (Amazon, Flipkart) ಪಾರ್ಸೆಲ್ ವಿವರ ಪರಿಶೀಲಿಸಿ.",
                    fakeExample = "\"IndiaPost: ಪಾರ್ಸೆಲ್ ಡೆಲಿವರಿ ಆಗಿಲ್ಲ. ಈ ಲಿಂಕ್ ಬಳಸಿ ರೂ. 48 ಮರು-ವಿತರಣಾ ಶುಲ್ಕ ಕಟ್ಟಿ.\"",
                    whyDangerous = "ಚಿಕ್ಕ ಮೊತ್ತದ ಪಾವತಿ ಲಿಂಕ್‌ಗಳು ಕ್ರೆಡಿಟ್ ಕಾರ್ಡ್ ಮತ್ತು ನೆಟ್‌ಬ್ಯಾಂಕಿಂಗ್ ವಿವರಗಳನ್ನು ಕದಿಯುತ್ತವೆ."
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "ಮನೆಯಿಂದಲೇ ಕೆಲಸ / ನಕಲಿ ಉದ್ಯೋಗ ವಂಚನೆ",
                    rule = "ನೈಜ ಉದ್ಯೋಗದಾತರು ಕೆಲಸ ನೀಡಲು ನಿಮ್ಮಿಂದ ಎಂದಿಗೂ ಹಣ ಪಡೆಯುವುದಿಲ್ಲ.",
                    fakeExample = "\"YouTube ವೀಡಿಯೊ ಲೈಕ್ ಮಾಡಿ ದಿನಕ್ಕೆ ರೂ. 5,000 ಗಳಿಸಿ. ಆರಂಭಿಸಲು ರೂ. 1,000 ಸೇರ್ಪಡೆ ಶುಲ್ಕ ಕಟ್ಟಿ.\"",
                    whyDangerous = "ಹಣವನ್ನು ಠೇವಣಿ ಮಾಡಿಸಿಕೊಂಡು ನಂತರ ಹಿಂಪಡೆಯಲು ಸಾಧ್ಯವಾಗದಂತೆ ಮೋಸ ಮಾಡಲಾಗುತ್ತದೆ."
                )
            )
            "hi" -> listOf(
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "OTP और पासवर्ड धोखाधड़ी",
                    rule = "कॉल या मैसेज करने वाले किसी भी व्यक्ति को अपना OTP न दें।",
                    fakeExample = "\"प्रिय ग्राहक, कार्ड ब्लॉक होने से बचाने के लिए 6 अंकों का OTP कोड साझा करें।\"",
                    whyDangerous = "बैंक अधिकारी कभी भी OTP नहीं मांगते। यदि कोई मांगता है, तो वह आपके खाते से पैसे चुराने की कोशिश कर रहा है।"
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "UPI पिन / कलेक्ट रिक्वेस्ट धोखाधड़ी",
                    rule = "पैसे प्राप्त करने के लिए आपको कभी भी पिन डालने की आवश्यकता नहीं होती।",
                    fakeExample = "\"₹5,000 कैशबैक प्राप्त करने के लिए Google Pay पर अप्रूव करें या UPI पिन दर्ज करें।\"",
                    whyDangerous = "UPI पिन दर्ज करने से हमेशा आपके खाते से पैसे कटते हैं, आते कभी नहीं।"
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "बिजली काटने की फर्जी धमकी",
                    rule = "बिजली विभाग कुछ ही घंटों में SMS भेजकर बिजली नहीं काटता।",
                    fakeExample = "\"बिल जमा न होने के कारण आज रात 9:30 बजे बिजली काट दी जाएगी। अधिकारी से तुरंत संपर्क करें।\"",
                    whyDangerous = "धोखेबाज डर का माहौल बनाकर आपसे पैसे ऐंठने या खतरनाक ऐप डाउनलोड कराने की कोशिश करते हैं।"
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "फर्जी KYC और खाता बंद होने की धमकी",
                    rule = "SMS लिंक के जरिए कभी भी KYC अपडेट न करें।",
                    fakeExample = "\"आपका SBI खाता ब्लॉक कर दिया गया है। आधार और पैन लिंक करने के लिए http://sbi-kyc.xyz पर क्लिक करें।\"",
                    whyDangerous = "यह लिंक एक फर्जी वेबसाइट खोलता है जो आपका नेट-बैंकिंग पासवर्ड और कार्ड विवरण चुरा लेती है।"
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "लॉटरी और लकी ड्रा धोखाधड़ी",
                    rule = "यदि आपने टिकट नहीं लिया, तो आप कभी नहीं जीत सकते।",
                    fakeExample = "\"बधाई हो! आपने ₹25 लाख जीते हैं। इनाम पाने के लिए ₹5,000 पंजीकरण शुल्क का भुगतान करें।\"",
                    whyDangerous = "धोखेबाज प्रोसेसिंग फीस के नाम पर अग्रिम भुगतान लेते हैं और फिर गायब हो जाते हैं।"
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "रिमोट कंट्रोल ऐप्स (AnyDesk / TeamViewer)",
                    rule = "अजनबी के कहने पर कभी भी स्क्रीन-शेयरिंग ऐप डाउनलोड न करें।",
                    fakeExample = "\"बैंक सहायता प्राप्त करने के लिए प्ले स्टोर से AnyDesk ऐप इंस्टॉल करें।\"",
                    whyDangerous = "ये ऐप धोखेबाज को आपकी फोन स्क्रीन देखने और आपके बैंक खातों से पैसे निकालने की अनुमति देते हैं।"
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "फर्जी पार्सल और कूरियर धोखाधड़ी",
                    rule = "आधिकारिक ऐप (Amazon, Flipkart) के अंदर ही डिलीवरी की जानकारी जांचें।",
                    fakeExample = "\"IndiaPost: पार्सल डिलीवर नहीं हुआ। दोबारा डिलीवरी के लिए इस लिंक से ₹48 का भुगतान करें।\"",
                    whyDangerous = "छोटे भुगतान के लिंक आपके क्रेडिट कार्ड और नेट-बैंकिंग का पासवर्ड चुरा लेते हैं।"
                ),
                com.scamshield.app.ui.screens.SafetyTopic(
                    title = "घर बैठे नौकरी और पार्ट-टाइम जॉब स्कैम",
                    rule = "असली कंपनियां नौकरी देने के लिए कभी पैसे नहीं मांगती हैं।",
                    fakeExample = "\"YouTube वीडियो लाइक करके रोज ₹5,000 कमाएं। शुरू करने के लिए ₹1,000 का शुल्क दें।\"",
                    whyDangerous = "पीड़ितों को पैसे जमा कराने के जाल में फंसाया जाता है जहां से पैसे कभी वापस नहीं मिलते।"
                )
            )
            else -> com.scamshield.app.ui.screens.SAFETY_TOPICS
        }
    }
}
