package com.example.rhythmspot

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun DownloadScreen(ViewModelData: ViewModelData, navigationController: NavController) {
    val trigger = ViewModelData.updateTrigger.value
    val songs = ViewModelData.downloadedSongsList
    Surface(modifier = Modifier.fillMaxSize()) {
        if (ViewModelData.downloadedSongsList.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No downloaded songs", fontSize = 20.sp)
            }
        } else {
            LazyColumn {

                items(songs) { song ->
                    SongCard(song = song) { songId ->
                        val songIds = songs.joinToString(",") { it.id }

                        ViewModelData.lastScreen.value="DownloadScreen"
                        navigationController.navigate("${Screens.Music.screen}/$songIds/$songId")
                    }
                }
            }
        }
    }
}
