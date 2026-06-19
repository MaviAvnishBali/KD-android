package com.kiladarbar.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiladarbar.ui.theme.*

@Composable
fun OtpScreen(
    phone: String,
    onVerified: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var otp by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onVerified()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(RoyalMaroon, RoyalMaroonDark, RoyalDark)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Verify OTP", style = MaterialTheme.typography.displaySmall, color = RoyalGold, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "OTP sent to $phone",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
            Text(
                "Valid for 5 minutes",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
            )

            Spacer(Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Text("Enter OTP", style = MaterialTheme.typography.titleMedium, color = RoyalMaroon, fontWeight = FontWeight.SemiBold)

                    OtpBoxInput(
                        otp = otp,
                        onOtpChange = { otp = it },
                    )

                    if (uiState.error != null) {
                        Text(
                            uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Button(
                        onClick = { viewModel.verifyOtp(phone, otp) },
                        enabled = otp.length == 6 && !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Verify & Login", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    // Resend
                    val canResend = uiState.resendSecondsLeft == 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Didn't receive? ", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        if (canResend) {
                            TextButton(
                                onClick = { viewModel.resendOtp(phone) },
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text("Resend OTP", color = RoyalMaroon, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Text(
                                "Resend in ${uiState.resendSecondsLeft}s",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpBoxInput(
    otp: String,
    onOtpChange: (String) -> Unit,
    length: Int = 6,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Hidden real input
        BasicTextField(
            value = otp,
            onValueChange = { if (it.length <= length) onOtpChange(it.filter(Char::isDigit)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .alpha(0.01f) // nearly invisible but still receives keyboard
                .height(1.dp),
        )

        // Visual boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(length) { index ->
                val digit = otp.getOrNull(index)?.toString() ?: ""
                val isCurrent = index == otp.length
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .border(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = when {
                                isCurrent -> RoyalMaroon
                                digit.isNotEmpty() -> RoyalMaroonDark
                                else -> Color(0xFFDDDDDD)
                            },
                            shape = RoundedCornerShape(10.dp),
                        )
                        .background(
                            color = if (digit.isNotEmpty()) Color(0xFFFCF0F0) else Color.White,
                            shape = RoundedCornerShape(10.dp),
                        )
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (digit.isNotEmpty()) {
                        Text(
                            text = digit,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon,
                        )
                    } else if (isCurrent) {
                        // Cursor indicator
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(24.dp)
                                .background(RoyalMaroon)
                        )
                    }
                }
            }
        }
    }
}
