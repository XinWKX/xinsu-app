package com.xinsu.heartrate

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler(
    private val context: Context
) : Thread.UncaughtExceptionHandler {

    private val defaultHandler =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(
        thread: Thread,
        throwable: Throwable
    ) {

        try {

            val writer = StringWriter()

            throwable.printStackTrace(
                PrintWriter(writer)
            )

            val log = buildString {

                appendLine("=== 心宿崩溃日志 ===")

                appendLine()

                appendLine(writer.toString())
            }

            val file = File(
                context.filesDir,
                "crash_log.txt"
            )

            file.writeText(log)

        } catch (_: Exception) {
        }

        defaultHandler?.uncaughtException(
            thread,
            throwable
        )
    }
}
