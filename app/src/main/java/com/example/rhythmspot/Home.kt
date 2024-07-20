package com.example.rhythmspot

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.ryhthmspot.ui.theme.foreground
import com.example.ryhthmspot.ui.theme.textSecondary

@Composable
fun Home(navgationController: NavController, ViewModelData: ViewModelData, isOffline: Boolean) {

    val scrollState = rememberScrollState()
    val historyScrollState = rememberScrollState()
    val artistscrollstate = rememberScrollState()

    var searchText by remember { mutableStateOf("") }
    val searchResults = ViewModelData.songList(searchText)
    val categories = ViewModelData.retrieveCategories()
    val HistoryList = ViewModelData.retrievehistory()
    val Artist = ViewModelData.retrieveArtists()
    Box(modifier = Modifier.fillMaxSize()) {
        if (isOffline) {
            Surface {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No internet Connection",
                        modifier = Modifier,
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp
                    )
                }
            }
        }else {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Column {
                    TextField(
                        //Search bar
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Search", color = Color.White) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp)),

                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = foreground,
                            focusedContainerColor = foreground,
                        ),
                        trailingIcon = {
                            IconButton(onClick = {  }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Box {
                        if (searchText.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .height(280.dp)
                                    .zIndex(1f)

                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()

                                ) {
                                    Column(
                                        modifier = Modifier.verticalScroll(rememberScrollState())
                                    ) {
                                        val songIds =
                                            searchResults.joinToString(separator = ",") { it.id }
                                        searchResults.forEach { song ->
                                            SongCard(
                                                song = song,

                                                ) { songId ->
                                                navgationController.navigate("${Screens.Music.screen}/$songIds/$songId")
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }

                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Column {
                            for (i in categories.indices step 2) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    ImageTextCard(
                                        image = rememberAsyncImagePainter(categories[i].coverUrl),
                                        text = categories[i].name,
                                        categoryName = categories[i].name,
                                        navController = navgationController,
                                    )

                                    // Check if there is a next category to avoid IndexOutOfBoundsException
                                    if (i + 1 < categories.size) {
                                        Spacer(modifier = Modifier.width(16.dp))

                                        ImageTextCard(
                                            image = rememberAsyncImagePainter(categories[i + 1].coverUrl),
                                            text = categories[i + 1].name,
                                            categoryName = categories[i + 1].name,
                                            navController = navgationController,

                                            )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (HistoryList.isNotEmpty()) {
                        Text(
                            text = "Recently Played", modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp), fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            Modifier
                                .padding(start = 16.dp)
                                .horizontalScroll(historyScrollState),
                        ) {

                            HistoryList.forEachIndexed { index, song ->
                                Recent(
                                    image = rememberAsyncImagePainter(song.coverUrl),
                                    song.title,
                                    onClick = {
                                        val historySongIds =
                                            HistoryList.joinToString(separator = ",") { it.id }
                                        navgationController.navigate("${Screens.Music.screen}/$historySongIds/${song.id}")
                                    }
                                )
                                Spacer(
                                    modifier = Modifier.width(16.dp)
                                )

                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Trending Artist", modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp), fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        Modifier
                            .padding(start = 16.dp)
                            .horizontalScroll(artistscrollstate),
                    ) {
                        Artist.forEachIndexed { index, artist ->
                            ArtistCard(
                                image = rememberAsyncImagePainter(artist.artistUrl),
                                text = artist.name,
                                navController = navgationController
                            )
                            Spacer(
                                modifier = Modifier.width(16.dp)
                            )

                        }
                    }


                }
            }
        }
    }
}
@Composable
fun ArtistCard(image: Painter, text: String, navController: NavController) {
    Column(
        modifier = Modifier.clickable(onClick = {
            navController.navigate("${Screens.Artist.screen}/$text")
        }),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
@Composable
fun Recent(image: Painter, text: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(180.dp)

            .clickable {
                onClick()
            },

        ) {
        Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@Composable
fun SongCard(song: Song, onSongClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = { onSongClick(song.id) }),
    ){
        Row {
            Image(
                painter = rememberAsyncImagePainter(song.coverUrl),
                contentDescription = "Song Cover",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = song.title, color = textSecondary, fontSize = 18.sp )
                Text(text = song.artist, color = Color.White, fontSize = 14.sp)
            }
        }
    }
}
@Composable
fun ImageTextCard(image: Painter, text: String, categoryName: String, navController: NavController) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { navController.navigate("${Screens.CategoryView.screen}/$categoryName") },
        colors = CardDefaults.cardColors(
            containerColor = foreground,
            contentColor = Color.White

        ),        // ...
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = image,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}