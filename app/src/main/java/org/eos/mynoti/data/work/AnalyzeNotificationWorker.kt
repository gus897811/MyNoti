package org.eos.mynoti.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.eos.mynoti.MyNotiApplication
import org.eos.mynoti.R
import org.eos.mynoti.data.repository.LlmRepository
import org.eos.mynoti.data.repository.NotificationRepository
import org.eos.mynoti.domain.model.AnalysisStatus
import org.eos.mynoti.domain.model.BatchAnalysisResult
import org.eos.mynoti.domain.model.Notification
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class AnalyzeNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return analysisForegroundInfo(applicationContext)
    }

    override suspend fun doWork(): Result {
        setForeground(analysisForegroundInfo(applicationContext))

        val container = (applicationContext as? MyNotiApplication)?.container
            ?: return Result.retry()

        val notificationRepository = container.notificationRepository
        val llmRepository = container.llmRepository

        try {
            while (true) {
                if (isStopped) {
                    resetInProgressToPending(notificationRepository)
                    return Result.retry()
                }
                notificationRepository.resetStuckAnalysis()
                val pending = notificationRepository.getByAnalysisStatus(
                    AnalysisStatus.PENDING,
                    BATCH_LIMIT
                )
                if (pending.isNotEmpty()) {
                    submitBatch(notificationRepository, llmRepository, pending)
                    continue
                }
                val failed = notificationRepository.getByAnalysisStatus(
                    AnalysisStatus.FAILED,
                    BATCH_LIMIT
                )
                if (failed.isEmpty()) {
                    return Result.success()
                }
                submitBatch(notificationRepository, llmRepository, failed)
                val stillPending = notificationRepository.getByAnalysisStatus(
                    AnalysisStatus.PENDING,
                    1
                ).isNotEmpty()
                if (stillPending) {
                    continue
                }
                val stillFailed = notificationRepository.getByAnalysisStatus(
                    AnalysisStatus.FAILED,
                    1
                ).isNotEmpty()
                return if (stillFailed) Result.retry() else Result.success()
            }
        } catch (error: HttpException) {
            resetInProgressToPending(notificationRepository)
            return if (error.code() in 500..599 || error.code() == 408 || error.code() == 429) {
                Result.retry()
            } else {
                Result.failure()
            }
        } catch (_: IOException) {
            resetInProgressToPending(notificationRepository)
            return Result.retry()
        } catch (_: Exception) {
            resetInProgressToPending(notificationRepository)
            return Result.retry()
        }
    }

    private suspend fun submitBatch(
        notificationRepository: NotificationRepository,
        llmRepository: LlmRepository,
        items: List<Notification>
    ): BatchAnalysisResult {
        items.forEach { notification ->
            notificationRepository.markAnalysisStatus(
                notification.id,
                AnalysisStatus.IN_PROGRESS
            )
        }
        val batch = llmRepository.analyzeBatch(items)
        batch.results.forEach { analysis ->
            notificationRepository.applyAnalysis(analysis)
        }
        batch.failedIds.forEach { id ->
            notificationRepository.markAnalysisStatus(id, AnalysisStatus.FAILED)
        }
        return batch
    }

    private suspend fun resetInProgressToPending(
        notificationRepository: NotificationRepository
    ) {
        runCatching { notificationRepository.resetStuckAnalysis() }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "analyze-notifications"
        const val PERIODIC_WORK_NAME = "analyze-notifications-periodic"
        private const val BATCH_LIMIT = 5
        private const val FOREGROUND_NOTIFICATION_ID = 42
        private const val CHANNEL_ID = "mynoti.analysis"

        private fun analysisForegroundInfo(context: Context): ForegroundInfo {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.analysis_channel_name),
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description = context.getString(R.string.analysis_channel_description)
                    }
                )
            }
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_reminder)
                .setContentTitle(context.getString(R.string.analysis_running))
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ForegroundInfo(
                    FOREGROUND_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                ForegroundInfo(FOREGROUND_NOTIFICATION_ID, notification)
            }
        }
    }
}

object AnalysisScheduler {
    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun enqueue(context: Context) {
        val request = OneTimeWorkRequestBuilder<AnalyzeNotificationWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(networkConstraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(
                AnalyzeNotificationWorker.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
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
