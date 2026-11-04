package me.thedise.mirpayinvoke.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import me.thedise.mirpayinvoke.R
import me.thedise.mirpayinvoke.common.Card
import me.thedise.mirpayinvoke.ui.theme.MirPayTheme
import me.thedise.mirpayinvoke.ui.widgets.actionChip
import me.thedise.mirpayinvoke.ui.widgets.cardChip
import me.thedise.mirpayinvoke.ui.widgets.hapticChip
import me.thedise.mirpayinvoke.ui.widgets.listHeaderItem
import me.thedise.mirpayinvoke.ui.widgets.timerChip
import me.thedise.mirpayinvoke.ui.widgets.versionChip
import me.thedise.mirpayinvoke.ui.widgets.vibrateEverySecondChip
import me.thedise.mirpayinvoke.ui.widgets.vibrationIntensityChip

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun SettingsScreen(
    timerTicks: Int,
    card: Card,
    vibrationIntensity: Int,
    onChangeTimer: (Int) -> Unit,
    onOpenCards: () -> Unit,
    onChangeVibrationIntensity: (Int) -> Unit,
    onToggleHaptic: Boolean,
    onToggleHapticAction: (Boolean) -> Unit,
    onToggleVibrateEverySecond: Boolean,
    onToggleVibrateEverySecondAction: (Boolean) -> Unit,
    onOpenDemo: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    MirPayTheme {
        val state = rememberScalingLazyListState()

        Scaffold(
            positionIndicator = { PositionIndicator(state) },
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
            timeText = { TimeText(modifier = Modifier.scrollAway(state)) }
        ) {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .rotaryWithScroll(scrollableState = state),
                state = state,
            ) {
                listHeaderItem(R.string.settings_title)

                timerChip(currentTicks = timerTicks, onChangeTimer = onChangeTimer)

                cardChip(currentCard = card, onClick = onOpenCards)

                actionChip(
                    title = R.string.settings_demo_title,
                    secondary = R.string.settings_demo_secondary,
                    icon = R.drawable.ic_mir_pay_24,
                    onClick = onOpenDemo
                )

                listHeaderItem(R.string.settings_haptic_feedback)

                hapticChip(
                    onToggleHaptic = onToggleHaptic,
                    onToggleHapticAction = onToggleHapticAction,
                )

                vibrateEverySecondChip(
                    onToggleVibrateEverySecond = onToggleVibrateEverySecond,
                    onToggleVibrateEverySecondAction = onToggleVibrateEverySecondAction,
                )

                vibrationIntensityChip(
                    currentMs = vibrationIntensity,
                    onChangeVibrationIntensity = onChangeVibrationIntensity,
                )

                listHeaderItem(R.string.settings_about)

                actionChip(
                    title = R.string.settings_diagnostics_title,
                    secondary = R.string.settings_diagnostics_secondary,
                    icon = R.drawable.ic_info_24,
                    onClick = onOpenDiagnostics
                )

                versionChip()
            }
        }
    }
}
