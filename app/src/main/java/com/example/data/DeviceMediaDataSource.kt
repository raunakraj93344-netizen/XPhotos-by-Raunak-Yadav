package com.example.data

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.example.model.ExifDetails
import com.example.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

sealed interface DeleteOutcome {
    data object Deleted : DeleteOutcome
    data class RequiresConfirmation(val intentSender: IntentSender) : DeleteOutcome
    data object Failed : DeleteOutcome
}

class DeviceMediaDataSource(private val context: Context) {

    fun hasMediaPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val full = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
            val partial = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED
            full || partial
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isPartialPermissionOnly(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun loadDevicePhotos(): List<Photo> = withContext(Dispatchers.IO) {
        if (!hasMediaPermission()) return@withContext emptyList()

        val result = mutableListOf<Photo>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = buildList {
            add(MediaStore.Images.Media._ID)
            add(MediaStore.Images.Media.DISPLAY_NAME)
            add(MediaStore.Images.Media.BUCKET_ID)
            add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            add(MediaStore.Images.Media.WIDTH)
            add(MediaStore.Images.Media.HEIGHT)
            add(MediaStore.Images.Media.DATE_TAKEN)
            add(MediaStore.Images.Media.DATE_MODIFIED)
            add(MediaStore.Images.Media.DATE_ADDED)
            add(MediaStore.Images.Media.SIZE)
            add(MediaStore.Images.Media.MIME_TYPE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Images.Media.RELATIVE_PATH)
            } else {
                @Suppress("DEPRECATION")
                add(MediaStore.Images.Media.DATA)
            }
        }.toTypedArray()

        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        runCatching {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val bucketIdCol = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
                val bucketNameCol = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val widthCol = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                val dateTakenCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                val dateModifiedCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                val sizeCol = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                val mimeCol = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
                val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
                } else -1
                @Suppress("DEPRECATION")
                val dataCol = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                } else -1

                while (cursor.moveToNext()) {
                    val mediaId = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        mediaId
                    )
                    val displayName = if (nameCol >= 0) cursor.getString(nameCol).orEmpty() else ""
                    val cleanTitle = displayName.substringBeforeLast('.').ifBlank { "Photo $mediaId" }
                    val bucketId = if (bucketIdCol >= 0) {
                        cursor.getString(bucketIdCol).orEmpty().ifBlank { "internal" }
                    } else "internal"
                    val bucketName = if (bucketNameCol >= 0) {
                        cursor.getString(bucketNameCol).orEmpty().ifBlank { "Device" }
                    } else "Device"

                    val width = (if (widthCol >= 0) cursor.getInt(widthCol) else 0).let {
                        if (it > 0) it else 1080
                    }
                    val height = (if (heightCol >= 0) cursor.getInt(heightCol) else 0).let {
                        if (it > 0) it else 1080
                    }

