package me.thedise.mirpayinvoke.main

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import me.thedise.mirpayinvoke.common.AppStorage
import me.thedise.mirpayinvoke.common.EXTRA_PAYMENT_SUCCESS
import me.thedise.mirpayinvoke.common.GOOGLE_WALLET_PACKAGE
import me.thedise.mirpayinvoke.common.MIR_PAY_PACKAGE
import me.thedise.mirpayinvoke.common.PREFS_NAME
import me.thedise.mirpayinvoke.common.PaymentEvents
import me.thedise.mirpayinvoke.common.SuccessMatcher

/**
 * 1) Как раньше: если открылся Google Wallet — закрываем его и открываем наш экран.
 * 2) Новое: читаем текст экрана и уведомлений Mir Pay. Если там «оплачено/успешно»
 *    во время открытого экрана оплаты — показываем анимацию успеха.
 */
class MainAccessibilityService : AccessibilityService() {

    private val storage by lazy {
        AppStorage(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    }
    private var lastText = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Старый код падал здесь: packageName бывает null, а стоял `!!`
        val pkg = event?.packageName?.toString() ?: return
        when (pkg) {
            GOOGLE_WALLET_PACKAGE -> if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                performGlobalAction(GLOBAL_ACTION_BACK)
                launchApp(success = false)
            }

            MIR_PAY_PACKAGE -> handleMirPay(event)
        }
    }

    private fun handleMirPay(event: AccessibilityEvent) {
        val text = collectText(event).trim()
        if (text.isEmpty() || text == lastText) return
        lastText = text
        storage.lastMirPayText = text.take(300)

        if (PaymentEvents.isSessionActive() && SuccessMatcher.isSuccess(text)) {
            if (PaymentEvents.signalSuccess()) launchApp(success = true)
        }
    }

    private fun collectText(event: AccessibilityEvent): String {
        val parts = mutableListOf<String>()
        event.text?.forEach { t -> if (t != null) parts += t.toString() }
        event.contentDescription?.let { parts += it.toString() }

        // Уведомление Mir Pay: заголовок и текст лежат в extras
        val notification = event.parcelableData as? Notification
        notification?.extras?.let { extras ->
            extras.getCharSequence(Notification.EXTRA_TITLE)?.let { parts += it.toString() }
            extras.getCharSequence(Notification.EXTRA_TEXT)?.let { parts += it.toString() }
        }

        // Окно Mir Pay: обходим дерево элементов (с ограничением, чтобы не тормозить)
        if (notification == null) {
            event.source?.let { collectNodeText(it, parts, intArrayOf(0)) }
        }
        return parts.filter { it.isNotBlank() }.distinct().joinToString(" | ")
    }

    private fun collectNodeText(node: AccessibilityNodeInfo, out: MutableList<String>, counter: IntArray) {
        if (counter[0]++ > 80) return
        node.text?.let { out += it.toString() }
        node.contentDescription?.let { out += it.toString() }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectNodeText(it, out, counter) }
        }
    }

    @SuppressLint("WearRecents")
    private fun launchApp(success: Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (success) {
                // Вернуть уже открытый экран оплаты наверх и показать галочку
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(EXTRA_PAYMENT_SUCCESS, true)
            } else {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        }
        startActivity(intent)
    }

    override fun onInterrupt() = Unit
}
