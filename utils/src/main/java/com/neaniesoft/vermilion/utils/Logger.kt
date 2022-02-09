package com.neaniesoft.vermilion.utils

interface Logger {
    var logLevel: Level

    fun debugIfEnabled(message: () -> String)
    fun infoIfEnabled(message: () -> String)
    fun warnIfEnabled(throwable: Throwable? = null, message: () -> String)
    fun errorIfEnabled(throwable: Throwable? = null, message: () -> String)

    enum class Level(val level: Int) {
        ERROR(0),
        WARN(1),
        INFO(2),
        DEBUG(3)
    }
}
