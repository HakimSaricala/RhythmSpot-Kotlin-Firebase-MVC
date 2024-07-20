package com.example.rhythmspot

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.ryhthmspot.ui.theme.bg
import com.example.ryhthmspot.ui.theme.components
import com.example.ryhthmspot.ui.theme.textSecondary

@Composable
fun Playlists(ViewModelData: ViewModelData, navgationController: NavController) {
    val openDialog = remember { mutableStateOf(false) }
    val playlistName = remember { mutableStateOf("") }
    val context = LocalContext.current

    val currentPlaylist = ViewModelData.retrieveAllPlaylists()
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Recent", color = Color.White)
                }

                TextButton(onClick = { openDialog.value = true }) {
                    TextButton(onClick = { openDialog.value = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Add Playlist", color = textSecondary)
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (currentPlaylist.isNotEmpty()){
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentPlaylist) { playlist ->
                        PlaylistCard(
                            navController = navgationController,
                            playlist = playlist,
                            viewModel = ViewModelData
                        )
                    }
                }
            }else{

                Text(
                    text = "No Playlists",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 200.dp),

                    textAlign = TextAlign.Center
                )
            }
        }

        if (openDialog.value) {
            Dialog(onDismissRequest = { openDialog.value = false }) {
                Surface(color = bg) {
                    Column(modifier = Modifier
                        .padding(16.dp)
                        .fillMaxHeight(0.3f),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Give your playlist a name")
                        TextField(value = playlistName.value, onValueChange = { playlistName.value = it })
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            OutlinedButton(onClick = { openDialog.value = false }) {
                                Text("Cancel",color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(onClick = {
                                if (!ViewModelData.isPlaylistExist(playlistName.value)) {
                                    ViewModelData.createPlaylist(playlistName.value)
                                    playlistName.value = ""
                                    openDialog.value = false
                                } else {
                                    Toast.makeText(context, "Playlist already exist", Toast.LENGTH_LONG).show()
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = components)
                            ) {
                                Text("Create", color = Color.White)
                            }
                        }
                    }
                }
            }
        }




    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistCard(
    navController: NavController,
    playlist: Playlist,
    viewModel: ViewModelData,

) {
    val showDialog = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = {
                    navController.navigate("${Screens.PlaylistView.screen}/${playlist.name}")
                },
                onLongClick = {
                    showDialog.value = true
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(playlist.coverUrl),
            contentDescription = "Playlist Image",
            modifier = Modifier
                .size(50.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp),
                color = textSecondary
            )
            Text(
                text = "${playlist.songIds.size} songs",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp),
                color = Color.Gray
            )
        }
    }
    if (showDialog.value) {
        Dialog(onDismissRequest = { showDialog.value = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f)
                    .zIndex(1f),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Delete ${playlist.name} in Playlist?", Modifier.padding(16.dp), fontSize = 20.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(30.dp))
                    Row(modifier = Modifier.padding(top = 16.dp)) {
                        OutlinedButton(onClick = { showDialog.value = false }) {
                            Text(text = "Cancel", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(onClick = {
                            // Call deletePlaylist with the specific playlist name
                            viewModel.deletePlaylist(playlist.name)
                            showDialog.value = false
                        }) {
                            Text(text = "Delete", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}