package com.mikix.data

import android.content.Context
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExporter @Inject constructor() {
    fun exportSessions(context: Context, sessions: List<WorkoutSession>): File {
        val file = File(context.cacheDir, "mikix_sessions.csv")
        file.printWriter().use { out ->
            out.println("id,startedAt,endedAt,totalVolume,notes,rpe")
            sessions.forEach { out.println("${it.id},${it.startedAt},${it.endedAt ?: ""},${it.totalVolume},\"${it.notes}\",${it.perceivedEffort}") }
        }
        return file
    }
}
