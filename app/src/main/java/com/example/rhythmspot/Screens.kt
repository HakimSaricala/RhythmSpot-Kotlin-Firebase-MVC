package com.example.rhythmspot

sealed class Screens(val screen: String) {
    object Home : Screens("home")
    object Music : Screens("music")
    object Library : Screens("library")
    object Profile : Screens("profile")
    object CategoryView : Screens("CategoryViewScreen")
    object Artist : Screens("artist")
    object PlaylistView : Screens("playlistView")
    object AddTo : Screens("AddToScreen")
}