package org.eos.mynoti

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.eos.mynoti.ui.theme.MyNotiTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FakeNotificationPoster.ensureChannel(this)
        enableEdgeToEdge()
        setContent {
            MyNotiTheme {
                NotificationScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val notifications by NotificationRepository.notifications.collectAsState()
    var selectedFilter by remember { mutableStateOf(NotiFilter.ALL) }
    var listenerEnabled by remember {
        mutableStateOf(isNotificationListenerEnabled(context))
    }
    var canPostNotifications by remember {
        mutableStateOf(hasPostNotificationPermission(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                listenerEnabled = isNotificationListenerEnabled(context)
                canPostNotifications = hasPostNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        canPostNotifications = granted
        if (granted) {
            FakeNotificationPoster.post(context)
        } else {
            Toast.makeText(context, "알림 전송 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("MyNoti") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            PermissionCard(
                listenerEnabled = listenerEnabled,
                canPostNotifications = canPostNotifications,
                onOpenListenerSettings = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (canPostNotifications) {
                            FakeNotificationPoster.post(context)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            FakeNotificationPoster.post(context)
                        }
                    },
                ) {
                    Text("가짜 알림 생성")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { NotificationRepository.clear() },
                ) {
                    Text("목록 지우기")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == NotiFilter.ALL,
                    onClick = { selectedFilter = NotiFilter.ALL },
                    label = { Text("전체") },
                )
                FilterChip(
                    selected = selectedFilter == NotiFilter.KAKAOTALK,
                    onClick = { selectedFilter = NotiFilter.KAKAOTALK },
                    label = { Text("카카오톡") },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val visibleNotifications = when (selectedFilter) {
                NotiFilter.ALL -> notifications
                NotiFilter.KAKAOTALK -> notifications.filter { it.isKakaoTalk }
            }
            val kakaoCount = notifications.count { it.isKakaoTalk }

            Text(
                text = if (listenerEnabled) {
                    "수신한 알림 ${visibleNotifications.size}개 · 카카오톡 ${kakaoCount}개"
                } else {
                    "알림 접근 권한을 허용하면 카카오톡을 포함한 알림이 표시됩니다."
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = context.getString(R.string.kakao_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(visibleNotifications, key = { it.key }) { item ->
                    NotificationItem(item)
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    listenerEnabled: Boolean,
    canPostNotifications: Boolean,
    onOpenListenerSettings: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (listenerEnabled) "알림 접근 권한: 허용됨" else "알림 접근 권한: 필요함",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (canPostNotifications) "알림 전송 권한: 허용됨" else "알림 전송 권한: 필요함",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (!listenerEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onOpenListenerSettings) {
                    Text("알림 접근 권한 설정")
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(item: CapturedNotification) {
    val timeText = remember(item.postedAtMillis) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(item.postedAtMillis))
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (item.isKakaoTalk) {
            CardDefaults.cardColors(containerColor = KakaoYellow.copy(alpha = 0.35f))
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.appLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.isKakaoTalk) KakaoBrown else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (item.isRemoved) "해제됨 · $timeText" else timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (item.text.isNotBlank()) {
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

private enum class NotiFilter { ALL, KAKAOTALK }

private val KakaoYellow = Color(0xFFFAE100)
private val KakaoBrown = Color(0xFF3C1E1E)

private fun isNotificationListenerEnabled(context: Context): Boolean {
    return NotificationManagerCompat.getEnabledListenerPackages(context)
        .contains(context.packageName)
}

private fun hasPostNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}
