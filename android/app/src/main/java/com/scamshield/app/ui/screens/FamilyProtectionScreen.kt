package com.scamshield.app.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scamshield.app.ui.theme.*

@Composable
fun FamilyProtectionScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("scamshield_prefs", Context.MODE_PRIVATE) }

    var isFamilyEnabled by remember {
        mutableStateOf(prefs.getBoolean("family_protection_enabled", false))
    }
    var trustedName by remember {
        mutableStateOf(prefs.getString("trusted_contact_name", "") ?: "")
    }
    var trustedPhone by remember {
        mutableStateOf(prefs.getString("trusted_contact_phone", "") ?: "")
    }
    var savedMessage by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppTheme.colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Family Protection",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AccentEmerald,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Protect Your Parents",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "When an elderly parent receives a confirmed high-risk scam or fraudulent payment demand, SCAMSHIELD can notify a designated family member so they can intervene before money is lost.",
                        fontSize = 15.sp,
                        color = AppTheme.colors.textSecondary,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Family Alerts",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.textPrimary
                        )
                        Switch(
                            checked = isFamilyEnabled,
                            onCheckedChange = { checked ->
                                isFamilyEnabled = checked
                                prefs.edit().putBoolean("family_protection_enabled", checked).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppTheme.colors.background,
                                checkedTrackColor = AccentEmerald
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isFamilyEnabled) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Trusted Family Contact",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = trustedName,
                            onValueChange = { trustedName = it },
                            label = { Text("Contact Name (e.g. Son, Daughter)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AccentEmerald) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary,
                                focusedBorderColor = AccentEmerald,
                                unfocusedBorderColor = AppTheme.colors.border
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = trustedPhone,
                            onValueChange = { trustedPhone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary,
                                focusedBorderColor = AccentEmerald,
                                unfocusedBorderColor = AppTheme.colors.border
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                prefs.edit()
                                    .putString("trusted_contact_name", trustedName)
                                    .putString("trusted_contact_phone", trustedPhone)
                                    .apply()
                                savedMessage = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                        ) {
                            Text(
                                "SAVE TRUSTED CONTACT",
                                fontWeight = FontWeight.Bold,
                                color = if (AppTheme.colors.isDark) SurfaceDark else androidx.compose.ui.graphics.Color(0xFF0F172A)
                            )
                        }

                        if (savedMessage) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ Trusted contact updated successfully.",
                                color = AccentEmerald,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Privacy Guarantee
            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AccentEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Zero Surveillance: Family members are never shown message chats or personal text. They only receive a safety notice: 'Your parent received a high-risk scam alert.'",
                        fontSize = 13.sp,
                        color = AppTheme.colors.textSecondary,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}
