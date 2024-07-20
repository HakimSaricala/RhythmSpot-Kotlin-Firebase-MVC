package com.example.rhythmspot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.ryhthmspot.ui.theme.textSecondary

data class BreadcrumbItem(val name: String, val onClick: () -> Unit)

@Composable
fun Library(
    navgationController: NavHostController,
    ViewModelData: ViewModelData,
    isOffline: Boolean
) {

    val breadcrumbItems = listOf(
        BreadcrumbItem(name = "Playlist", onClick = { }),
        BreadcrumbItem(name = "Liked", onClick = { }),
        BreadcrumbItem(name = "Downloads", onClick = { })
    ).filterNot { isOffline && (it.name == "Playlist" || it.name == "Liked") }


    val initialBreadcrumb = breadcrumbItems.firstOrNull()?.name ?: ""
    val selectedBreadcrumb = remember { mutableStateOf(initialBreadcrumb) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            Breadcrumb(items = breadcrumbItems, selectedBreadcrumb = selectedBreadcrumb)
            when (selectedBreadcrumb.value) {
                "Playlist" -> Playlist(ViewModelData, navgationController)
                "Liked" -> Liked(ViewModelData, navgationController)
                "Downloads" -> Downloads(ViewModelData, navgationController)
            }
        }
    }
}
@Composable
fun Breadcrumb(items: List<BreadcrumbItem>, selectedBreadcrumb: MutableState<String>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row {
            items.forEachIndexed { index, item ->
                val isSelected = item.name == selectedBreadcrumb.value
                val textColor = if (isSelected) textSecondary else Color.White
                val buttonWidth = remember { mutableStateOf(0.dp) }

                Column {
                    Layout(
                        content = {
                            TextButton(onClick = {
                                item.onClick()
                                selectedBreadcrumb.value = item.name
                            }) {
                                Text(text = item.name, color = textColor)
                            }
                        },
                        measurePolicy = { measurables, constraints ->
                            val placeable = measurables.first().measure(constraints)
                            buttonWidth.value = placeable.width.toDp()
                            layout(placeable.width, placeable.height) {
                                placeable.placeRelative(0, 0)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(buttonWidth.value)
                                .height(2.dp)
                                .background(textSecondary)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .width(buttonWidth.value)
                                .height(0.dp)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun Liked(ViewModelData: ViewModelData, navgationController: NavController) {
    FavoriteScreen(ViewModelData, navgationController)
}
@Composable
fun Playlist(ViewModelData: ViewModelData, navgationController: NavController) {
    Playlists(ViewModelData, navgationController)
}
@Composable
fun Downloads(ViewModelData: ViewModelData, navigationController: NavController) {
    DownloadScreen(ViewModelData, navigationController)
}