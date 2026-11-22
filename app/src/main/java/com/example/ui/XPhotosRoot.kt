package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.navigation.XPhotosRoute
import com.example.ui.screens.AboutDeveloperScreen
import com.example.ui.screens.AlbumDetailScreen
import com.example.ui.screens.AlbumsScreen
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.DeveloperOptionsScreen
import com.example.ui.screens.HiddenVaultScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PhotoViewerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SlideshowScreen
import com.example.ui.theme.xPalette

private data class TopTab(
    val route: XPhotosRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val topTabs = listOf(
    TopTab(XPhotosRoute.Library, "Library", Icons.Rounded.PhotoLibrary, Icons.Outlined.PhotoLibrary, "tab_library"),
    TopTab(XPhotosRoute.Search(), "Search", Icons.Rounded.Search, Icons.Outlined.Search, "tab_search"),
    TopTab(XPhotosRoute.Albums, "Albums", Icons.Rounded.Layers, Icons.Outlined.Layers, "tab_albums"),
    TopTab(XPhotosRoute.Settings, "Settings", Icons.Rounded.Settings, Icons.Outlined.Settings, "tab_settings")
)

@Composable
fun XPhotosRoot(
    uiState: GalleryUiState,
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.xPalette
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    val showTabs = destination?.let { dest ->
        dest.hasRoute<XPhotosRoute.Library>() ||
            dest.hasRoute<XPhotosRoute.Search>() ||
            dest.hasRoute<XPhotosRoute.Albums>() ||
            dest.hasRoute<XPhotosRoute.Settings>()
    } ?: true

    fun navigateToTab(route: XPhotosRoute) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = route !is XPhotosRoute.Search || route.initialQuery == null
        }
    }

    fun isTabSelected(tab: TopTab): Boolean {
        val dest = destination ?: return tab.route is XPhotosRoute.Library
        return when (tab.route) {
            is XPhotosRoute.Library -> dest.hasRoute<XPhotosRoute.Library>()
            is XPhotosRoute.Search -> dest.hasRoute<XPhotosRoute.Search>()
            is XPhotosRoute.Albums -> dest.hasRoute<XPhotosRoute.Albums>()
            is XPhotosRoute.Settings -> dest.hasRoute<XPhotosRoute.Settings>()
            else -> false
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg)
    ) {
        val useRail = maxWidth >= 600.dp

        val navHostContent: @Composable (Modifier) -> Unit = { hostMod ->
            NavHost(
                navController = navController,
                startDestination = XPhotosRoute.Library,
                modifier = hostMod
            ) {
                composable<XPhotosRoute.Library> {
                    LibraryScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenPhoto = { id -> navController.navigate(XPhotosRoute.PhotoViewer(id)) },
                        onOpenAlbum = { id -> navController.navigate(XPhotosRoute.AlbumDetail(id)) },
                        onOpenSlideshow = { startId -> navController.navigate(XPhotosRoute.Slideshow(startPhotoId = startId)) },
                        onOpenCamera = { navController.navigate(XPhotosRoute.Camera) },
                        onOpenSearch = { navigateToTab(XPhotosRoute.Search()) },
                        onOpenAlbumsTab = { navigateToTab(XPhotosRoute.Albums) }
                    )
                }

                composable<XPhotosRoute.Search> { entry ->
                    val route = entry.toRoute<XPhotosRoute.Search>()
                    SearchScreen(
                        initialQuery = route.initialQuery,
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenPhoto = { id -> navController.navigate(XPhotosRoute.PhotoViewer(id)) },
                        onOpenAlbum = { id -> navController.navigate(XPhotosRoute.AlbumDetail(id)) }
                    )
                }

                composable<XPhotosRoute.Albums> {
                    AlbumsScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenAlbum = { id -> navController.navigate(XPhotosRoute.AlbumDetail(id)) }
                    )
                }

                composable<XPhotosRoute.Settings> {
                    SettingsScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenAboutDeveloper = { navController.navigate(XPhotosRoute.AboutDeveloper) },
                        onOpenDeveloperOptions = { navController.navigate(XPhotosRoute.DeveloperOptions) },
                        onOpenHiddenVault = { navController.navigate(XPhotosRoute.HiddenVault) }
                    )
                }

                composable<XPhotosRoute.AboutDeveloper> {
                    AboutDeveloperScreen(
                        uiState = uiState,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<XPhotosRoute.DeveloperOptions> {
                    DeveloperOptionsScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<XPhotosRoute.HiddenVault> {
                    HiddenVaultScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenPhoto = { id ->
                            navController.navigate(XPhotosRoute.PhotoViewer(photoId = id, fromVault = true))
                        }
                    )
                }

                composable<XPhotosRoute.PhotoViewer> { entry ->
                    val route = entry.toRoute<XPhotosRoute.PhotoViewer>()
                    PhotoViewerScreen(
                        photoId = route.photoId,
                        albumId = route.albumId,
                        fromVault = route.fromVault,
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenPhoto = { id ->
                            navController.navigate(
                                XPhotosRoute.PhotoViewer(
                                    photoId = id,
                                    albumId = route.albumId,
                                    fromVault = route.fromVault
                                )
                            )
                        },
                        onOpenSlideshow = { id ->
                            navController.navigate(XPhotosRoute.Slideshow(startPhotoId = id, albumId = route.albumId))
                        },
                        onOpenAlbumsTab = {
                            navController.popBackStack(XPhotosRoute.Library, inclusive = false)
                            navigateToTab(XPhotosRoute.Albums)
                        }
                    )
                }

                composable<XPhotosRoute.AlbumDetail> { entry ->
                    val route = entry.toRoute<XPhotosRoute.AlbumDetail>()
                    AlbumDetailScreen(
                        albumId = route.albumId,
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenPhoto = { id, aId ->
                            navController.navigate(XPhotosRoute.PhotoViewer(photoId = id, albumId = aId))
                        },
                        onOpenSlideshow = { startId, albumId ->
                            navController.navigate(XPhotosRoute.Slideshow(startPhotoId = startId, albumId = albumId))
                        },
                        onOpenLibraryTab = {
                            navController.popBackStack(XPhotosRoute.Library, inclusive = false)
                            navigateToTab(XPhotosRoute.Library)
                        },
                        onOpenAlbumsTab = {
                            navController.popBackStack(XPhotosRoute.Library, inclusive = false)
                            navigateToTab(XPhotosRoute.Albums)
                        }
                    )
                }

                composable<XPhotosRoute.Slideshow> { entry ->
                    val route = entry.toRoute<XPhotosRoute.Slideshow>()
                    SlideshowScreen(
                        startPhotoId = route.startPhotoId,
                        albumId = route.albumId,
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<XPhotosRoute.Camera> {
                    CameraScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenPhoto = { id ->
                            navController.navigate(XPhotosRoute.PhotoViewer(id)) {
                                popUpTo(XPhotosRoute.Camera) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }

        if (useRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (showTabs) {
                    NavigationRail(
                        containerColor = palette.tabBar,
                        contentColor = palette.text,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        topTabs.forEach { tab ->
                            val selected = isTabSelected(tab)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = palette.primary,
                                    selectedTextColor = palette.primary,
                                    unselectedIconColor = palette.textMuted,
                                    unselectedTextColor = palette.textMuted,
                                    indicatorColor = palette.primarySoft
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
                navHostContent(Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Scaffold(
                containerColor = palette.bg,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (showTabs) {
                        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
                            NavigationBar(
                                containerColor = palette.tabBar,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                topTabs.forEach { tab ->
                                    val selected = isTabSelected(tab)
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateToTab(tab.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = palette.primary,
                                            selectedTextColor = palette.primary,
                                            unselectedIconColor = palette.textMuted,
                                            unselectedTextColor = palette.textMuted,
                                            indicatorColor = palette.primarySoft
                                        ),
                                        modifier = Modifier.testTag(tab.testTag)
                                    )
                                }
                            }
                            HorizontalDivider(
                                thickness = 0.75.dp,
                                color = palette.border,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            ) { innerPadding ->
                navHostContent(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}
