package com.mikix.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mikix.data.MikixRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CloudSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: MikixRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val token = inputData.getString("accessToken") ?: return Result.failure()
        return try {
            repository.syncSessions(token)
            repository.refreshCommunity(token)
            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }
}
