package me.thedise.mirpayinvoke.common

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Связь между службой спецвозможностей (видит экран Mir Pay)
 * и экраном оплаты. Всё в одном процессе, поэтому достаточно StateFlow.
 */
object PaymentEvents {
    private val _successAt = MutableStateFlow(0L)

    /** Время (elapsedRealtime) последней успешной оплаты */
    val successAt: StateFlow<Long> = _successAt.asStateFlow()

    /** Когда открыли экран оплаты; 0 — экран закрыт */
    @Volatile
    var sessionStartedAt: Long = 0L

    /** true, пока экран Wallet на переднем плане (а не Mir Pay поверх) */
    @Volatile
    var appResumed: Boolean = false

    fun isSessionActive(): Boolean =
        sessionStartedAt != 0L && SystemClock.elapsedRealtime() - sessionStartedAt < SESSION_TIMEOUT_MS

    /** true, если для текущей сессии успех ещё не отмечен */
    fun signalSuccess(): Boolean {
        if (_successAt.value > sessionStartedAt) return false
        _successAt.value = SystemClock.elapsedRealtime()
        return true
    }

    private const val SESSION_TIMEOUT_MS = 120_000L
}

/** Простой поиск слов «успешной оплаты» в тексте экрана/уведомления Mir Pay */
object SuccessMatcher {
    private val negative = listOf(
        "не прошла", "не выполнен", "ошибка", "отклон", "недостаточно", "отказ",
        "failed", "declined", "error", "denied"
    )
    private val positive = listOf(
        "оплачено", "оплата прошла", "оплата выполнена", "оплата совершена",
        "покупка совершена", "успешно", "успешная", "одобрено",
        "approved", "payment successful", "payment complete"
    )

    fun isSuccess(text: String): Boolean {
        val s = text.lowercase()
        if (negative.any { it in s }) return false
        return positive.any { it in s }
    }
}

/** Вибрация через актуальный API (VibratorManager на Android 12+) */
class Haptics(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun tick(ms: Int) {
        vibrator?.vibrate(
            VibrationEffect.createOneShot(ms.coerceAtLeast(1).toLong(), VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    fun success() {
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 80, 70), -1))
    }
}
