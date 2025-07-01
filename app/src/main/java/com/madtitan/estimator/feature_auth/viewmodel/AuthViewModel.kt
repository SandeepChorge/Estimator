package com.madtitan.estimator.feature_auth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.madtitan.estimator.feature_auth.domain.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase
) : ViewModel() {

    suspend fun signIn(context: Context): Boolean {
        return signInUseCase(context)
    }
}