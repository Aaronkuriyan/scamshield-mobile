package com.scamshield.app.service

import android.content.Context
import android.provider.Telephony

/**
 * Filter and classifier for supported messaging applications.
 * Cleanly abstracts package detection so additional messaging apps can be supported in future.
 */
enum class SupportedApp(val displayName: String, val packageNames: Set<String>) {
    WHATSAPP("WhatsApp", setOf("com.whatsapp", "com.whatsapp.w4b")),
    SMS("SMS", emptySet())
}

object MessageSourceFilter {

    /**
     * Resolves the incoming package name to a supported messaging source, or null if ignored.
     */
    fun fromPackageName(context: Context, packageName: String): SupportedApp? {
        if (SupportedApp.WHATSAPP.packageNames.contains(packageName)) {
            return SupportedApp.WHATSAPP
        }

        // Check if matches default SMS app
        try {
            val defaultSms = Telephony.Sms.getDefaultSmsPackage(context)
            if (defaultSms != null && packageName == defaultSms) {
                return SupportedApp.SMS
            }
        } catch (_: Exception) {
            // Ignore telephony resolution failure
        }

        // Check known SMS / MMS package patterns
        if (isKnownSmsPackage(packageName)) {
            return SupportedApp.SMS
        }

        return null
    }

    private fun isKnownSmsPackage(pkg: String): Boolean {
        return when {
            pkg == "com.google.android.apps.messaging" -> true
            pkg == "com.android.mms" -> true
            pkg == "com.samsung.android.messaging" -> true
            pkg == "com.xiaomi.mms" -> true
            pkg.contains(".mms") -> true
            pkg.contains(".messaging") -> true
            else -> false
        }
    }
}
