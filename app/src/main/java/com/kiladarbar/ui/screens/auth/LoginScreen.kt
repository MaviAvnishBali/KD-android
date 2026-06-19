package com.kiladarbar.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kiladarbar.BuildConfig
import com.kiladarbar.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onOtpSent: (String) -> Unit,
    onLoggedIn: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(uiState.otpSent) {
        if (uiState.otpSent) onOtpSent("+91$phone")
    }
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoggedIn()
    }

    fun launchGoogleSignIn() {
        scope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.googleLogin(googleCredential.idToken)
                }
            } catch (_: GetCredentialCancellationException) {
                // user cancelled
            } catch (e: Exception) {
                viewModel.clearError()
            }
        }
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(RoyalGold, shape = RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "کِلا",
                    style = MaterialTheme.typography.displaySmall,
                    color = RoyalDark,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("Kila Darbar", style = MaterialTheme.typography.displaySmall, color = RoyalGold, fontWeight = FontWeight.Bold)
            Text("Royal Mughal Cuisine", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.7f))

            Spacer(Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("Welcome!", style = MaterialTheme.typography.titleLarge, color = RoyalMaroon, fontWeight = FontWeight.Bold)
                    Text(
                        "Sign in or create an account to enjoy royal dining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                    )

                    HorizontalDivider(color = Color(0xFFF0E8E8))

                    // Phone field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) phone = it.filter(Char::isDigit) },
                        label = { Text("Mobile Number") },
                        placeholder = { Text("10-digit number") },
                        leadingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 12.dp),
                            ) {
                                Text("+91", color = RoyalMaroon, fontWeight = FontWeight.SemiBold)
                                VerticalDivider(
                                    modifier = Modifier.height(24.dp).padding(horizontal = 8.dp),
                                    color = Color.LightGray,
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (phone.length == 10) viewModel.sendOtp("+91$phone")
                        }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalMaroon,
                            focusedLabelColor = RoyalMaroon,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.error != null,
                        supportingText = uiState.error?.let { msg -> { Text(msg, color = MaterialTheme.colorScheme.error) } },
                    )

                    AnimatedVisibility(visible = uiState.error != null && !uiState.otpSent) {
                        Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.sendOtp("+91$phone")
                        },
                        enabled = phone.length == 10 && !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Get OTP", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text("  or  ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    // Google button
                    OutlinedButton(
                        onClick = ::launchGoogleSignIn,
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                    ) {
                        // Google "G" logo approximation using text
                        Text(
                            "G",
                            color = Color(0xFF4285F4),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Continue with Google", color = Color.DarkGray, fontWeight = FontWeight.Medium)
                    }

                    TextButton(onClick = { /* guest */ }) {
                        Text("Continue as Guest", color = RoyalMaroon.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "By continuing, you agree to our Terms of Service and Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
