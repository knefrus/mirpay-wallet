package me.thedise.mirpayinvoke.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import me.thedise.mirpayinvoke.common.AppStorage

private const val ROUTE_MAIN = "main"
private const val ROUTE_CARDS = "cards"
private const val ROUTE_DEMO = "demo"
private const val ROUTE_DIAGNOSTICS = "diagnostics"

@Composable
fun WearApp(
    appStorage: AppStorage, forceSuccess: Boolean, onFinish: () -> Unit
) {
    val navController = rememberSwipeDismissableNavController()

    // Настройки держим в state — экран оплаты сразу видит изменения
    // (раньше значения читались один раз и не обновлялись)
    var card by remember { mutableStateOf(appStorage.card) }
    var timerTicks by remember { mutableIntStateOf(appStorage.timerTicks) }
    var haptic by remember { mutableStateOf(appStorage.haptic) }
    var everySecond by remember { mutableStateOf(appStorage.vibrateEverySecond) }
    var vibrationMs by remember { mutableIntStateOf(appStorage.vibrationIntensity) }

    val round = LocalConfiguration.current.isScreenRound

    SwipeDismissableNavHost(startDestination = ROUTE_MAIN, navController = navController) {
        composable(ROUTE_MAIN) {
            val pagerState = rememberPagerState(pageCount = { 2 })
            HorizontalPager(modifier = Modifier.fillMaxSize(), state = pagerState) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                        .then(if (round) Modifier.clip(CircleShape) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    when (page) {
                        0 -> PaymentScreen(
                            card = card,
                            maxTicks = timerTicks,
                            haptic = haptic,
                            vibrateEverySecond = everySecond,
                            vibrationMs = vibrationMs,
                            forceSuccess = forceSuccess,
                            demo = false,
                            onFinish = onFinish
                        )

                        else -> SettingsScreen(
                            card = card,
                            timerTicks = timerTicks,
                            vibrationIntensity = vibrationMs,
                            onChangeTimer = { timerTicks = it; appStorage.timerTicks = it },
                            onOpenCards = { navController.navigate(ROUTE_CARDS) },
                            onChangeVibrationIntensity = { vibrationMs = it; appStorage.vibrationIntensity = it },
                            onToggleHaptic = haptic,
                            onToggleHapticAction = { haptic = it; appStorage.haptic = it },
                            onToggleVibrateEverySecond = everySecond,
                            onToggleVibrateEverySecondAction = { everySecond = it; appStorage.vibrateEverySecond = it },
                            onOpenDemo = { navController.navigate(ROUTE_DEMO) },
                            onOpenDiagnostics = { navController.navigate(ROUTE_DIAGNOSTICS) }
                        )
                    }
                }
            }
        }

        composable(ROUTE_CARDS) {
            CardPickerScreen(selected = card) {
                card = it
                appStorage.card = it
                navController.popBackStack()
            }
        }

        composable(ROUTE_DEMO) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
                PaymentScreen(
                    card = card,
                    maxTicks = timerTicks,
                    haptic = haptic,
                    vibrateEverySecond = everySecond,
                    vibrationMs = vibrationMs,
                    forceSuccess = false,
                    demo = true,
                    onFinish = { navController.popBackStack() }
                )
            }
        }

        composable(ROUTE_DIAGNOSTICS) {
            DiagnosticsScreen(appStorage = appStorage)
        }
    }
}
