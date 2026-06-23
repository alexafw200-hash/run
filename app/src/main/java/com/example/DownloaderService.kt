package com.example

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class DownloaderService(private val context: Context) {
    fun downloadVideo(url: String): Flow<String> = flow {
        val filesDir = context.filesDir
        val pythonBin = File(filesDir, "python3.13").absolutePath
        val ytDlp = File(filesDir, "yt-dlp").absolutePath
        
        // Environment.getExternalStoragePublicDirectory is the correct way for legacy/standard public downloads writing
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputPath = File(downloadsDir, "%(title)s.%(ext)s").absolutePath

        val command = listOf(pythonBin, ytDlp, url, "-o", outputPath)

        emit("Initializing download...")
        emit("Target URL: $url")
        emit("Output path: $outputPath")
        
        val processBuilder = ProcessBuilder(command)
        val env = processBuilder.environment()
        // Proper environment setup for Python standalone
        env["PYTHONHOME"] = filesDir.absolutePath
        env["PYTHONPATH"] = File(filesDir, "python_stdlib.zip").absolutePath
        
        processBuilder.redirectErrorStream(true)

        try {
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                emit(line ?: "")
            }
            
            val exitCode = process.waitFor()
            emit("Process finished with status code: $exitCode")
        } catch (e: Exception) {
            emit("Error: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)
}
