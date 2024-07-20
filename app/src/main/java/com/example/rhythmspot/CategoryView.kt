package com.example.rhythmspot

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.ryhthmspot.ui.theme.components

@Composable
fun CategoryViewScreen(
    ViewModelData: ViewModelData,
    categoryName: String,
    navgationController: NavHostController
) {
    val category = ViewModelData.retrieveCategories().find { it.name == categoryName }
    val songs  = ViewModelData.retriveCategorySongs(categoryName)
    val songIds = songs.joinToString(",") { it.id }
    val firstSongId = songs.firstOrNull()?.id ?: ""
    val context = LocalContext.current
    val isShuffleEnabled = ViewModelData.isShuffled.observeAsState(false)
    val playlistExists = ViewModelData.isPlaylistExist(categoryName)
    Column {
        IconButton(onClick = { navgationController.navigate(Screens.Home.screen) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")

        }
        Image(
            painter = rememberAsyncImagePainter(category?.coverUrl),
            contentDescription = "Category Cover",
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterHorizontally)
        )
        Text(text = categoryName, fontSize = 24.sp,modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(

                onClick = {
                    navgationController.navigate("${Screens.Music.screen}/$songIds/$firstSongId") },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    contentDescription = "Play",
                    modifier = Modifier.size(50.dp),
                    tint = components
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = {
                    ViewModelData.toggleShuffle()
                    navgationController.navigate("${Screens.Music.screen}/$songIds/$firstSongId")                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.shuffle),
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(30.dp),
                    tint = if (isShuffleEnabled.value) components else Color.White

                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = {
                    if (playlistExists) {
                        Toast.makeText(context, "Playlist already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        ViewModelData.createPlaylistCategory(categoryName)
                        Toast.makeText(context, "Playlist added successfully...", Toast.LENGTH_SHORT).show()
                    }
                },

                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (playlistExists) R.drawable.playlistadd
                        else R.drawable.add_playlist
                    ),
                    contentDescription = "Add Playlist",
                    modifier = Modifier.size(30.dp),
                )
            }
        }
        LazyColumn {
            items(songs) { song ->
                SongCard(song = song) { songId ->
                    navgationController.navigate("${Screens.Music.screen}/$songIds/$songId")
                }
            }
        }
    }
}