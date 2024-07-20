package com.example.rhythmspot

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream


class ViewModelData : ViewModel() {
    val songs = mutableStateListOf<Song>()
    val artists = mutableStateListOf<Artist>()
    val categories = mutableStateListOf<Category>()
    val history = History(mutableListOf())
    val playlists = mutableStateOf(listOf<Playlist>())
    val favorites = Favorites(mutableListOf())
    val db = Firebase.firestore
    val isShuffled = MutableLiveData<Boolean>().apply { value = false }
    val user = Firebase.auth.currentUser
    val updateTrigger = mutableStateOf(0)
    val userInfo = mutableStateOf(UserInfo())
    val isDownload = MutableLiveData<Boolean>().apply { value = false }
    val downloadedSongsList = mutableStateListOf<Song>()
    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val lastScreen = mutableStateOf<String?>(null)

    init {
        loadSongs()
        loadCategories()

    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel() // Cancel the job when the ViewModel is cleared to stop the coroutine
    }
    fun isSongDownloaded(songId: String): Boolean {
        val song = songs.find { it.id == songId }
        song?.let {
            val songFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "/RhythmSpot/${it.title}.mp3")
            val isDownloaded = songFile.exists()
            isDownload.postValue(isDownloaded)
            return isDownloaded
        }
        return false
    }
    fun downloadSong(context: Context, songId: String) {
        val isAlreadyDownloaded = downloadedSongsList.any { it.id == songId }

        if (isAlreadyDownloaded) {
            Log.d("Downloads", "Song already downloaded: $songId")
            isDownload.postValue(true)
            return
        }

        val song = songs.find { it.id == songId }
        song?.let {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(song.songUrl))
                .setTitle(song.title)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setDescription("Downloading ${song.title}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "/RhythmSpot/${song.title}.mp3")

            val downloadId = downloadManager.enqueue(request)

            viewModelScope.launch {
                var done = false
                while (!done) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor: Cursor = downloadManager.query(query)

                    if (cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                            done = true

                            val downloadedSong = Song(
                                id = song.id,
                                title = song.title,
                                artist = song.artist,
                                coverUrl = R.drawable.default_cover.toString(),
                                songUrl = Uri.fromFile(File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "/RhythmSpot/${song.title}.mp3")).toString()
                            )
                            updateTrigger.value ++
                            isDownload.postValue(true)
                            downloadedSongsList.add(downloadedSong)
                            loadDownloadedSongs()
                        }
                    }
                    cursor.close()
                    if (!done) delay(1000)
                }
            }
        } ?: Log.d("Downloads", "Song with ID $songId not found")
    }
    fun getUserInfo(): UserInfo {
        return userInfo.value
    }
    fun retrievehistory(): List<Song> {
        return history.songs
    }
    fun songList(query: String): List<Song> {
        return songs.filter { song ->
            song.title.contains(query, ignoreCase = true)
        }
    }
    fun retrieveSongs(): List<Song> {
        return songs
    }
    fun retrieveCategories(): List<Category> {
        return categories

    }

    fun retriveCategorySongs(Category: String): List<Song> {
        val category = categories.find { it.name == Category }

        val songIds = category?.songs ?: emptyList()

        val filteredSongs = songs.filter { song ->
            song.id in songIds
        }

        return filteredSongs
    }
    fun toggleShuffle() {
        isShuffled.value = isShuffled.value?.not()
    }
    fun resetShuffleState() {
        isShuffled.value = false
    }
    fun retrieveFavorites(): List<Song> {
        return favorites.songs
    }
    fun retrieveArtists(): List<Artist> {
        return artists
    }
    fun retrieveArtistSongs(artistName: String): List<Song> {
        val artist = artists.find { it.name == artistName }
        val songIds = artist?.songs ?: emptyList()
        return songs.filter { it.id in songIds }
    }
    fun addSongToFavorites(songId: String) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            val userDocRef = db.collection("users").document(currentUser.uid)

            userDocRef.update("favorites", FieldValue.arrayUnion(songId))
                .addOnSuccessListener {
                    Log.d("ViewModelData", "Song added to favorites successfully")
                    if (!favorites.songs.any { it.id == songId }) {
                        val songToAdd = songs.firstOrNull { it.id == songId }
                        songToAdd?.let {
                            favorites.songs.add(it)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("ViewModelData", "Error adding song to favorites", e)
                }
        } else {
            Log.d("ViewModelData", "No user logged in")
        }
    }
    fun isSongInFavorites(songId: String): Boolean {
        return favorites.songs.any { it.id == songId }
    }
    fun removeSongFromFavorites(songId: String) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            val userDocRef = db.collection("users").document(currentUser.uid)

            // Remove the song ID from the favorites array in Firestore
            userDocRef.update("favorites", FieldValue.arrayRemove(songId))
                .addOnSuccessListener {
                    Log.d("ViewModelData", "Song removed from favorites successfully")


                    // Update the local favorites object
                    favorites.songs.removeAll { it.id == songId }
                }
                .addOnFailureListener { e ->
                    Log.d("ViewModelData", "Error removing song from favorites", e)
                }
        } else {
            Log.d("ViewModelData", "No user logged in")
        }
    }
    fun isPlaylistExist(playlistName: String): Boolean {
        return playlists.value.any { it.name.equals(playlistName) }
    }
    fun createPlaylist(playlistName: String) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            val defaultCoverUrl = "https://firebasestorage.googleapis.com/v0/b/rhythmspot-8d068.appspot.com/o/images%2Fdefault_playlist.jpg?alt=media&token=2445d833-fc33-48bd-8031-2f3d5c194fc3"
            val newPlaylist = Playlist(name = playlistName, songIds = mutableListOf(), coverUrl = defaultCoverUrl)

            val playlistData = mapOf(
                "name" to newPlaylist.name,
                "songIds" to newPlaylist.songIds,
                "coverUrl" to newPlaylist.coverUrl
            )

            val playlistDocRef = db.collection("users").document(currentUser.uid).collection("playlists").document(playlistName)

            // Set the new playlist data, overwriting any existing document with the same ID
            playlistDocRef.set(playlistData)
                .addOnSuccessListener {
                    Log.d("ViewModelData", "Playlist created successfully with name as ID: $playlistName")
                    val updatedPlaylists = playlists.value.toMutableList()
                    updatedPlaylists.add(newPlaylist)
                    playlists.value = updatedPlaylists
                }
                .addOnFailureListener { e ->
                    Log.d("ViewModelData", "Error creating playlist with name as ID", e)
                }
        } else {
            Log.d("ViewModelData", "No user logged in")
        }
    }
    fun deletePlaylist(playlistName: String) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null && isPlaylistExist(playlistName)) {
            val playlistDocRef = db.collection("users").document(currentUser.uid).collection("playlists").document(playlistName)

            playlistDocRef.delete()
                .addOnSuccessListener {
                    Log.d("ViewModelData", "Playlist deleted successfully")
                    playlists.value = playlists.value.filterNot { it.name == playlistName }
                }
                .addOnFailureListener { e ->
                    Log.d("ViewModelData", "Error deleting playlist", e)
                }
        } else {
            Log.d("ViewModelData", "No user logged in or playlist does not exist")
        }
    }
    fun retrieveAllPlaylists(): List<Playlist> {
        return playlists.value
    }
    fun isSongInPlaylist(playlistName: String, songId: String): Boolean {
        val playlist = playlists.value.find { it.name == playlistName }
        return playlist?.songIds?.contains(songId) ?: false
    }
    fun addSongToPlaylist(playlistName: String, songId: String) {
        val song = songs.find { it.id == songId }
        val songCoverUrl = song?.coverUrl ?: ""
        playlists.value.find { it.name == playlistName }?.let { playlist ->
            if (!playlist.songIds.contains(songId)) {

                playlist.songIds.add(songId)
                playlist.coverUrl = songCoverUrl
                updateTrigger.value += 1 // Trigger UI update

                val playlistDocRef = db.collection("users").document(user?.uid ?: "").collection("playlists").document(playlistName)
                playlistDocRef.update(mapOf(
                    "songIds" to FieldValue.arrayUnion(songId),
                    "coverUrl" to songCoverUrl
                ))
                    .addOnSuccessListener {
                        Log.d("ViewModelData", "Song added to playlist successfully both locally and in Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.d("ViewModelData", "Error adding song to playlist in Firestore", e)
                    }
            }
        } ?: Log.d("ViewModelData", "Playlist not found")
    }
    fun removeSongFromPlaylist(playlistName: String, songId: String) {
        playlists.value.find { it.name == playlistName }?.let { playlist ->
            playlist.songIds.removeAll { it == songId }

            // Check if the playlist is not empty and update the cover URL
            if (playlist.songIds.isNotEmpty()) {
                val lastSongCoverUrl = songs.find { it.id == playlist.songIds.last() }?.coverUrl ?: ""
                playlist.coverUrl = lastSongCoverUrl
            } else {
                playlist.coverUrl = "https://firebasestorage.googleapis.com/v0/b/rhythmspot-8d068.appspot.com/o/images%2Fdefault_playlist.jpg?alt=media&token=2445d833-fc33-48bd-8031-2f3d5c194fc3"
            }

            updateTrigger.value += 1 // Trigger UI update

            val playlistDocRef = db.collection("users").document(user?.uid ?: "").collection("playlists").document(playlistName)
            val updateMap = hashMapOf<String, Any>(
                "songIds" to FieldValue.arrayRemove(songId)
            )

            if (playlist.songIds.isNotEmpty()) {
                val lastSongCoverUrl = songs.find { it.id == playlist.songIds.last() }?.coverUrl ?: ""
                updateMap["coverUrl"] = lastSongCoverUrl
            }

            playlistDocRef.update(updateMap)
                .addOnSuccessListener {
                    Log.d("ViewModelData", "Song removed from playlist successfully both locally and in Firestore")
                }
                .addOnFailureListener { e ->
                    Log.d("ViewModelData", "Error removing song from playlist in Firestore", e)
                }
        } ?: Log.d("ViewModelData", "Playlist not found")
    }
    fun getSongPlaylist(playlistName: String): List<Song> {
        val playlist = playlists.value.find { it.name == playlistName }

        val songIds = playlist?.songIds ?: emptyList()

        val filteredSongs = songs.filter { song ->
            song.id in songIds
        }

        return filteredSongs
    }
    fun createPlaylistCategory(categoryName: String) {
        val category = categories.find { it.name == categoryName }
        if (category != null) {
            val playlistName = categoryName
            val coverUrl = category.coverUrl
            val songIds = category.songs
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                val playlistData = mapOf(
                    "name" to playlistName,
                    "songIds" to songIds,
                    "coverUrl" to coverUrl
                )
                val playlistDocRef = db.collection("users").document(currentUser.uid).collection("playlists").document(playlistName)
                playlistDocRef.set(playlistData)
                    .addOnSuccessListener {
                        Log.d("ViewModelData", "Playlist created successfully from category: $categoryName with its own details")
                        val newPlaylist = Playlist(name = playlistName, songIds = songIds.toMutableList(), coverUrl = coverUrl)
                        val updatedPlaylists = playlists.value.toMutableList()
                        updatedPlaylists.add(newPlaylist)
                        playlists.value = updatedPlaylists
                    }
                    .addOnFailureListener { e ->
                        Log.d("ViewModelData", "Error creating playlist from category with its own details", e)
                    }

            } else {
                Log.d("ViewModelData", "No user logged in")
            }
        } else {
            Log.d("ViewModelData", "Category not found: $categoryName")
        }
    }
    fun addSongToHistory(songId: String) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            val userDocRef = db.collection("users").document(currentUser.uid)

            userDocRef.update("history", FieldValue.arrayRemove(songId))
                .addOnSuccessListener {
                    userDocRef.update("history", FieldValue.arrayUnion(songId))
                        .addOnSuccessListener {
                            Log.d("ViewModelData", "Song added to history successfully")

                            // Remove the song if it's already in the local history
                            history.songs.removeAll { it.id == songId }
                            val songToAdd = songs.firstOrNull { it.id == songId }
                            songToAdd?.let {
                                history.songs.add(0, it)
                            }

                            // If the history exceeds 7 songs, remove the oldest ones
                            while (history.songs.size > 7) {
                                history.songs.removeAt(history.songs.size - 1)
                            }


                            val newHistoryIds = history.songs.map { it.id }
                            userDocRef.update("history", newHistoryIds)
                                .addOnSuccessListener {
                                    Log.d("ViewModelData", "History updated successfully in Firestore")
                                }
                                .addOnFailureListener { e ->
                                    Log.d("ViewModelData", "Error updating history in Firestore", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.d("ViewModelData", "Error adding song to history", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.d("ViewModelData", "Error removing duplicate song from history", e)
                }
        } else {
            Log.d("ViewModelData", "No user logged in")
        }
    }


}


fun ViewModelData.loadSongs() {
    db.collection("songs").get().addOnSuccessListener { result ->
        for (document in result) {
            val song = document.toObject(Song::class.java)
            songs.add(song)
        }
        loadFavorites()
        loadPlaylists()
        loadhistory()
        loadArtists()
        loadUsers()
        loadDownloadedSongs()
    }
}
fun ViewModelData.loadUsers() {
    val currentUser = Firebase.auth.currentUser
    if(currentUser != null){
        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
            val user = document.toObject(UserInfo::class.java)
            user?.let {
                userInfo.value = UserInfo(name = it.name, email = it.email, profilePic = it.profilePic)
            }
        }
    }
}
fun ViewModelData.loadCategories() {
    db.collection("category").get().addOnSuccessListener { result ->
        for (document in result) {
            Log.d("ViewModelData", "loadCategories: ${document.data}")
            val category = document.toObject(Category::class.java)
            categories.add(category)
        }
    }.addOnFailureListener() {
        Log.d("ViewModelData", "loadCategories: ${it.message}")
    }
}

