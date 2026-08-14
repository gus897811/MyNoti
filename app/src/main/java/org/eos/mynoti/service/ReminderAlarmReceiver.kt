package org.eos.mynoti.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.eos.mynoti.MyNotiApplication

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0L) return
        val pending = goAsync()
        val app = context.applicationContext as? MyNotiApplication
        if (app == null) {
            pending.finish()
            return
        }
        receiverScope.launch {
            try {
                app.container.reminderRepository.fire(reminderId)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_FIRE = "org.eos.mynoti.action.FIRE_REMINDER"
        const val EXTRA_REMINDER_ID = "reminder_id"
        private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
