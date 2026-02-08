package com.example.eva.ui.theme.spash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eva.R
import com.example.eva.app.data.repository.AuthRepository
import com.example.eva.app.utils.Constants
import kotlinx.coroutines.delay

/*
================================================================================
SPLASH SCREEN
================================================================================

PURPOSE:
    The first screen users see when opening the app.

RESPONSIBILITIES:
    1. Display Eva logo and branding
    2. Check if user is already logged in
    3. Navigate to appropriate screen (login or chat)

FLOW:
    App opens
        ↓
    Show splash screen (2 seconds)
        ↓
    Check login status
        ↓
    If logged in → Navigate to Chat
    If not logged in → Navigate to Login

DESIGN:
    - Dark background
    - Eva logo (icon)
    - "Eva" text
    - "Your Personal AI Assistant" tagline
    - Loading indicator

================================================================================
*/

@Composable
fun SplashScreen(
    authRepository: AuthRepository,
    onNavigateToLogin: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    /*
    ================================================================================
    STATE
    ================================================================================
    */

    // Controls fade-in animation
    var startAnimation by remember { mutableStateOf(false) }

    // Animate alpha from 0 to 1
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "splash_alpha"
    )

    /*
    ================================================================================
    EFFECTS
    ================================================================================
    */

    // Start animation and check login status
    LaunchedEffect(key1 = true) {
        // Start fade-in animation
        startAnimation = true

        // Wait for splash duration
        delay(Constants.SPLASH_DELAY_MS)

        // Check if user is logged in
        if (authRepository.isLoggedIn()) {
            onNavigateToChat()
        } else {
            onNavigateToLogin()
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
            modifier = Modifier.alpha(alphaAnim)
        ) {
            // Eva Logo (using Assistant icon as placeholder)
            Icon(
                imageVector = Icons.Default.Assistant,
                contentDescription = stringResource(R.string.cd_app_logo),
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = stringResource(R.string.splash_tagline),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loading text
            Text(
                text = stringResource(R.string.splash_loading),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Version number at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alphaAnim),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = stringResource(R.string.app_version),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.BottomCenter)
                    .then(Modifier.background(MaterialTheme.colorScheme.background))
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}