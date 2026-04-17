package com.vrijgeld

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.vrijgeld.ui.navigation.NavGraph
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.TextSecondary
import com.vrijgeld.ui.theme.VrijGeldTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var isAuthenticated by mutableStateOf(false)
    private var authError       by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        authenticate()
        setContent {
            VrijGeldTheme {
                AnimatedContent(targetState = isAuthenticated, label = "auth") { authenticated ->
                    if (authenticated) NavGraph()
                    else LockScreen(error = authError, onRetry = ::authenticate)
                }
            }
        }
    }

    private fun authenticate() {
        val canAuth = BiometricManager.from(this)
            .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            // No lock screen configured — allow access on developer devices
            isAuthenticated = true
            return
        }

        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    isAuthenticated = true
                    authError       = null
                }
                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    authError = msg.toString()
                }
            }
        )

        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock VrijGeld")
                .setSubtitle("Confirm your identity to access financial data")
                .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                .build()
        )
    }
}

@Composable
private fun LockScreen(error: String?, onRetry: () -> Unit) {
    Box(
        modifier            = Modifier.fillMaxSize().background(Background),
        contentAlignment    = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🔒", style = MaterialTheme.typography.displayMedium)
            Text("VrijGeld", style = MaterialTheme.typography.headlineMedium)
            Text(
                text      = error ?: "Authenticate to continue",
                style     = MaterialTheme.typography.bodyMedium,
                color     = TextSecondary,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 32.dp)
            )
            Button(onClick = onRetry) { Text("Unlock") }
        }
    }
}
