package com.example.rhythmspot

import android.widget.Toast
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Authnav(modifier: Modifier,authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Authenticated -> navController.navigate("Main")
            is AuthState.AuthError -> Toast.makeText(context,
                (authState.value as AuthState.AuthError).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }
    Surface {
        NavHost(navController = navController, startDestination = "Splash") {
            composable("login") {
                Login(modifier,navController, authViewModel)
            }
            composable("register") {
                Register(modifier,navController, authViewModel)
            }
            composable("Main") {
                MainScreen(modifier,navController,authState, authViewModel)
            }
            composable("Splash") {
                SplashScreen(modifier,navController, authState)
            }
        }
    }
}