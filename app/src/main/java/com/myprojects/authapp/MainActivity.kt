package com.myprojects.authapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.myprojects.authapp.biometric.BiometricPromptManager
import com.myprojects.authapp.biometric.BiometricPromptManager.*
import com.myprojects.authapp.ui.theme.AuthAppTheme

class MainActivity : AppCompatActivity() {

    private val biometricPromptManager by lazy {  BiometricPromptManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuthAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val biometricResult by biometricPromptManager.result.collectAsState(initial = null)
                    val enrollLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult(),
                        onResult = {}
                    )
                    LaunchedEffect(biometricResult){
                        if(biometricResult == BiometricResult.AuthenticationNotSet){
                            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                putExtra(
                                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                                )
                            }
                            enrollLauncher.launch(enrollIntent)
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ){
                        Button(
                            onClick = {
                                biometricPromptManager.showBiometricPrompt(
                                    title = "authentication prompt",
                                    description = "For authenticate user"
                                )
                            }
                        ){
                            Text("Authenticate yourself to use app!")
                        }
                        biometricResult?.let { result ->
                            Text ( text =
                            when(result){
                                is BiometricResult.AuthenticationError -> {
                                    "Authentication Error: ${result.error}"
                                }
                                BiometricResult.AuthenticationFailed -> {
                                    "Authentication Failed."
                                }
                                BiometricResult.AuthenticationNotSet -> {
                                    "Please set authentication from settings."
                                }
                                BiometricResult.AuthenticationSuccess -> {
                                    "Successfully authorized!"
                                }
                                BiometricResult.FeatureUnavailable -> {
                                    "Sorry!, Authentication feature is not available in this device."
                                }
                                BiometricResult.HardwareUnavailable -> {
                                    "Due to some other apps, authentication hardware is not available right now. Try again in sometimes."
                                }
                            }
                            )
                        }
                    }
                }
            }
        }
    }
}