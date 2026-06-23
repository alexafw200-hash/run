package com.example

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object EngineManager {
    suspend fun initializeEngine(context: Context): Boolean = withContext(Dispatchers.IO) {
        val filesDir = context.filesDir
        val engineFiles = listOf("python3.13", "python_stdlib.zip", "yt-dlp")
        var allSuccess = true

        for (fileName in engineFiles) {
            val targetFile = File(filesDir, fileName)
            // Skip extraction if already there and non-empty
            if (!targetFile.exists() || targetFile.length() == 0L) {
                try {
                    context.assets.open(fileName).use { inputStream ->
                        FileOutputStream(targetFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    // Crucially, grant execution permissions
                    if (fileName == "python3.13" || fileName == "yt-dlp") {
                        targetFile.setExecutable(true, false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    allSuccess = false
                }
            } else {
                // Ensure executable permissions just in case
                if (fileName == "python3.13" || fileName == "yt-dlp") {
                    targetFile.setExecutable(true, false)
                }
            }
        }
        allSuccess
    }
}
