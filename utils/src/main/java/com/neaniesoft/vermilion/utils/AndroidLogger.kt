package com.neaniesoft.vermilion.utils

import android.util.Log
import kotlin.reflect.KProperty

class AndroidLogger(
    private val tag: String,
    override var logLevel: Logger.Level = Logger.Level.DEBUG
) : Logger {
    override fun debugIfEnabled(message: () -> String) {
        if (logLevel.level >= Logger.Level.DEBUG.level) {
            Log.d(tag, message())
        }
    }

    override fun infoIfEnabled(message: () -> String) {
        if (logLevel.level >= Logger.Level.INFO.level) {
            Log.i(tag, message())
        }
    }

    override fun warnIfEnabled(throwable: Throwable?, message: () -> String) {
        if (logLevel.level >= Logger.Level.WARN.level) {
            Log.w(tag, message(), throwable)
        }
    }

    override fun errorIfEnabled(throwable: Throwable?, message: () -> String) {
        if (logLevel.level >= Logger.Level.ERROR.level) {
            Log.e(tag, message(), throwable)
        }
    }
}

class LoggerDelegate(private val level: Logger.Level) {
    operator fun getValue(thisRef: Any, property: KProperty<*>): Logger {
        return AndroidLogger(thisRef::class.simpleName ?: "null", level)
    }
}

class AnonymousLoggerDelegate(private val level: Logger.Level, private val tag: String) {
    operator fun getValue(thisRef: Nothing?, property: KProperty<*>): Logger {
        return AndroidLogger(tag, level)
    }
}

fun logger(level: Logger.Level = Logger.Level.DEBUG) = LoggerDelegate(level)
fun anonymousLogger(tag: String, level: Logger.Level = Logger.Level.DEBUG) =
    AnonymousLoggerDelegate(level, tag)

fun getLogger(tag: String, level: Logger.Level = Logger.Level.DEBUG): Logger = AndroidLogger(tag, level)
