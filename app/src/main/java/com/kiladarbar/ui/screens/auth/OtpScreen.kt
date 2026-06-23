package com.kiladarbar.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.activity.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiladarbar.ui.components.GoldButton
import com.kiladarbar.ui.components.GoldParticleCanvas
import com.kiladarbar.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OtpScreen(
    phone:      String,
    onVerified: () -> Unit,
    onBack:     () -> Unit,
    viewModel:  AuthViewModel = hiltViewModel(),
) {
    val uiState  by viewModel.uiState.collectAsState()
    val context  = LocalContext.current
    val activity = context as ComponentActivity
    var otp      by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onVerified()
    }

    // Entrance animations
    val cardY     = remember { Animatable(48f) }
    val cardAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        cardAlpha.animateTo(1f, tween(500))
        cardY.animateTo(0f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow))
    }

    Box(modifier = Modifier.fillMaxSize().background(Obsidian)) {
        // Particles
        GoldParticleCanvas(modifier = Modifier.fillMaxSize(), count = 35, canvasWidth = 400.dp, canvasHeight = 800.dp)

        // Radial glow
        Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Maroon.copy(0.35f), Color.Transparent), radius = 900f)))

        Column(
            modifier            = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))

            // Back button
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { viewModel.resetOtpSent(); onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ivory)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Shield icon
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape)
                    .background(LuxuryGradients.goldHorizontal),
                contentAlignment = Alignment.Center,
            ) {
                Text("🔒", fontSize = 36.sp)
            }

            Spacer(Modifier.height(20.dp))

            Text("Verify Your Number", style = MaterialTheme.typography.headlineMedium, color = Ivory, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "We sent a 6-digit code to",
                style = MaterialTheme.typography.bodyMedium,
                color = IvoryDim.copy(0.6f),
            )
            Text(
                phone,
                style      = MaterialTheme.typography.titleMedium,
                color      = Gold,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(36.dp))

            // OTP card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(cardAlpha.value)
                    .offset(y = cardY.value.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Surface1)
                    .border(1.dp, GoldBorder, RoundedCornerShape(28.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                EyebrowLabel("Enter OTP")

                // 6-box OTP input
                OtpBoxRow(
                    otp         = otp,
                    onOtpChange = { otp = it },
                )

                // Error
                AnimatedVisibility(uiState.error != null) {
                    Text(
                        uiState.error ?: "",
                        color     = Color(0xFFCF6679),
                        style     = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }

                // Verify button
                GoldButton(
                    text     = if (uiState.isLoading) "Verifying…" else "Verify & Login",
                    onClick  = { viewModel.verifyPhoneCode(otp) },
                    enabled  = otp.length == 6 && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Resend row
                val canResend = uiState.resendSecondsLeft == 0 && !uiState.isLoading
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("Didn't receive it? ", style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.5f))
                    if (canResend) {
                        Text(
                            "Resend OTP",
                            style     = MaterialTheme.typography.bodySmall,
                            color     = Gold,
                            fontWeight = FontWeight.SemiBold,
                            modifier  = Modifier.clickable {
                                otp = ""
                                viewModel.startPhoneVerification(phone, activity)
                            },
                        )
                    } else {
                        Text(
                            "Resend in ${uiState.resendSecondsLeft}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = IvoryDim.copy(0.4f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "⚡ Powered by Firebase Authentication",
                style  = MaterialTheme.typography.labelSmall,
                color  = IvoryDim.copy(0.25f),
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun OtpBoxRow(otp: String, onOtpChange: (String) -> Unit, length: Int = 6) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { delay(300); focusRequester.requestFocus() }

    Box(modifier = Modifier.fillMaxWidth()) {
        BasicTextField(
            value           = otp,
            onValueChange   = { if (it.length <= length) onOtpChange(it.filter(Char::isDigit)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier        = Modifier.fillMaxWidth().focusRequester(focusRequester).alpha(0.01f).height(1.dp),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(length) { i ->
                val digit     = otp.getOrNull(i)?.toString() ?: ""
                val isCurrent = i == otp.length
                val scale by animateFloatAsState(
                    if (digit.isNotEmpty()) 1.08f else 1f,
                    spring(Spring.DampingRatioMediumBouncy),
                    label = "boxScale",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (digit.isNotEmpty()) Gold.copy(0.12f) else Surface2)
                        .border(
                            width  = if (isCurrent) 1.5.dp else 1.dp,
                            color  = when { isCurrent -> Gold; digit.isNotEmpty() -> Gold.copy(0.6f); else -> GoldBorder },
                            shape  = RoundedCornerShape(12.dp),
                        )
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (digit.isNotEmpty()) {
                        Text(digit, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Gold)
                    } else if (isCurrent) {
                        Box(Modifier.width(2.dp).height(24.dp).background(Gold))
                    }
                }
            }
        }
    }
}

@Composable
private fun EyebrowLabel(text: String) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelSmall,
        color         = Gold,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 2.sp,
    )
}
