package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface XPhotosRoute {
    @Serializable
    data object Library : XPhotosRoute

    @Serializable
    data class Search(val initialQuery: String? = null) : XPhotosRoute

    @Serializable
    data object Albums : XPhotosRoute

    @Serializable
    data object Settings : XPhotosRoute

    @Serializable
    data class PhotoViewer(
        val photoId: String,
        val albumId: String? = null,
        val fromVault: Boolean = false
    ) : XPhotosRoute

    @Serializable
    data class AlbumDetail(val albumId: String) : XPhotosRoute

    @Serializable
    data class Slideshow(
        val startPhotoId: String? = null,
        val albumId: String? = null
    ) : XPhotosRoute

    @Serializable
    data object Camera : XPhotosRoute

    @Serializable
    data object AboutDeveloper : XPhotosRoute

    @Serializable
    data object DeveloperOptions : XPhotosRoute

    @Serializable
    data object HiddenVault : XPhotosRoute
}
