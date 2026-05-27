package com.example.util

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

object DocumentUtils {

    fun readTextFromUri(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: ""
        
        val inputStream = contentResolver.openInputStream(uri) ?: return ""
        
        return try {
            if (mimeType.contains("pdf", ignoreCase = true) || uri.path?.endsWith(".pdf", ignoreCase = true) == true) {
                extractTextFromPdfStream(inputStream)
            } else {
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = java.lang.StringBuilder()
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line).append("\n")
                    line = reader.readLine()
                }
                stringBuilder.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Dosya okunamadı: ${e.localizedMessage}"
        } finally {
            inputStream.close()
        }
    }

    private fun extractTextFromPdfStream(inputStream: java.io.InputStream): String {
        val bytes = inputStream.readBytes()
        val textBuilder = StringBuilder()
        
        var i = 0
        val len = bytes.size
        var inTextObject = false
        val currentWord = StringBuilder()
        
        while (i < len) {
            val element = bytes[i].toInt().toChar()
            if (element == '(') {
                inTextObject = true
                currentWord.clear()
            } else if (element == ')' && inTextObject) {
                inTextObject = false
                val word = currentWord.toString()
                if (word.isNotBlank()) {
                    val cleaned = word.replace("\\(", "(")
                        .replace("\\)", ")")
                        .replace("\\\\", "\\")
                        .replace("\\r", " ")
                        .replace("\\n", " ")
                    textBuilder.append(cleaned).append(" ")
                }
            } else if (inTextObject) {
                currentWord.append(element)
            }
            i++
        }
        
        var result = textBuilder.toString().trim()
        result = result.replace(Regex("\\s+"), " ")
        
        if (result.isBlank() || result.length < 50) {
            return "PDF dosyasından doğrudan metin çıkartılamadı (Dosya taranmış bir görsel veya şifreli olabilir).\nLütfen özgeçmiş metninizi kopyalayıp buraya yapıştırın."
        }
        
        return result
    }
}
