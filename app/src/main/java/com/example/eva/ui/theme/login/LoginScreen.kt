package com.eva.app.ui.login

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.app.R
import com.eva.app.data.repository.AuthRepository
import com.eva.app.utils.Constants
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/*
================================================================================
LOGIN SCREEN
================================================================================

PURPOSE:
    Allows users to sign in with their Google account.

GOOGLE SIGN-IN FLOW:
    1. User taps "Sign in with Google" button
    2. Google One Tap UI appears (or account picker)
    3. User selects their Google account
    4. We receive a Google ID token
    5. Send token to our backend (/auth/login or /auth/register)
    6. Backend verifies token with Google
    7. Backend returns our JWT access token
    8. We save the token and navigate to chat

GOOGLE IDENTITY SERVICES:
    We use the modern Google Identity Services library (One Tap).
    This provides a better UX than the legacy Google Sign-In.

ERROR HANDLING:
    - Network errors: Show retry option
    - Sign-in cancelled: Allow user to try again
    - Backend errors: Show error message

================================================================================
*/

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit
) {
    /*
    ================================================================================
    CONTEXT & COROUTINES
    ================================================================================
    */

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    /*
    ================================================================================
    GOOGLE SIGN-IN CLIENT
    ================================================================================
    */

    // Google Identity Services client
    val oneTapClient: SignInClient = remember {
        Identity.getSignInClient(context)
    }

    // Sign-in request configuration
    val signInRequest: BeginSignInRequest = remember {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Use the Web Client ID (not Android client ID!)
                    .setServerClientId(Constants.GOOGLE_WEB_CLIENT_ID)
                    // Show all accounts, not just those already signed in
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            // Automatically select account if only one exists
            .setAutoSelectEnabled(true)
            .build()
    }

    /*
    ================================================================================
    STATE
    ================================================================================
    */

    // Loading state - true while signing in
    var isLoading by remember { mutableStateOf(false) }

    // Error message to display
    var errorMessage by remember { mutableStateOf<String?>(null) }

    /*
    ================================================================================
    ACTIVITY RESULT LAUNCHER
    ================================================================================
    */

    // Launcher for Google Sign-In intent
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // User completed sign-in
            scope.launch {
                try {
                    // Extract the credential from the result
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken

                    if (idToken != null) {
                        // Send token to our backend
                        isLoading = true
                        errorMessage = null

                        val authResult = authRepository.authenticateWithGoogle(idToken)

                        authResult.onSuccess {
                            // Login successful - navigate to chat
                            isLoading = false
                            onLoginSuccess()
                        }.onFailure { error ->
                            // Login failed
                            isLoading = false
                            errorMessage = error.message ?: "Authentication failed"
                            Log.e("LoginScreen", "Auth failed", error)
                        }
                    } else {
                        errorMessage = "No ID token received"
                        Log.e("LoginScreen", "No ID token in credential")
                    }
                } catch (e: ApiException) {
                    errorMessage = "Sign in failed: ${e.message}"
                    Log.e("LoginScreen", "Sign in failed", e)
                }
            }
        } else {
            // User cancelled or error occurred
            isLoading = false
            Log.d("LoginScreen", "Sign in cancelled or failed: ${result.resultCode}")
        }
    }

    /*
    ================================================================================
    SIGN-IN FUNCTION
    ================================================================================
    */

    fun startSignIn() {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null

                // Begin the One Tap sign-in flow
                val result = oneTapClient.beginSignIn(signInRequest).await()

                // Launch the sign-in intent
                val intentSenderRequest = IntentSenderRequest.Builder(
                    result.pendingIntent.intentSender
                ).build()

                signInLauncher.launch(intentSenderRequest)

            } catch (e: Exception) {
                isLoading = false
                errorMessage = when {
                    e.message?.contains("16") == true -> {
                        // Error 16: No Google accounts on device
                        "No Google account found. Please add a Google account to your device."
                    }
                    e.message?.contains("network") == true -> {
                        "Network error. Please check your connection."
                    }
                    else -> {
                        "Sign in failed: ${e.message}"
                    }
                }
                Log.e("LoginScreen", "Begin sign in failed", e)
            }
        }
    }

    /*
    ================================================================================
    UI
    ================================================================================
    */

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Logo container with border
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Assistant,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Welcome text
            Text(
                text = stringResource(R.string.login_welcome),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = stringResource(R.string.login_subtitle),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Sign in button
            Button(
                onClick = { startSignIn() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Google "G" icon would go here
                        // Using text as placeholder
                        Text(
                            text = "G",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4285F4) // Google Blue
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = stringResource(R.string.login_button_google),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Version info
            Text(
                text = stringResource(R.string.app_version),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}