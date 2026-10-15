package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, prefix: String = "img"): String {
        val directory = File(context.filesDir, "report_images")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return Uri.fromFile(file).toString()
    }

    fun saveUriToInternalStorage(context: Context, uri: Uri, prefix: String = "photo"): String? {
        return try {
            val directory = File(context.filesDir, "report_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val extension = if (uri.toString().endsWith(".png")) "png" else "jpg"
            val file = File(directory, "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$extension")
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createTempImageUri(context: Context): Pair<Uri, File> {
        val directory = File(context.cacheDir, "camera_photos")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "temp_capture_${System.currentTimeMillis()}.jpg")
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(uri, file)
    }

    fun loadScaledBitmap(context: Context, uriString: String?, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            var inputStream: InputStream? = if (uri.scheme == "file") {
                File(uri.path ?: "").inputStream()
            } else {
                context.contentResolver.openInputStream(uri)
            }
            
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate inSampleSize
            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize

            inputStream = if (uri.scheme == "file") {
                File(uri.path ?: "").inputStream()
            } else {
                context.contentResolver.openInputStream(uri)
            }
            val decodedBitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Fix orientation if needed
            if (decodedBitmap != null && uri.scheme == "file") {
                val exif = ExifInterface(uri.path ?: "")
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                rotateBitmap(decodedBitmap, orientation)
            } else {
                decodedBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
