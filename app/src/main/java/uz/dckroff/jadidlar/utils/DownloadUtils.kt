package uz.dckroff.jadidlar.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast

object DownloadUtils {
    
    fun downloadPdf(context: Context, url: String, fileName: String): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        val request = DownloadManager.Request(Uri.parse(url)).apply {
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
