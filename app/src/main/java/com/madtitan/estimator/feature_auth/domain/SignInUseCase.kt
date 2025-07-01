package com.madtitan.estimator.feature_auth.domain

import android.app.Activity
import android.content.Context
import android.util.Log
import com.madtitan.estimator.feature_auth.data.AuthRepository

class SignInUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(context: Context): Boolean {
        Log.d("Auth", "Context: ${context::class.java.name}")
        return authRepository.signInWithGoogle(context)
    }
}