package com.madtitan.estimator.feature_auth.ui

import android.content.Context
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.madtitan.estimator.Screen
import com.madtitan.estimator.feature_auth.domain.SignInUseCase
import com.madtitan.estimator.feature_auth.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    var loginState by remember { mutableStateOf(false) }

    Button(onClick = {
        coroutineScope.launch {
            val success = viewModel.signIn(context) // Get result from use case
            loginState = success
            if (success) {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
    }) {
        Text(text = "Sign in with Google")
    }

    if (loginState) {
        Text("Login successful!", color = Color.Green)
    }
}