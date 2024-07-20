package com.example.rhythmspot

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ryhthmspot.ui.theme.bg
import com.example.ryhthmspot.ui.theme.components
import com.example.ryhthmspot.ui.theme.foreground
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier,
    navController: NavHostController,
    authState: State<AuthState?>,
    authViewModel: AuthViewModel
) {
    val user = FirebaseAuth.getInstance().currentUser
    val navgationController = rememberNavController()
    val ViewModelData : ViewModelData = viewModel()

    val context = LocalContext.current
    val networkStatusHelper = remember { NetworkMonitor(context) }
    // Collect the StateFlow into a State object
    val isNetworkAvailableState = networkStatusHelper.isOffline.collectAsState(initial = true)
    // Use the value from the State object
    val isOffline = isNetworkAvailableState.value


    LaunchedEffect(key1 = Unit) {
        networkStatusHelper.startNetworkCallback()
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            networkStatusHelper.stopNetworkCallback()
        }
    }

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }
    Surface(color = bg) {
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),

                    title = {
                        Text("")

                    },

                    navigationIcon = {
                        val image: Painter = painterResource(id = R.drawable.logo)
                        Row(modifier = Modifier.padding(10.dp)) {
                            IconButton(onClick = { navgationController.navigate(Screens.Home.screen) },
                                modifier = Modifier.size(40.dp)

                            ) {
                                Image(
                                    painter = image,
                                    contentDescription = "Localized description",

                                    )
                            }
                        }


                    },
                )
            },
            bottomBar = { Buttombar(navgationController,ViewModelData) }
        ) { innerPadding ->
            NavHost(navController = navgationController, startDestination = if (!isOffline) Screens.Home.screen else Screens.Library.screen,
                modifier = Modifier.padding(innerPadding)) {

                composable(Screens.Library.screen){
                    Library(navgationController, ViewModelData,isOffline )
                }
                composable(Screens.Home.screen) {
                    Home(navgationController, ViewModelData, isOffline)
                }
                composable("${Screens.CategoryView.screen}/{categoryName}") { backStackEntry ->
                    val categoryName = backStackEntry.arguments?.getString("categoryName")
                    if (categoryName != null) {
                        CategoryViewScreen(ViewModelData, categoryName, navgationController)
                    }
                }
                composable(Screens.Profile.screen){
                    Profile(ViewModelData,navController, authViewModel)
                }

                composable("${Screens.Music.screen}/{songIds}/{currentSongId}") { backStackEntry ->
                    val songIds = backStackEntry.arguments?.getString("songIds")?.split(",")
                    val currentSongId = backStackEntry.arguments?.getString("currentSongId")
                    val downloadedSongs = ViewModelData.downloadedSongsList

                    Music(
                        offlineSongs = downloadedSongs,
                        songIds = songIds,
                        currentSongId = currentSongId,
                        navController = navgationController,
                        ViewModelData,
                        isOffline = isOffline,
                    )
                }
                composable("${Screens.PlaylistView.screen}/{playlistName}") { backStackEntry ->
                    val playlistName = backStackEntry.arguments?.getString("playlistName")
                    if (playlistName != null) {
                        PlaylistViewScreen(navgationController, ViewModelData, playlistName)
                    }
                }
                composable("${Screens.AddTo.screen}/{songId}") { backStackEntry ->
                    val songId = backStackEntry.arguments?.getString("songId")
                    val song = ViewModelData.songs.find { it.id == songId } // Find the song by its ID
                    if (song != null) {
                        AddToScreen(navController = navgationController, viewModelData = ViewModelData, currentSongId = songId)
                    }
                }
                composable("${Screens.Artist.screen}/{artistName}") { backStackEntry ->
                    val artistName = backStackEntry.arguments?.getString("artistName")
                    val artist = ViewModelData.artists.find { it.name == artistName }
                    if (artist != null) {
                        ArtistView(artist = artist, navController = navgationController, viewModelData = ViewModelData)
                    }
                }



            }
        }
    }
}
@Composable
fun Buttombar(navgationController: NavController, ViewModelData: ViewModelData) {
    val home = painterResource(id = R.drawable.home)
    val music = painterResource(id = R.drawable.music)
    val favourite = painterResource(id = R.drawable.favourite)
    val profile = painterResource(id = R.drawable.profile)

    //get the current route
    val navBackStackEntry = navgationController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val selected = when {
        currentRoute?.startsWith(Screens.Home.screen) == true -> home
        currentRoute?.startsWith(Screens.CategoryView.screen) == true -> home

        currentRoute?.startsWith(Screens.Music.screen) == true -> music
        currentRoute?.startsWith(Screens.AddTo.screen) == true -> music

        currentRoute?.startsWith(Screens.Library.screen) == true -> favourite
        currentRoute?.startsWith(Screens.PlaylistView.screen) == true -> favourite


        currentRoute?.startsWith(Screens.Profile.screen) == true -> profile



        else -> home
    }
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp)),
        containerColor = foreground,
        contentColor = components,
        tonalElevation = 10.dp

    ) {
        IconButton(
            onClick = {
                navgationController.navigate(Screens.Home.screen) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = home,
                contentDescription = null,
                Modifier.size(26.dp),
                tint = if (selected == home) components else Color.White
            )
        }
        IconButton(
            onClick = {
                val allSongs = ViewModelData.retrieveSongs()
                val allSongIds = allSongs.map { it.id }
                val randomSongId = allSongIds.random()
                val songIdsString = allSongIds.joinToString(",")

                navgationController.navigate("${Screens.Music.screen}/$songIdsString/$randomSongId")
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = music,
                contentDescription = null,
                Modifier.size(26.dp),
                tint = if (selected == music) components else Color.White
            )
        }
        IconButton(
            onClick = {
                navgationController.navigate(Screens.Library.screen) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = favourite,
                contentDescription = null,
                Modifier.size(26.dp),
                tint = if (selected == favourite) components else Color.White
            )
        }
        IconButton(
            onClick = {
                navgationController.navigate(Screens.Profile.screen) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = profile,
                contentDescription = null,
                Modifier.size(26.dp),
                tint = if (selected == profile) components else Color.White
            )
        }
    }
}