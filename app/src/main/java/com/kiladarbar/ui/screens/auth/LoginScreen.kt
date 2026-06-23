package com.kiladarbar.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kiladarbar.BuildConfig
import com.kiladarbar.ui.components.EyebrowLabel
import com.kiladarbar.ui.components.GoldButton
import com.kiladarbar.ui.components.GoldParticleCanvas
import com.kiladarbar.ui.components.GoldDivider
import com.kiladarbar.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onOtpSent: (String) -> Unit,
    onLoggedIn: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf("") }

    /* ── Navigate on state changes ── */
    LaunchedEffect(uiState.otpSent)  { if (uiState.otpSent)   onOtpSent("+91$phone") }
    LaunchedEffect(uiState.isLoggedIn) { if (uiState.isLoggedIn) onLoggedIn() }

    /* ── Entrance animations ── */
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val cardY     = remember { Animatable(60f) }
    val cardAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(500))
        logoScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow))
        delay(200)
        cardAlpha.animateTo(1f, tween(500))
        cardY.animateTo(0f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow))
    }

    fun idTokenFrom(credential: androidx.credentials.Credential): String? =
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        else null

    fun launchGoogleSignIn() {
        scope.launch {
            val credentialManager = CredentialManager.create(activity)

            // Step 1 — bottom-sheet picker (fast path, previously used accounts)
            val idTokenFast: String? = try {
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                            .setAutoSelectEnabled(false)
                            .build()
                    )
                    .build()
                idTokenFrom(credentialManager.getCredential(context = activity, request = request).credential)
            } catch (_: GetCredentialCancellationException) {
                return@launch   // user pressed Back — do nothing
            } catch (_: NoCredentialException) {
                null            // no saved account — fall through to full picker
            } catch (_: GetCredentialException) {
                null
            } catch (_: Exception) {
                null
            }

            if (idTokenFast != null) {
                viewModel.googleLogin(idTokenFast)
                return@launch
            }

            // Step 2 — full Google account chooser dialog
            try {
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build()
                    )
                    .build()
                val idToken = idTokenFrom(credentialManager.getCredential(context = activity, request = request).credential)
                if (idToken != null) {
                    viewModel.googleLogin(idToken)
                } else {
                    viewModel.setError("Google sign-in failed. Please try again.")
                }
            } catch (_: GetCredentialCancellationException) {
                // dismissed
            } catch (e: Exception) {
                viewModel.setError("Google sign-in error: ${e.message}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian),
    ) {
        /* ── Background particles ── */
        GoldParticleCanvas(
            modifier     = Modifier.fillMaxSize(),
            count        = 40,
            canvasWidth  = 400.dp,
            canvasHeight = 800.dp,
        )

        /* ── Maroon radial glow ── */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Maroon.copy(alpha = 0.35f), Obsidian.copy(alpha = 0f)),
                        radius = 900f,
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(56.dp))

            /* ── Logo seal ── */
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
            ) {
                /* Outer glow ring */
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(Gold.copy(0.15f), Color.Transparent))
                        )
                )
                /* Gold circle */
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(LuxuryGradients.goldHorizontal),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = "KD",
                        fontSize   = 28.sp,
                        color      = Obsidian,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            /* Brand name */
            Text(
                text          = "KILA DARBAR",
                color         = Ivory,
                fontSize      = 26.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 5.sp,
                modifier      = Modifier.alpha(logoAlpha.value),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text          = "ROYAL MUGHAL CUISINE",
                color         = Gold,
                fontSize      = 10.sp,
                letterSpacing = 3.sp,
                modifier      = Modifier.alpha(logoAlpha.value),
            )

            Spacer(Modifier.height(40.dp))

            /* ── Login card ── */
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
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                EyebrowLabel("Sign In / Register")
                Text(
                    text      = "Welcome to a royal experience",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = IvoryDim.copy(0.6f),
                    textAlign = TextAlign.Center,
                )

                GoldDivider()

                /* Phone field */
                PhoneField(
                    phone     = phone,
                    onchange  = { phone = it },
                    onSend    = { if (phone.length == 10) { focusManager.clearFocus(); viewModel.startPhoneVerification("+91$phone", activity) } },
                    hasError  = uiState.error != null,
                    errorMsg  = uiState.error,
                )

                /* OTP button */
                GoldButton(
                    text     = if (uiState.isLoading) "Sending…" else "Get OTP",
                    onClick  = { focusManager.clearFocus(); viewModel.startPhoneVerification("+91$phone", activity) },
                    enabled  = phone.length == 10 && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )

                /* Divider */
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).height(1.dp).background(GoldBorder))
                    Text("  or  ", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.4f))
                    Box(Modifier.weight(1f).height(1.dp).background(GoldBorder))
                }

                /* Google sign-in */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            width = 1.dp,
                            color = if (!uiState.isLoading) GoldBorder else Gold.copy(0.4f),
                            shape = RoundedCornerShape(14.dp),
                        )
                        .background(Surface2)
                        .clickable(enabled = !uiState.isLoading) { launchGoogleSignIn() }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (uiState.isLoading) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier  = Modifier.size(18.dp),
                                color     = Gold,
                                strokeWidth = 2.dp,
                            )
                            Text(
                                "Signing in with Google…",
                                style      = MaterialTheme.typography.labelLarge,
                                color      = IvoryDim.copy(0.6f),
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            // Google "G" coloured icon
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("G", fontSize = 13.sp, color = Color(0xFF4285F4), fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text       = "Continue with Google",
                                style      = MaterialTheme.typography.labelLarge,
                                color      = Ivory,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                GoldDivider()

                /* ── Guest CTA — the main focus ── */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Maroon.copy(0.25f), MaroonDark.copy(0.25f))
                            )
                        )
                        .border(1.dp, Maroon.copy(0.5f), RoundedCornerShape(14.dp))
                        .clickable { viewModel.continueAsGuest() }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("👤", fontSize = 16.sp)
                        Text(
                            text       = "Explore as Guest",
                            style      = MaterialTheme.typography.labelLarge,
                            color      = Ivory,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Text(
                    text      = "Guest access lets you browse the menu, view our story,\nand place an order. Sign in anytime for loyalty points.",
                    style     = MaterialTheme.typography.labelSmall,
                    color     = IvoryDim.copy(0.4f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text      = "By continuing you agree to our Terms & Privacy Policy",
                style     = MaterialTheme.typography.labelSmall,
                color     = IvoryDim.copy(0.3f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PhoneField(
    phone: String,
    onchange: (String) -> Unit,
    onSend: () -> Unit,
    hasError: Boolean,
    errorMsg: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Surface2)
                .border(
                    1.dp,
                    if (hasError) Color(0xFFCF6679).copy(0.7f) else GoldBorder,
                    RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Filled.Phone, null, tint = Gold, modifier = Modifier.size(18.dp))
            Text("+91", style = MaterialTheme.typography.labelLarge, color = Gold, fontWeight = FontWeight.Bold)
            Box(Modifier.width(1.dp).height(20.dp).background(GoldBorder))
            BasicTextField(
                value         = phone,
                onValueChange = { if (it.length <= 10) onchange(it.filter(Char::isDigit)) },
                modifier      = Modifier.weight(1f),
                textStyle     = MaterialTheme.typography.bodyLarge.copy(color = Ivory),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSend() }),
                singleLine    = true,
                decorationBox = { inner ->
                    Box {
                        if (phone.isEmpty()) {
                            Text("10-digit mobile number", style = MaterialTheme.typography.bodyLarge, color = IvoryDim.copy(0.35f))
                        }
                        inner()
                    }
                },
            )
            if (phone.isNotEmpty()) {
                Text(
                    text  = "${phone.length}/10",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (phone.length == 10) Gold else IvoryDim.copy(0.4f),
                )
            }
        }

        AnimatedVisibility(visible = hasError && errorMsg != null) {
            Text(
                text  = errorMsg ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFCF6679),
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}