                    val taken = if (dateTakenCol >= 0) cursor.getLong(dateTakenCol) else 0L
                    val modifiedSec = if (dateModifiedCol >= 0) cursor.getLong(dateModifiedCol) else 0L
                    val addedSec = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) else 0L
                    val epochMillis = when {
                        taken > 0L -> taken
                        modifiedSec > 0L -> modifiedSec * 1000L
                        addedSec > 0L -> addedSec * 1000L
                        else -> System.currentTimeMillis()
                    }

                    val sizeBytes = if (sizeCol >= 0) cursor.getLong(sizeCol).coerceAtLeast(0L) else 0L
                    val mimeType = if (mimeCol >= 0) {
                        cursor.getString(mimeCol).orEmpty().ifBlank { "image/jpeg" }
                    } else "image/jpeg"

                    val relPath = when {
                        relPathCol >= 0 -> {
                            val dir = cursor.getString(relPathCol).orEmpty()
                            if (displayName.isNotBlank()) "$dir$displayName" else dir.ifBlank { bucketName }
                        }
                        dataCol >= 0 -> cursor.getString(dataCol).orEmpty().ifBlank { bucketName }
                        else -> bucketName
                    }

                    result.add(
                        Photo(
                            id = "ms-$mediaId",
                            mediaStoreId = mediaId,
                            uri = contentUri.toString(),
                            thumb = contentUri.toString(),
                            width = width,
                            height = height,
                            title = cleanTitle,
                            bucketId = bucketId,
                            bucketName = bucketName,
                            relativePath = relPath,
                            mimeType = mimeType,
                            sizeBytes = sizeBytes,
                            epochMillis = epochMillis,
                            isCustom = false
                        )
                    )
                }
            }
        }
        result
    }

    suspend fun readExif(photo: Photo): ExifDetails = withContext(Dispatchers.IO) {
        runCatching {
            val parsedUri = Uri.parse(photo.uri)
            val stream = if (photo.uri.startsWith("file://")) {
                File(parsedUri.path.orEmpty()).takeIf { it.exists() }?.inputStream()
            } else {
                context.contentResolver.openInputStream(parsedUri)
            }
            stream?.use { input ->
                val exif = ExifInterface(input)
                val make = exif.getAttribute(ExifInterface.TAG_MAKE)?.trim().orEmpty()
                val model = exif.getAttribute(ExifInterface.TAG_MODEL)?.trim().orEmpty()
                val camera = when {
                    make.isNotBlank() && model.isNotBlank() -> {
                        if (model.startsWith(make, ignoreCase = true)) model else "$make $model"
                    }
                    model.isNotBlank() -> model
                    make.isNotBlank() -> make
                    photo.isCustom -> "In-App Camera"
                    else -> "Device Camera"
                }

                val lens = exif.getAttribute("LensModel")
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: "Standard lens"

                val iso = exif.getAttribute("PhotographicSensitivity")
                    ?: exif.getAttribute("ISOSpeedRatings")
                    ?: "—"

                val fNum = exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, -1.0)
                val aperture = if (fNum > 0.0) {
                    String.format(Locale.US, "f/%.1f", fNum)
                } else "—"

                val exp = exif.getAttributeDouble(ExifInterface.TAG_EXPOSURE_TIME, -1.0)
                val shutter = when {
                    exp <= 0.0 -> "—"
                    exp < 1.0 -> "1/${(1.0 / exp).toInt().coerceAtLeast(1)}s"
                    else -> String.format(Locale.US, "%.1fs", exp)
                }

                val focal = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, -1.0)
                val focalLength = if (focal > 0.0) {
                    String.format(Locale.US, "%.0fmm", focal)
                } else "—"

                val rawPairs = buildList {
                    fun addTag(label: String, key: String) {
                        exif.getAttribute(key)?.takeIf { it.isNotBlank() }?.let { add(label to it) }
                    }
                    addTag("DateTimeOriginal", ExifInterface.TAG_DATETIME_ORIGINAL)
                    addTag("Software", ExifInterface.TAG_SOFTWARE)
                    addTag("Orientation", ExifInterface.TAG_ORIENTATION)
                    addTag("WhiteBalance", ExifInterface.TAG_WHITE_BALANCE)
                    addTag("Flash", ExifInterface.TAG_FLASH)
                    addTag("ColorSpace", ExifInterface.TAG_COLOR_SPACE)
                    addTag("PixelXDim", ExifInterface.TAG_PIXEL_X_DIMENSION)
                    addTag("PixelYDim", ExifInterface.TAG_PIXEL_Y_DIMENSION)
                    addTag("GPS Lat", ExifInterface.TAG_GPS_LATITUDE)
                    addTag("GPS Lon", ExifInterface.TAG_GPS_LONGITUDE)
                }

                ExifDetails(
                    camera = camera,
                    lens = lens,
                    iso = iso,
                    aperture = aperture,
                    shutter = shutter,
                    focalLength = focalLength,
                    rawTags = rawPairs
                )
            } ?: ExifDetails()
        }.getOrDefault(ExifDetails())
    }

    suspend fun deletePhotosFromDevice(photos: List<Photo>): DeleteOutcome = withContext(Dispatchers.IO) {
        if (photos.isEmpty()) return@withContext DeleteOutcome.Deleted

        // Delete local app-captured files first
        val customFiles = photos.filter { it.uri.startsWith("file://") }
        customFiles.forEach { photo ->
            runCatching {
                File(Uri.parse(photo.uri).path.orEmpty()).takeIf { it.exists() }?.delete()
            }
        }

        val mediaUris = photos.mapNotNull { photo ->
            photo.mediaStoreId?.let {
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it)
            } ?: if (photo.uri.startsWith("content://")) Uri.parse(photo.uri) else null
        }

        if (mediaUris.isEmpty()) {
            return@withContext DeleteOutcome.Deleted
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return@withContext runCatching {
                val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, mediaUris)
                DeleteOutcome.RequiresConfirmation(pendingIntent.intentSender)
            }.getOrElse { DeleteOutcome.Failed }
        }

        return@withContext try {
            var allDeleted = true
            mediaUris.forEach { uri ->
                val rows = context.contentResolver.delete(uri, null, null)
                if (rows <= 0) allDeleted = false
            }
            if (allDeleted) DeleteOutcome.Deleted else DeleteOutcome.Failed
        } catch (security: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && security is RecoverableSecurityException) {
                DeleteOutcome.RequiresConfirmation(security.userAction.actionIntent.intentSender)
            } else {
                DeleteOutcome.Failed
            }
        } catch (_: Throwable) {
            DeleteOutcome.Failed
        }
    }
}
