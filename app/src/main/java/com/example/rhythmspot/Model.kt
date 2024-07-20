package com.example.rhythmspot

import androidx.compose.runtime.mutableIntStateOf

data class Song(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val coverUrl: String = "",
    val songUrl: String = "",
)

data class UserDetails(
    var name: String = "",
    var email: String = "",
    var favorites:  MutableList<String> = mutableListOf(),
    var playlists: MutableList<String> = mutableListOf(),
    var history: MutableList<String> = mutableListOf()
)
data class UserInfo(
    val name: String = "",
    val email: String = "",
    val profilePic: String = "",
)
data class Category(
    var name: String ="",
    val coverUrl: String = "",
    val songs: List<String> = listOf()
)

data class Artist(
    val id: String ="",
    val name: String ="",
    val artistUrl: String ="",
    val songs: List<String> = listOf()
)

data class Favorites(
    val songs: MutableList<Song> = mutableListOf()
)
data class Playlist(
    val name: String = "",
    var songIds: MutableList<String> = mutableListOf(),
    var coverUrl: String = ""
)
data class History(
    val songs: MutableList<Song> = mutableListOf()
)