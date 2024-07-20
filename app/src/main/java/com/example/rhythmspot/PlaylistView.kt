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
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.ryhthmspot.ui.theme.components


@Composable
fun PlaylistViewScreen(
    navgationController: NavHostController,
    ViewModelData: ViewModelData,
    playlistName: String
) {
    val playlistSongs = ViewModelData.getSongPlaylist(playlistName)
    val songids = playlistSongs.joinToString(",") { it.id }
    var isEditModeEnabled by remember { mutableStateOf(false) }
    val trigger = ViewModelData.updateTrigger.value

    Column(modifier = Modifier.fillMaxSize()) {

        IconButton(onClick = { navgationController.popBackStack() }) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text(text = playlistName, fontSize = 24.sp, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)

        if(playlistSongs.isEmpty()){
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("No song added yet", fontSize = 18.sp, color = Color.Gray)
            }        }
        else{
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {


                IconButton(
                    onClick = {
                        ViewModelData.toggleShuffle()

                        navgationController.navigate("${Screens.Music.screen}/$songids/${playlistSongs.first().id}")
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.shuffle),
                        contentDescription = "Shuffle",
                        modifier = Modifier.size(30.dp),
                        tint = if (ViewModelData.isShuffled.value == true) components else Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(
                    onClick = {

                        navgationController.navigate("${Screens.Music.screen}/$songids/${playlistSongs.first().id}")
                    },
                    modifier = Modifier.size(65.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = "Play",
                        modifier = Modifier.size(65.dp),
                        tint = components
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = {
                        // Toggle edit mode
                        isEditModeEnabled = !isEditModeEnabled
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.edit),
                        contentDescription = "Edit",
                        modifier = Modifier.size(30.dp),
                        tint = if (isEditModeEnabled) components else Color.White
                    )
                }
            }


            LazyColumn {
                items(playlistSongs) { song ->
                    SongItem(
                        song, ViewModelData,isEditModeEnabled,playlistName,
                        onSongClick = { songId ->
                            navgationController.navigate("${Screens.Music.screen}/$songids/$songId")
                        },)
                }
            }
        }




    }

}

@Composable
fun SongItem(
    song: Song,
    ViewModelData: ViewModelData,
    isEditModeEnabled: Boolean,
    playlistName: String,
    onSongClick: (String) -> Unit,
) {
    Column ( modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .clickable(onClick = { onSongClick(song.id) }),){

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(model = song.coverUrl),
                    contentDescription = "Song Cover",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = song.title, color = components, fontSize = 18.sp)
                    Text(text = song.artist, color = Color.White, fontSize = 14.sp)
                }
            }
            if (isEditModeEnabled) {
                IconButton(onClick = {
                    ViewModelData.removeSongFromPlaylist(playlistName,song.id)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.remove),
                        contentDescription = "Remove song",
                        tint = Color.Gray
                    )
                }
            }
        }
    }

}