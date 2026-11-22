package com.example

import android.app.Application
import com.example.data.GalleryRepository

class XPhotosApp : Application() {
    lateinit var repository: GalleryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = GalleryRepository(applicationContext)
    }
}
