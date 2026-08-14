package org.eos.mynoti.data.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.eos.mynoti.MyNotiApplication
import org.eos.mynoti.domain.model.AnalysisStatus
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class AnalyzeNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as? MyNotiApplication)?.container
            ?: return Result.retry()

        val notificationRepository = container.notificationRepository
        val llmRepository = container.llmRepository

        return try {
            notificationRepository.resetStuckAnalysis()
            val pending = notificationRepository.getPendingAnalysis(BATCH_LIMIT)
            if (pending.isEmpty()) {
                return Result.success()
            }

            pending.forEach { notification ->
                notificationRepository.markAnalysisStatus(
                    notification.id,
                    AnalysisStatus.IN_PROGRESS
                )
            }

            val batch = llmRepository.analyzeBatch(pending)
            batch.results.forEach { analysis ->
                notificationRepository.applyAnalysis(analysis)
            }
            batch.failedIds.forEach { id ->
                notificationRepository.markAnalysisStatus(id, AnalysisStatus.FAILED)
            }

            val remaining = notificationRepository.getPendingAnalysis(1)
            when {
                batch.failedIds.isNotEmpty() || remaining.isNotEmpty() -> Result.retry()
                else -> Result.success()
            }
        } catch (error: HttpException) {
            resetInProgressToPending(notificationRepository)
            if (error.code() in 500..599 || error.code() == 408 || error.code() == 429) {
                Result.retry()
            } else {
                // 401 등 설정 오류는 재시도해도 동일하다.
                Result.failure()
            }
        } catch (_: IOException) {
            resetInProgressToPending(notificationRepository)
            Result.retry()
        } catch (_: Exception) {
            resetInProgressToPending(notificationRepository)
            Result.retry()
        }
    }

    private suspend fun resetInProgressToPending(
        notificationRepository: org.eos.mynoti.data.repository.NotificationRepository
    ) {
        runCatching { notificationRepository.resetStuckAnalysis() }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "analyze-notifications"
        const val PERIODIC_WORK_NAME = "analyze-notifications-periodic"
        private const val BATCH_LIMIT = 20
    }
}

object AnalysisScheduler {
    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun enqueue(context: Context) {
        val request = OneTimeWorkRequestBuilder<AnalyzeNotificationWorker>()
            .setConstraints(networkConstraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(
                AnalyzeNotificationWorker.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request
            )
    }

    fun enqueuePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<AnalyzeNotificationWorker>(15, TimeUnit.MINUTES)
            .setConstraints(networkConstraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                AnalyzeNotificationWorker.PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}
