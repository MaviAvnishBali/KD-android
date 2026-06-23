package com.kiladarbar.data.firebase

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed class PhoneVerificationResult {
    data class CodeSent(
        val verificationId: String,
        val resendToken:    PhoneAuthProvider.ForceResendingToken,
    ) : PhoneVerificationResult()

    data class AutoVerified(val credential: PhoneAuthCredential) : PhoneVerificationResult()
    data class Failed(val message: String)                       : PhoneVerificationResult()
}

@Singleton
class FirebaseAuthManager @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()

    /* ── Google Sign-In ── */

    /**
     * Exchange a Google ID token (from Credential Manager) for a Firebase ID token.
     */
    suspend fun signInWithGoogle(googleIdToken: String): String {
        val credential  = GoogleAuthProvider.getCredential(googleIdToken, null)
        val authResult  = auth.signInWithCredential(credential).await()
        val firebaseToken = authResult.user?.getIdToken(false)?.await()?.token
            ?: throw Exception("Failed to get Firebase ID token from Google sign-in")
        return firebaseToken
    }

    /* ── Phone Auth — Step 1: trigger SMS ── */

    fun startPhoneVerification(
        phone:    String,
        activity: Activity,
        onResult: (PhoneVerificationResult) -> Unit,
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onResult(PhoneVerificationResult.AutoVerified(credential))
            }
            override fun onVerificationFailed(e: FirebaseException) {
                onResult(PhoneVerificationResult.Failed(e.message ?: "Phone verification failed"))
            }
            override fun onCodeSent(
                verificationId: String,
                resendToken:    PhoneAuthProvider.ForceResendingToken,
            ) {
                onResult(PhoneVerificationResult.CodeSent(verificationId, resendToken))
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /* ── Phone Auth — Step 2: verify OTP code ── */

    suspend fun verifyPhoneCode(verificationId: String, smsCode: String): String {
        val credential    = PhoneAuthProvider.getCredential(verificationId, smsCode)
        return signInWithPhoneCredential(credential)
    }

    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): String {
        val authResult    = auth.signInWithCredential(credential).await()
        val firebaseToken = authResult.user?.getIdToken(false)?.await()?.token
            ?: throw Exception("Failed to get Firebase ID token after phone sign-in")
        return firebaseToken
    }
}
