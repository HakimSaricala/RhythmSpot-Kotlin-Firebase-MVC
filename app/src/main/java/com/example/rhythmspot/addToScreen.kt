package com.example.rhythmspot

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.ryhthmspot.ui.theme.components

@Composable
fun AddToScreen(navController: NavController, viewModelData: ViewModelData, currentSongId: String?) {
    val currentSong = viewModelData.songs.find { it.id == currentSongId }
    val playlists = viewModelData.retrieveAllPlaylists()
    val selectedPlaylists = remember { mutableStateOf(listOf<String>()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Text(text = "Add to Playlist", modifier = Modifier.padding(16.dp))
            if (playlists.isNotEmpty()) {
                LazyColumn {
                    items(playlists.size) { playlist ->
                        PlaylistsCard(playlists[playlist], currentSongId, viewModelData) { playlistName, isChecked ->
                            val currentSelected = selectedPlaylists.value.toMutableList()
                            if (isChecked) {
                                if (!currentSelected.contains(playlistName)) {
                                    currentSelected.add(playlistName)
                                    currentSong?.let { song ->
                                        viewModelData.addSongToPlaylist(playlistName, song.id)
                                    }
                                }
                            } else {
                                currentSelected.remove(playlistName)
                                currentSong?.let { song ->
                                    viewModelData.removeSongFromPlaylist(playlistName, song.id)
                                }
                            }
                            selectedPlaylists.value = currentSelected
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(onClick = { navController.navigate(Screens.Library.screen) }) {
                        Text("Add Playlist")
                    }
                }
            }
        }
        Button(
            onClick = {
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = components
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(150.dp)
                .padding(bottom = 56.dp)
                .zIndex(1f)
        ) {
            Text("Done")
        }
    }
}
@Composable
fun PlaylistsCard(playlist: Playlist, currentSongId: String?, viewModelData: ViewModelData, updateSelectedPlaylists: (String, Boolean) -> Unit) {
    val isSongInPlaylist = currentSongId?.let { viewModelData.isSongInPlaylist(playlist.name, it) } ?: false
    var isChecked by remember { mutableStateOf(isSongInPlaylist) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                isChecked = !isChecked
                updateSelectedPlaylists(playlist.name, isChecked)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(playlist.coverUrl),
                contentDescription = "Playlist Image",
                modifier = Modifier
                    .size(50.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Checkbox(
            checked = isChecked,
            onCheckedChange = { newCheckedValue ->
                updateSelectedPlaylists(playlist.name, newCheckedValue)
                isChecked = newCheckedValue
            }
        )
    }
}