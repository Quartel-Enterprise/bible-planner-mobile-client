package com.quare.bibleplanner.core.books.data.datasource

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class AndroidResourceReader(
    private val context: Context,
) : ResourceReader {

    override suspend fun readResource(path: String): String = withContext(Dispatchers.IO) {
        // Remove "assets/" prefix for Android AssetManager
        val assetPath = path.removePrefix("assets/")
        
        context.assets.open(assetPath).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).readText()
        }
    }
}
