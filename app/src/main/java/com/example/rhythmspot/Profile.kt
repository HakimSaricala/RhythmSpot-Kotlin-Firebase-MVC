package com.example.rhythmspot

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

import com.example.ryhthmspot.ui.theme.components
import com.example.ryhthmspot.ui.theme.foreground

@Composable
fun Profile(
    viewModelData: ViewModelData,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val user = viewModelData.getUserInfo()
    Surface {
        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally){
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Image(
                    painter = rememberAsyncImagePainter(user.profilePic),
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop

                    )
            }
            Spacer(modifier = Modifier.height(32.dp))
            TextField(
                value = user.name,
                onValueChange = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = foreground,
                ),

                )
            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = user.email,
                onValueChange = {},
                enabled = false, // Disable input
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = foreground,
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { authViewModel.logout() },
                    modifier = Modifier.padding(end = 40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = components,
                        contentColor = Color.White
                    )

                ) {

                    Text(text = "Logout")
                }
            }
        }
    }
}
