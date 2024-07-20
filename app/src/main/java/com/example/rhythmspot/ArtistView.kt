package com.example.rhythmspot

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.ryhthmspot.ui.theme.components
import com.example.ryhthmspot.ui.theme.textSecondary

@Composable
fun ArtistView(artist: Artist, navController: NavController, viewModelData: ViewModelData) {
    val artistSongs = viewModelData.retrieveArtistSongs(artist.name)
    val songids = artistSongs.joinToString(",") { it.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(artist.artistUrl),
                contentDescription = "Artist Profile Picture",
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = artist.name,
                fontSize = 26.sp,
                color = textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        Spacer(modifier = Modifier.size(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {

            IconButton(
                onClick = {
                    viewModelData.toggleShuffle()
                    navController.navigate("${Screens.Music.screen}/$songids/${artistSongs.first().id}")
                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.shuffle),
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(30.dp),
                    tint = if (viewModelData.isShuffled.value == true) components else Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = {

                    navController.navigate("${Screens.Music.screen}/$songids/${artistSongs.first().id}")
                },
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    contentDescription = "Play",
                    modifier = Modifier.size(60.dp),
                    tint = components
                )
            }
        }

        LazyColumn {
            items(artistSongs) { song ->
                ArtistSongDisplay(song = song, modifier = Modifier.padding(16.dp)) { songId ->
                    navController.navigate("${Screens.Music.screen}/$songids/$songId")
                }
            }

        }

    }
}



@Composable
fun ArtistSongDisplay(song: Song, modifier: Modifier = Modifier, onSongClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = { onSongClick(song.id) }),

        ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(song.coverUrl),
                contentDescription = "Song Cover",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = song.title, color = Color.White, fontSize = 24.sp)
        }
    }
}