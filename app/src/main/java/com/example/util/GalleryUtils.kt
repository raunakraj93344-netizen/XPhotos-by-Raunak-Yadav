package com.example.util

import android.app.WallpaperManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.core.content.FileProvider
import com.example.model.DeviceDiagnostics
import com.example.model.Photo
import com.example.model.WallpaperTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong

object GalleryUtils {

    fun formatCount(value: Int): String = when {
        value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000.0)
        value >= 1_000 -> String.format(Locale.US, "%.1fK", value / 1_000.0)
        else -> value.toString()
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0 MB"
        val units = listOf("B", "KB", "MB", "GB", "TB")
        val i = min(floor(log(bytes.toDouble(), 1024.0)).toInt(), units.lastIndex)
        val value = bytes.toDouble() / 1024.0.pow(i)
        return if (i == 0) "${value.toInt()} ${units[i]}"
        else String.format(Locale.US, "%.1f %s", value, units[i])
    }

    fun estimateSize(photo: Photo): Long =
        if (photo.sizeBytes > 0L) photo.sizeBytes
        else (photo.width.toLong() * photo.height.toLong() * 0.62).roundToLong()

    fun formatDate(epochMillis: Long): String =
        DateFormat.getDateInstance(DateFormat.LONG).format(Date(epochMillis))

    fun formatDateTime(epochMillis: Long): String =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun initials(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "XP"
            parts.size == 1 -> parts.first().take(2).uppercase()
            else -> "${parts.first().first()}${parts.last().first()}".uppercase()
        }
    }

    fun searchPhotos(items: List<Photo>, query: String): List<Photo> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return items
        val terms = q.split(Regex("\\s+")).filter { it.isNotBlank() }

        fun haystackOf(photo: Photo): String =
            "${photo.title} ${photo.bucketName} ${photo.relativePath} ${photo.mimeType} ${formatDate(photo.epochMillis)}".lowercase()

        fun matchesTerm(haystack: String, term: String): Boolean {
            if (haystack.contains(term)) return true
            if (term.length > 3 && term.endsWith("s") && haystack.contains(term.dropLast(1))) return true
            return false
        }

        val exact = items.filter { photo ->
            val haystack = haystackOf(photo)
            terms.all { term -> matchesTerm(haystack, term) }
        }
        if (exact.isNotEmpty()) return exact

        return items.filter { photo ->
            val haystack = haystackOf(photo)
            terms.any { term -> matchesTerm(haystack, term) }
        }
    }

    private fun resolveShareableUri(context: Context, photo: Photo): Uri? {
        if (photo.uri.startsWith("content://")) {
            return Uri.parse(photo.uri)
        }
        if (photo.uri.startsWith("file://")) {
            val file = File(Uri.parse(photo.uri).path.orEmpty())
            if (file.exists()) {
                return runCatching {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                }.getOrNull()
            }
        }
        return null
    }

    fun sharePhoto(context: Context, photo: Photo, appLabel: String = "XPhotos"): Boolean {
        val contentUri = resolveShareableUri(context, photo) ?: return false
        val mime = photo.mimeType.ifBlank { "image/*" }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, photo.title)
            putExtra(Intent.EXTRA_TEXT, "${photo.title} · Shared from $appLabel")
            clipData = ClipData.newRawUri(photo.title, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Share image with").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(chooser)
            true
        }.getOrDefault(false)
    }

    fun sharePhotos(context: Context, photos: List<Photo>, appLabel: String = "XPhotos"): Boolean {
        if (photos.isEmpty()) return false
        if (photos.size == 1) return sharePhoto(context, photos.first(), appLabel)

        val uris = ArrayList<Uri>(photos.mapNotNull { resolveShareableUri(context, it) })
        if (uris.isEmpty()) return false

        val clip = ClipData.newRawUri("Images", uris.first()).apply {
            uris.drop(1).forEach { addItem(ClipData.Item(it)) }
        }
        val sendIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            putExtra(Intent.EXTRA_TEXT, "${uris.size} images · Shared from $appLabel")
            clipData = clip
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Share ${uris.size} images with").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(chooser)
            true
        }.getOrDefault(false)
    }

    suspend fun setWallpaper(
        context: Context,
        photo: Photo,
        target: WallpaperTarget
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeScaledBitmap(context, photo, maxDim = 2560) ?: return@withContext false
            val wm = WallpaperManager.getInstance(context)
            val flag = when (target) {
                WallpaperTarget.Home -> WallpaperManager.FLAG_SYSTEM
                WallpaperTarget.Lock -> WallpaperManager.FLAG_LOCK
                WallpaperTarget.Both -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }
            wm.setBitmap(bitmap, null, true, flag)
            if (!bitmap.isRecycled) bitmap.recycle()
            true
        }.getOrDefault(false)
    }

    private fun decodeScaledBitmap(context: Context, photo: Photo, maxDim: Int): Bitmap? {
        val uri = Uri.parse(photo.uri)
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openPhotoStream(context, photo, uri)?.use {
            BitmapFactory.decodeStream(it, null, boundsOpts)
        }
        var sampleSize = 1
        while (boundsOpts.outWidth / sampleSize > maxDim || boundsOpts.outHeight / sampleSize > maxDim) {
            sampleSize *= 2
        }
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return openPhotoStream(context, photo, uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        }
    }

    private fun openPhotoStream(context: Context, photo: Photo, uri: Uri) =
        if (photo.uri.startsWith("file://")) {
            File(uri.path.orEmpty()).takeIf { it.exists() }?.inputStream()
        } else {
            context.contentResolver.openInputStream(uri)
        }

    fun getDeviceDiagnostics(context: Context): DeviceDiagnostics {
        val dm = context.resources.displayMetrics
        val versionName = runCatching {
            @Suppress("DEPRECATION")
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            info.versionName ?: "1.0.0"
        }.getOrDefault("1.0.0")

        val stat = StatFs(Environment.getDataDirectory().path)
        return DeviceDiagnostics(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            sdkInt = Build.VERSION.SDK_INT,
            supportedAbis = Build.SUPPORTED_ABIS.joinToString(", "),
            screenMetrics = "${dm.widthPixels}×${dm.heightPixels} (${dm.densityDpi} dpi)",
            packageName = context.packageName,
            versionName = versionName,
            availableStorageBytes = stat.availableBytes,
            totalStorageBytes = stat.totalBytes
        )
    }
}
