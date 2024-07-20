package com.example.rhythmspot

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(modifier: Modifier, navController: NavController, authState: State<AuthState?>) {
    LaunchedEffect(key1 = true) {
        delay(3000) // Delay for 3 seconds
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("Main") {
                popUpTo("Splash") { inclusive = true }
            }
            else -> navController.navigate("login") {
                popUpTo("Splash") { inclusive = true }
            }
        }
    }
    Surface(modifier = modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            Image(painter = painterResource(id = R.drawable.splash_logo), contentDescription = "Logo",
                modifier = Modifier.size(350.dp)
            )
        }
    }
}