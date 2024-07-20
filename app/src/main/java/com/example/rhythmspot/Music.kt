package com.example.rhythmspot

import android.content.Context
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.ryhthmspot.ui.theme.components
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun Music(
    offlineSongs: List<Song>? = null,
    songIds: List<String>?,
    currentSongId: String?,
    navController: NavController,
    ViewModelData: ViewModelData,
    isOffline: Boolean,

    ) {
    val source = ViewModelData.lastScreen.value
    val filteredSongs = if(source == "DownloadScreen"){
                offlineSongs ?: listOf()
            }else{
                ViewModelData.songs.filter { song ->
                    songIds?.contains(song.id) == true
                }

            }

    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = ExoPlayer.REPEAT_MODE_ALL // Set repeat mode to play songs in a loop
            shuffleModeEnabled = ViewModelData.isShuffled.value == true
        }
    }

    val songsToPlay = when {
        isOffline -> offlineSongs ?: listOf()
        else -> filteredSongs
    }
    // Prepare the player with the filtered songs
    LaunchedEffect(key1 = songsToPlay) {
        setPlayerMediaItems(player, songsToPlay, context, ViewModelData.isShuffled.value ?: false, currentSongId ?: "")
        player.play()
    }

    val currentSong = remember { mutableStateOf<Song?>(null) }
    val isDownloaded = remember { mutableStateOf(false) }
    val isDownloading = remember { mutableStateOf(false) }

    LaunchedEffect(ViewModelData.downloadedSongsList.size) {
        isDownloaded.value = ViewModelData.isSongDownloaded(currentSong.value?.id ?: "")
    }

    LaunchedEffect(player.currentMediaItem) {
        val currentMediaItemId = player.currentMediaItem?.mediaId
        currentMediaItemId?.let { id ->
            // Find the corresponding Song object using the mediaId
            val playingsong = filteredSongs.find { it.id == id }
            // Add the current song to history
            currentSong.value = playingsong
            currentSong.value?.let { song ->
                isDownloaded.value = ViewModelData.isSongDownloaded(song.id)
                ViewModelData.addSongToHistory(song.id)
            }

        }
    }
    val currentPosition = remember {
        mutableLongStateOf(0)
    }
    val sliderPosition = remember {
        mutableLongStateOf(0)
    }
    val totalDuration = remember {
        mutableLongStateOf(0)
    }
    val isPlaying = remember {
        mutableStateOf(true)
    }



    LaunchedEffect(key1 = player.currentPosition, key2 = player.isPlaying) {
        delay(1000)
        withContext(Dispatchers.Main) {
            currentPosition.longValue = player.currentPosition
        }
    }

    LaunchedEffect(currentPosition.longValue) {
        withContext(Dispatchers.Main) {
            sliderPosition.longValue = currentPosition.longValue
        }
    }

    LaunchedEffect(player.duration) {
        withContext(Dispatchers.Main) {
            if (player.duration > 0) {
                totalDuration.longValue = player.duration
            }
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    modifier = Modifier.size(30.dp),

                    tint = Color.White
                )
            }
            Row {
                IconButton(onClick = {

                    currentSong.value?.id?.let { songId ->
                        if (!isDownloaded.value && !isDownloading.value) {
                            isDownloading.value = true
                            ViewModelData.downloadSong(context, songId)
                            if(isDownloaded.value == true){
                                isDownloading.value = false
                            }
                        } else {
                            Toast.makeText(context, "Song is already downloaded", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    val iconset = when {
                        isDownloaded.value -> R.drawable.download_done
                        isDownloading.value -> R.drawable.downloading
                        else -> R.drawable.download
                    }
                    Icon(
                        painter = painterResource(id = iconset),
                        contentDescription = "Download",
                        modifier = Modifier.size(35.dp),
                    )
                }
                IconButton(onClick = {
                    currentSong.value?.id?.let { songId ->
                        navController.navigate("${Screens.AddTo.screen}/$songId")
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_playlist),
                        contentDescription = "add playlist",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
            }

        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                currentSong.value?.coverUrl?.let { coverUrl ->

                    Image(
                        painter = rememberAsyncImagePainter(coverUrl),
                        contentDescription = "Cover Image",
                        modifier = Modifier
                            .size(330.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = currentSong.value?.title ?: "",
                                color = components,
                                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),

                                )
                            Text(
                                text = currentSong.value?.artist ?: "",
                                color = Color.White,
                                style = TextStyle(fontSize = 14.sp)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){

                            IconButton(onClick = {
                                ViewModelData.toggleShuffle()
                                player.shuffleModeEnabled = ViewModelData.isShuffled.value ?: false
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.shuffle),
                                    contentDescription = "shuffle",
                                    modifier = Modifier.size(25.dp),
                                    tint = if (ViewModelData.isShuffled.value == true) components else Color.White
                                )
                            }
                            IconButton(onClick = {
                                currentSong.value?.id?.let { songId ->
                                    if (!ViewModelData.isSongInFavorites(songId)) {
                                        ViewModelData.addSongToFavorites(songId)
                                    } else {
                                        ViewModelData.removeSongFromFavorites(songId)
                                    }
                                }
                            }) {
                                val isFavorite = currentSong.value?.id?.let { songId ->
                                    ViewModelData.isSongInFavorites(songId)
                                } ?: false
                                Icon(
                                    painter = painterResource(id = if (isFavorite) R.drawable.heart_filled else R.drawable.favourite),
                                    contentDescription = "favourite",
                                    modifier = Modifier.size(25.dp),
                                    tint = if (isFavorite) components else Color.White
                                )
                            }


                        }


                    }


                    TrackSlider(
                        value = sliderPosition.longValue.toFloat(),
                        onValueChange = {
                            sliderPosition.longValue = it.toLong()
                        },
                        onValueChangeFinished = {
                            currentPosition.longValue = sliderPosition.longValue
                            player.seekTo(sliderPosition.longValue)
                        },
                        songDuration = totalDuration.longValue.toFloat()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {

                        Text(
                            text = (currentPosition.longValue).convertToText(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            color = Color.White,
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )

                        val remainTime = totalDuration.longValue - currentPosition.longValue
                        Text(
                            text = if (remainTime >= 0) remainTime.convertToText() else "",
                            modifier = Modifier
                                .padding(8.dp),
                            color = Color.White,
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        ControlButton(icon = R.drawable.skipprev, size = 40.dp, onClick = {
                            player.seekToPreviousMediaItem()
                        })
                        Spacer(modifier = Modifier.width(20.dp))
                        ControlButton(
                            icon = if (isPlaying.value) R.drawable.pause else R.drawable.play,
                            size = 100.dp,
                            onClick = {
                                if (isPlaying.value) {
                                    player.pause()
                                } else {
                                    player.play()
                                }
                                isPlaying.value = player.isPlaying
                            })
                        Spacer(modifier = Modifier.width(20.dp))
                        ControlButton(icon = R.drawable.skipnext, size = 40.dp, onClick = {
                            player.seekToNextMediaItem()
                        })
                    }
                }

            }
        }
        DisposableEffect(player) {
            onDispose {
                ViewModelData.resetShuffleState()
                ViewModelData.lastScreen.value = ""
                player.release()

            }
        }
    }


}
fun setPlayerMediaItems(player: ExoPlayer, songs: List<Song>, context: Context, shuffle: Boolean, currentSongId: String?) {
    val playlist = mutableListOf<MediaItem>()
    if (shuffle) {
        songs.shuffled().forEach { song ->
            val mediaItem = MediaItem.Builder()
                .setUri(song.songUrl)
                .setMediaId(song.id)
                .build()
            playlist.add(mediaItem)
        }
    } else {
        // Find the index of the current song
        val currentIndex = songs.indexOfFirst { it.id == currentSongId }.takeIf { it >= 0 } ?: 0
        // Reorder the list to start from the current song
        val orderedSongs = songs.drop(currentIndex) + songs.take(currentIndex)
        orderedSongs.forEach { song ->
            val mediaItem = MediaItem.Builder()
                .setUri(song.songUrl)
                .setMediaId(song.id)
                .build()
            playlist.add(mediaItem)
        }
    }
    player.setMediaItems(playlist, true)
    player.prepare()
}
private fun Long.convertToText(): String {
    val sec = this / 1000
    val minutes = sec / 60
    val seconds = sec % 60

    val minutesString = if (minutes < 10) {
        "0$minutes"
    } else {
        minutes.toString()
    }
    val secondsString = if (seconds < 10) {
        "0$seconds"
    } else {
        seconds.toString()
    }
    return "$minutesString:$secondsString"
}
@Composable
fun TrackSlider(
    value: Float,
    onValueChange: (newValue: Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    songDuration: Float

) {
    Slider(
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        onValueChangeFinished = {

            onValueChangeFinished()

        },
        valueRange = 0f..songDuration,
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.White,
            inactiveTrackColor = Color.White,
        )
    )
}
@Composable
fun ControlButton(icon: Int, size: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable {
                onClick()
            }, contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(size / 1.5f),
            painter = painterResource(id = icon),
            tint = Color.White,
            contentDescription = null
        )
    }
}

