package me.thedise.mirpayinvoke.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import me.thedise.mirpayinvoke.common.AppStorage
import me.thedise.mirpayinvoke.common.EXTRA_PAYMENT_SUCCESS
import me.thedise.mirpayinvoke.common.PREFS_NAME
import me.thedise.mirpayinvoke.common.PaymentEvents
import me.thedise.mirpayinvoke.ui.WearApp
import me.thedise.mirpayinvoke.ui.theme.MirPayTheme

class MainActivity : ComponentActivity() {

    private val appStorage by lazy {
        AppStorage(sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    }

    /** true, если служба вернула нас наверх после успешной оплаты */
    private val forceSuccess = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        PaymentEvents.sessionStartedAt = SystemClock.elapsedRealtime()
        forceSuccess.value = intent.getBooleanExtra(EXTRA_PAYMENT_SUCCESS, false)

        setContent {
            MirPayTheme {
                WearApp(
                    appStorage = appStorage,
                    forceSuccess = forceSuccess.value,
                    onFinish = {
                        // Сразу на циферблат, чтобы под Wallet не остался экран Mir Pay
                        startActivity(
                            Intent(Intent.ACTION_MAIN)
                                .addCategory(Intent.CATEGORY_HOME)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PaymentEvents.appResumed = true
        // Вернулись после Mir Pay, а успех уже был — сразу галочка
        if (PaymentEvents.successAt.value > PaymentEvents.sessionStartedAt) forceSuccess.value = true
    }

    override fun onPause() {
        PaymentEvents.appResumed = false
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra(EXTRA_PAYMENT_SUCCESS, false)) forceSuccess.value = true
    }

    override fun onDestroy() {
        PaymentEvents.sessionStartedAt = 0L
        super.onDestroy()
    }
}
