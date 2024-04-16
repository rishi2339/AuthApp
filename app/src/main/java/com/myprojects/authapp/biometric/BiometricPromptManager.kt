package com.myprojects.authapp.biometric

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricPromptManager(
    private val activity: AppCompatActivity
) {
    private val channelResult = Channel<BiometricResult>()
    val result = channelResult.receiveAsFlow()

    fun showBiometricPrompt(
        title:String,
        description: String
    ){
        val biometricManager = BiometricManager.from(activity)
        val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        val promptInfo = PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(false)

        when(biometricManager.canAuthenticate(authenticators)){
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                channelResult.trySend(BiometricResult.HardwareUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                channelResult.trySend(BiometricResult.AuthenticationNotSet)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                channelResult.trySend(BiometricResult.FeatureUnavailable)
                return
            }
            else -> {Unit}
        }

        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    channelResult.trySend(BiometricResult.AuthenticationError(errString.toString()))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    channelResult.trySend(BiometricResult.AuthenticationSuccess)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    channelResult.trySend(BiometricResult.AuthenticationFailed)
                }
            }
        )
        prompt.authenticate(promptInfo.build())
    }

    sealed interface BiometricResult {
        data object HardwareUnavailable:BiometricResult
        data object FeatureUnavailable:BiometricResult
        data object AuthenticationNotSet: BiometricResult
        data object AuthenticationFailed: BiometricResult
        data object AuthenticationSuccess:BiometricResult
        data class AuthenticationError(val error:String):BiometricResult
    }
}