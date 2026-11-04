package me.thedise.mirpayinvoke.ui

import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import me.thedise.mirpayinvoke.R
import me.thedise.mirpayinvoke.common.AppStorage

/** Показывает, включена ли служба и что она последним увидела в Mir Pay */
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun DiagnosticsScreen(appStorage: AppStorage) {
    val context = LocalContext.current
    val serviceOn = remember {
        val enabled = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        enabled.contains(context.packageName)
    }
    val lastText = remember { appStorage.lastMirPayText }
    val state = rememberScalingLazyListState()

    Scaffold(positionIndicator = { PositionIndicator(state) }) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .rotaryWithScroll(scrollableState = state),
            state = state
        ) {
            item {
                Text(
                    text = stringResource(if (serviceOn) R.string.diag_service_on else R.string.diag_service_off),
                    color = if (serviceOn) Color(0xFF34C759) else MaterialTheme.colors.error,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.caption1,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )
            }
            item {
                Text(
                    text = stringResource(R.string.diag_last_text),
                    style = MaterialTheme.typography.caption2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Text(
                    text = lastText.ifBlank { stringResource(R.string.diag_empty) },
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }
            item {
                Text(
                    text = stringResource(R.string.diag_hint),
                    style = MaterialTheme.typography.caption3,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )
            }
        }
    }
}
