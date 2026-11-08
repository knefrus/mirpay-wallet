package me.thedise.mirpayinvoke.ui

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.thedise.mirpayinvoke.R
import me.thedise.mirpayinvoke.common.Card
import me.thedise.mirpayinvoke.common.Haptics
import me.thedise.mirpayinvoke.common.PaymentEvents
import me.thedise.mirpayinvoke.ui.cards.CARD_RATIO
import me.thedise.mirpayinvoke.ui.cards.CardArt
import me.thedise.mirpayinvoke.ui.widgets.AnimatedCounter
import me.thedise.mirpayinvoke.ui.widgets.LogoBlock
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

private enum class Phase { Waiting, Success, Timeout }

private val SuccessGreen = Color(0xFF34C759)

/**
 * Экран оплаты.
 * Ожидание: от карты расходятся волны, по краю экрана тает кольцо таймера.
 * Успех: карта сжимается, вспыхивает зелёный круг, рисуется галочка, разлетаются искры,
 *        затем экран закрывается (возврат на циферблат).
 * Нажатие на карту = вручную показать успех.
 */
@Composable
fun PaymentScreen(
    card: Card,
    maxTicks: Int,
    haptic: Boolean,
    vibrateEverySecond: Boolean,
    vibrationMs: Int,
    forceSuccess: Boolean,
    demo: Boolean,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { Haptics(context) }
    val startedAt = remember { SystemClock.elapsedRealtime() }
    val successAt by PaymentEvents.successAt.collectAsState()

    var phase by remember { mutableStateOf(Phase.Waiting) }
    var ticksLeft by remember { mutableIntStateOf(maxTicks) }

    val accent = Color(card.colorId)

    // ---------- Логика ----------

    // Таймер
    LaunchedEffect(Unit) {
        if (haptic && !vibrateEverySecond) haptics.tick(vibrationMs)
        while (ticksLeft > 0) {
            delay(1000)
            if (phase != Phase.Waiting) return@LaunchedEffect
            // Пока сверху Mir Pay (идёт оплата) — таймер на паузе
            if (!PaymentEvents.appResumed) continue
            ticksLeft--
            if (haptic && vibrateEverySecond) haptics.tick(vibrationMs)
        }
        if (phase == Phase.Waiting) {
            if (PaymentEvents.successAt.value > startedAt) {
                phase = Phase.Success
                return@LaunchedEffect
            }
            if (haptic && !vibrateEverySecond) haptics.tick(vibrationMs)
            phase = Phase.Timeout
        }
    }

    // Демо из настроек: успех через 2.6 с
    LaunchedEffect(demo) {
        if (demo) {
            delay(2600)
            if (phase == Phase.Waiting) phase = Phase.Success
        }
    }

    // Сигнал от службы (Mir Pay показал «оплачено»)
    LaunchedEffect(successAt, forceSuccess) {
        // Успех важнее таймера: даже если время вышло, показываем галочку
        if (phase != Phase.Success && (forceSuccess || successAt > startedAt)) phase = Phase.Success
    }

    // ---------- Анимации ----------

    val infinite = rememberInfiniteTransition(label = "waiting")
    val wave by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "wave"
    )
    val shine by infinite.animateFloat(
        -0.6f, 1.6f, infiniteRepeatable(tween(2800, easing = LinearEasing)), label = "shine"
    )
    val floatY by infinite.animateFloat(
        -1f, 1f, infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "float"
    )
    val timerProgress by animateFloatAsState(
        targetValue = ticksLeft / maxTicks.coerceAtLeast(1).toFloat(),
        animationSpec = tween(1000, easing = LinearEasing), label = "timer"
    )

    val success = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }

    LaunchedEffect(phase) {
        when (phase) {
            Phase.Success -> {
                exitAlpha.snapTo(1f)
                haptics.success()
                launch { success.animateTo(1f, tween(1100, easing = LinearEasing)) }
                delay(2300)
                exitAlpha.animateTo(0f, tween(300))
                onFinish()
            }

            Phase.Timeout -> {
                exitAlpha.animateTo(0f, tween(400))
                onFinish()
            }

            Phase.Waiting -> Unit
        }
    }

    val sp = success.value
    val cardOut = easeInOut(seg(sp, 0f, 0.35f))        // карта уходит
    val circleIn = easeOutBack(seg(sp, 0.18f, 0.55f))  // зелёный круг
    val checkIn = easeOut(seg(sp, 0.42f, 0.78f))       // галочка
    val burst = easeOut(seg(sp, 0.35f, 1f))            // искры и кольцо
    val textIn = easeOut(seg(sp, 0.6f, 0.9f))          // «Оплачено»

    // ---------- Отрисовка ----------

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = exitAlpha.value }
    ) {
        val side = min(maxWidth, maxHeight)
        val cardW = side * 0.64f
        val cardH = cardW / CARD_RATIO
        val circleR = side * 0.19f

        // Фон, волны, кольцо таймера, круг успеха, искры
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val s = size.minDimension
            val glowColor = lerp(accent, SuccessGreen, circleIn.coerceIn(0f, 1f))

            // Фоновое свечение
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(glowColor.copy(alpha = 0.38f), Color.Transparent),
                    center = c, radius = s * 0.62f
                ),
                radius = s * 0.62f, center = c
            )

            // Волны от карты (контактная оплата)
            val wavesAlpha = 1f - cardOut
            if (wavesAlpha > 0.01f) {
                val cw = cardW.toPx()
                val ch = cardH.toPx()
                val baseCorner = cw * 0.06f
                val maxExpand = s * 0.2f
                for (i in 0 until 3) {
                    val p = (wave + i / 3f) % 1f
                    val e = p * maxExpand
                    val a = (1f - p).pow(1.6f) * 0.75f * wavesAlpha
                    drawRoundRect(
                        color = accent.copy(alpha = a),
                        topLeft = Offset(c.x - cw / 2 - e, c.y - ch / 2 - e),
                        size = Size(cw + 2 * e, ch + 2 * e),
                        cornerRadius = CornerRadius(baseCorner + e),
                        style = Stroke(width = (3.5f - 2.5f * p).dp.toPx())
                    )
                }
            }

            // Кольцо таймера по краю экрана
            val ringW = 4.dp.toPx()
            val inset = ringW / 2 + 2.dp.toPx()
            val ringSize = Size(size.width - inset * 2, size.height - inset * 2)
            val ringAlpha = 1f - cardOut
            drawArc(
                color = Color.White.copy(alpha = 0.10f * ringAlpha),
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = ringSize, style = Stroke(ringW)
            )
            drawArc(
                color = accent.copy(alpha = ringAlpha),
                startAngle = -90f, sweepAngle = 360f * timerProgress, useCenter = false,
                topLeft = Offset(inset, inset), size = ringSize,
                style = Stroke(ringW, cap = StrokeCap.Round)
            )

            // Успех
            if (sp > 0f) {
                val r = circleR.toPx()

                // Расходящееся кольцо
                if (burst > 0f) {
                    drawCircle(
                        color = SuccessGreen.copy(alpha = (1f - burst) * 0.8f),
                        radius = r * (1f + burst * 0.45f), center = c,
                        style = Stroke(width = (6f * (1f - burst) + 1f).dp.toPx())
                    )
                }

                // Искры
                if (burst > 0f && burst < 1f) {
                    val n = 16
                    for (i in 0 until n) {
                        val ang = (i * 2 * PI / n + (i % 2) * 0.12).toFloat()
                        val dist = r * (1.05f + burst * (0.5f + (i % 3) * 0.15f))
                        val pc = when (i % 3) {
                            0 -> SuccessGreen
                            1 -> accent
                            else -> Color.White
                        }
                        drawCircle(
                            color = pc.copy(alpha = (1f - burst)),
                            radius = (3.2f - 1.8f * burst).dp.toPx(),
                            center = Offset(c.x + cos(ang) * dist, c.y + sin(ang) * dist)
                        )
                    }
                }

                // Зелёный круг
                if (circleIn > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF4BE07A), SuccessGreen, Color(0xFF248A3D)),
                            center = Offset(c.x - r * 0.3f, c.y - r * 0.3f), radius = r * 1.6f
                        ),
                        radius = r * circleIn, center = c
                    )
                }

                // Галочка: рисуется линией от начала к концу
                if (checkIn > 0f) {
                    val check = Path().apply {
                        moveTo(c.x - r * 0.38f, c.y + r * 0.02f)
                        lineTo(c.x - r * 0.10f, c.y + r * 0.30f)
                        lineTo(c.x + r * 0.40f, c.y - r * 0.26f)
                    }
                    val pm = PathMeasure()
                    pm.setPath(check, false)
                    val part = Path()
                    pm.getSegment(0f, pm.length * checkIn, part, true)
                    drawPath(
                        part, Color.White,
                        style = Stroke(
                            width = r * 0.15f, cap = StrokeCap.Round, join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        // Верх: логотип
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = side * 0.11f)
                .graphicsLayer { alpha = 1f - cardOut },
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogoBlock()
        }

        // Карта
        val cardShape = RoundedCornerShape(cardW * 0.06f)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(cardW, cardH)
                .graphicsLayer {
                    val sc = 1f - 0.75f * cardOut
                    scaleX = sc
                    scaleY = sc
                    alpha = 1f - cardOut
                    translationY = floatY * 3.dp.toPx() * (1f - cardOut)
                    rotationZ = -2f * (1f - cardOut)
                }
                .shadow(14.dp, cardShape, spotColor = accent, ambientColor = accent)
                .clip(cardShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { if (phase == Phase.Waiting) phase = Phase.Success }
                .drawWithContent {
                    drawContent()
                    // Блик, пробегающий по карте
                    val x = size.width * shine
                    drawRect(
                        Brush.linearGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.22f),
                                Color.Transparent
                            ),
                            start = Offset(x - size.width * 0.25f, 0f),
                            end = Offset(x + size.width * 0.25f, size.height)
                        )
                    )
                }
        ) {
            CardArt(card = card, modifier = Modifier.fillMaxSize())
        }

        // Низ: таймер или «Оплачено»
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = side * 0.1f)
                .graphicsLayer { alpha = 1f - cardOut }
        ) {
            AnimatedCounter(count = ticksLeft)
        }

        Text(
            text = stringResource(R.string.payment_paid),
            style = MaterialTheme.typography.title3,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = circleR + side * 0.13f)
                .graphicsLayer {
                    alpha = textIn
                    translationY = (1f - textIn) * 8.dp.toPx()
                }
        )
    }
}

// ---------- Кривые ----------

private fun seg(x: Float, a: Float, b: Float) = ((x - a) / (b - a)).coerceIn(0f, 1f)

private fun easeOut(t: Float) = 1f - (1f - t).pow(3)

private fun easeInOut(t: Float) = if (t < 0.5f) 4f * t * t * t else 1f - (-2f * t + 2f).pow(3) / 2f

private fun easeOutBack(t: Float): Float {
    if (t <= 0f) return 0f
    val c1 = 1.70158f
    val c3 = c1 + 1f
    return 1f + c3 * (t - 1f).pow(3) + c1 * (t - 1f).pow(2)
}