fun ViewModelData.loadArtists() {
    db.collection("artist").get().addOnSuccessListener { result ->
        for (document in result) {
            val artist = document.toObject(Artist::class.java)
            artists.add(artist)
        }
    }.addOnFailureListener {
        Log.d("ViewModelData", "Error loading artists: ${it.message}")
    }
}
fun ViewModelData.loadFavorites() {
    val currentUser = Firebase.auth.currentUser

    if (currentUser != null) {
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(UserDetails::class.java)
                val favoriteSongIds = user?.favorites ?: emptyList()
                Log.d("ViewModelData", "Favorite song IDs fetched directly from database: $favoriteSongIds")

                // Clear the current favorites list to avoid duplicates
                favorites.songs.clear()

                val favoriteSongs = songs.filter { song ->
                    song.id in favoriteSongIds
                }

                favorites.songs.addAll(favoriteSongs)

                Log.d("ViewModelData", "Favorites updated from local songs: ${favorites.songs.size} songs")
            }
            .addOnFailureListener { e ->
                Log.d("ViewModelData", "Error fetching favorite song IDs from database", e)
            }
    }
}
fun ViewModelData.loadPlaylists() {
    val currentUser = Firebase.auth.currentUser
    if (currentUser != null) {
        db.collection("users").document(currentUser.uid).collection("playlists")
            .get()
            .addOnSuccessListener { result ->
                val fetchedPlaylists = mutableListOf<Playlist>()
                for (document in result) {
                    val playlist = document.toObject(Playlist::class.java)
                    fetchedPlaylists.add(playlist)
                }
                playlists.value = fetchedPlaylists
            }
            .addOnFailureListener { e ->
                Log.d("ViewModelData", "Error loading playlists", e)
            }
    } else {
        Log.d("ViewModelData", "No user logged in")
    }
}
fun ViewModelData.loadhistory() {
    val currentUser = Firebase.auth.currentUser
    if (currentUser != null) {
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(UserDetails::class.java)
                val historySongIds = user?.history ?: emptyList()
                Log.d("ViewModelData", "History song IDs fetched from database: $historySongIds")

                // Clear the current history list to avoid duplicates
                history.songs.clear()

                val historySongs = songs.filter { song ->
                    song.id in historySongIds
                }

                history.songs.addAll(historySongs)

                Log.d("ViewModelData", "History updated from local songs: ${history.songs.size} songs")
            }
            .addOnFailureListener { e ->
                Log.d("ViewModelData", "Error fetching history song IDs from database", e)
            }
    }
}
fun ViewModelData.loadDownloadedSongs() {

    val downloadedSongsDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "/RhythmSpot/")
    val defaultCoverUri = Uri.parse("android.resource://" + "com.example.rhythmspot"+ "/" + R.drawable.default_cover)

    if (!downloadedSongsDirectory.exists()) {
        Log.d("ViewModelData", "Downloaded songs directory does not exist.")
        return
    }

    val downloadedSongs = downloadedSongsDirectory.listFiles()?.filter { it.isFile && it.name.endsWith(".mp3") }
    downloadedSongs?.let { files ->
        downloadedSongsList.clear()
        files.forEach { file ->
            val songUri = Uri.fromFile(file).toString()
            val song = Song(
                id = file.nameWithoutExtension,
                title = file.nameWithoutExtension,
                artist = "Unknown",
                coverUrl = defaultCoverUri.toString(),
                songUrl = songUri
            )
            downloadedSongsList.add(song)
        }
    } ?: Log.d("ViewModelData", "No downloaded songs found.")
}
