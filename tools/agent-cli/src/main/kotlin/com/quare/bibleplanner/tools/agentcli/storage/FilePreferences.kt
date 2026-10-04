package com.quare.bibleplanner.tools.agentcli.storage

import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Properties
import java.util.prefs.AbstractPreferences

class FilePreferences(
    private val directory: File,
    parent: FilePreferences?,
    name: String,
) : AbstractPreferences(parent, name) {
    private val file = File(directory, FILE_NAME)
    private val properties = Properties().apply {
        if (file.exists()) file.inputStream().use(::load)
    }

    override fun putSpi(
        key: String,
        value: String,
    ) {
        properties.setProperty(key, value)
        save()
    }

    override fun getSpi(key: String): String? = properties.getProperty(key)

    override fun removeSpi(key: String) {
        properties.remove(key)
        save()
    }

    override fun removeNodeSpi() {
        directory.deleteRecursively()
    }

    override fun keysSpi(): Array<String> = properties.stringPropertyNames().toTypedArray()

    override fun childrenNamesSpi(): Array<String> = directory
        .listFiles(File::isDirectory)
        .orEmpty()
        .map { child -> URLDecoder.decode(child.name, Charsets.UTF_8) }
        .toTypedArray()

    override fun childSpi(name: String): AbstractPreferences = FilePreferences(
        parent = this,
        name = name,
        directory = File(directory, URLEncoder.encode(name, Charsets.UTF_8)),
    )

    override fun syncSpi() {}

    override fun flushSpi() {}

    private fun save() {
        directory.mkdirs()
        file.outputStream().use { stream -> properties.store(stream, null) }
    }

    private companion object {
        const val FILE_NAME = "prefs.properties"
    }
}
