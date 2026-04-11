package com.quare.bibleplanner.notification

interface NotificationStringProvider {
    fun getPreparingTitle(versionName: String): String
    fun getPreparingProgress(percent: Int): String
    fun getPreparingStarting(): String
    fun getCompleteTitle(versionName: String): String
    fun getCompleteMessage(): String
    fun getErrorTitle(versionName: String): String
    fun getErrorMessage(): String
    fun getPausedTitle(versionName: String): String
    fun getPausedMessage(percent: Int): String
}
