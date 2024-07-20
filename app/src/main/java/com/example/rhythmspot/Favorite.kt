package com.example.rhythmspot

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ryhthmspot.ui.theme.components

@Composable
fun FavoriteScreen(viewModelData: ViewModelData, navigationController: NavController) {
    val favoriteSongs = viewModelData.retrieveFavorites()
    Column {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween)
        {
            Text(
                text = "Favorites",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            if (favoriteSongs.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            viewModelData.toggleShuffle()
                            val favoriteSongIds = viewModelData.retrieveFavorites().joinToString(separator = ",") { it.id }
                            navigationController.navigate("Music/$favoriteSongIds/${viewModelData.retrieveFavorites().first().id}")
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.shuffle),
                            contentDescription = "Shuffle Favorites",
                            modifier = Modifier.size(30.dp),
                            tint = if (viewModelData.isShuffled.value == true) components else Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = {
                            val favoriteSongIds = viewModelData.retrieveFavorites().joinToString(separator = ",") { it.id }
                            navigationController.navigate("Music/$favoriteSongIds/${viewModelData.retrieveFavorites().first().id}")
                        },
                        modifier = Modifier.size(65.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.play),
                            contentDescription = "Play Favorites",
                            modifier = Modifier.size(65.dp),
                            tint = components
                        )
                    }
                }
            }

        }


        LazyColumn {
            items(favoriteSongs) { song ->
                SongCard(song = song, onSongClick = { songId ->
                    val favoriteSongIds = favoriteSongs.joinToString(",") { it.id }
                    navigationController.navigate("Music/$favoriteSongIds/$songId")
                })
            }
        }
    }
}