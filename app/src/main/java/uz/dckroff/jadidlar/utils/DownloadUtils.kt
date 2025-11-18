package uz.dckroff.jadidlar.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object DownloadUtils {
    
    fun convertGoogleDriveUrl(url: String): String {
        return when {
            url.contains("drive.google.com/file/d/") -> {
                val fileIdRegex = "drive\\.google\\.com/file/d/([^/]+)".toRegex()
                val matchResult = fileIdRegex.find(url)
                if (matchResult != null) {
                    val fileId = matchResult.groupValues[1]
                    "https://drive.google.com/uc?export=download&id=$fileId&confirm=t"
                } else {
                    url
                }
            }
            url.contains("drive.google.com/uc?") && !url.contains("confirm=") -> {
                "$url&confirm=t"
            }
            else -> url
        }
    }
    
    fun getPdfCacheFile(context: Context, bookId: String): File {
        val cacheDir = File(context.cacheDir, "pdfs")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val safeFileName = bookId.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        return File(cacheDir, "$safeFileName.pdf")
    }
    
    suspend fun downloadPdfToCache(context: Context, url: String, bookId: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val cacheFile = getPdfCacheFile(context, bookId)
                
                if (cacheFile.exists() && cacheFile.length() > 0) {
                    return@withContext cacheFile
                }
                
                val directUrl = convertGoogleDriveUrl(url)
                val connection = URL(directUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                
                connection.connect()
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.use { input ->
                        FileOutputStream(cacheFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    cacheFile
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    fun downloadPdf(context: Context, url: String, fileName: String): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val directUrl = convertGoogleDriveUrl(url)
        
        val request = DownloadManager.Request(Uri.parse(directUrl)).apply {
            setTitle(fileName)
            setDescription("Downloading PDF...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Jadidlar/$fileName")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        
        return try {
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            -1
        }
    }
}
