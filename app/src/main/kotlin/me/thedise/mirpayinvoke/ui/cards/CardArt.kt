package me.thedise.mirpayinvoke.ui.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import me.thedise.mirpayinvoke.common.Card
import kotlin.random.Random

/** Соотношение сторон банковской карты (ISO 7810) */
const val CARD_RATIO = 1.586f

/** Рисует карту: обычные — картинкой, кастомные — кодом */
@Composable
fun CardArt(card: Card, modifier: Modifier = Modifier) {
    // graphicsLayer: карта кешируется и не перерисовывается на каждом кадре анимации
    val layered = modifier.graphicsLayer()
    if (card.custom) {
        TBankMirSupremeCard(layered)
    } else {
        Image(
            modifier = layered,
            painter = painterResource(card.imageId),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Т-Банк Мир Supreme: чёрный «щит» сверху, шлифованный металл снизу,
 * голубая подсветка по краю щита. Рисуется вертикально и поворачивается
 * на 90° — как на горизонтальной версии карты.
 */
@Composable
fun TBankMirSupremeCard(modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    // Штрихи шлифовки: фиксированный seed, чтобы рисунок был одинаковым
    val brushLines = remember {
        val r = Random(7)
        List(90) { Triple(r.nextFloat(), r.nextFloat() * 0.6f + 0.2f, r.nextBoolean()) }
    }

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        // Вертикальная карта: ширина = h, высота = w
        val pw = h
        val ph = w
        rotate(degrees = -90f, pivot = center) {
            translate(left = (w - pw) / 2f, top = (h - ph) / 2f) {
                // Всё рисуем внутри скруглённой формы карты
                val shape = Path().apply {
                    addRoundRect(RoundRect(0f, 0f, pw, ph, CornerRadius(pw * 0.085f)))
                }
                clipPath(shape) {
                    drawPortrait(pw, ph, textMeasurer, brushLines)
                }
            }
        }
    }
}

private fun DrawScope.drawPortrait(
    pw: Float, ph: Float, tm: TextMeasurer, brushLines: List<Triple<Float, Float, Boolean>>
) {
    val corner = CornerRadius(pw * 0.085f)

    // 1. Металл (вся карта)
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE9EBED), Color(0xFFB4B8BC), Color(0xFFDADDE0),
                Color(0xFF9EA3A8), Color(0xFFD0D3D6)
            ),
            start = Offset(0f, ph * 0.45f), end = Offset(pw, ph)
        ),
        size = Size(pw, ph), cornerRadius = corner
    )
    // Шлифовка: тонкие горизонтальные штрихи
    brushLines.forEach { (y, len, light) ->
        val yy = ph * (0.45f + y * 0.55f)
        val x0 = pw * (1f - len) * (if (light) 0.2f else 0.8f)
        drawLine(
            color = if (light) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.05f),
            start = Offset(x0, yy), end = Offset((x0 + pw * len).coerceAtMost(pw), yy),
            strokeWidth = 1f
        )
    }

    // 2. Чёрный щит
    val shield = Path().apply {
        moveTo(0f, 0f)
        lineTo(pw, 0f)
        lineTo(pw, ph * 0.50f)
        cubicTo(pw * 0.92f, ph * 0.565f, pw * 0.62f, ph * 0.68f, pw * 0.5f, ph * 0.735f)
        cubicTo(pw * 0.38f, ph * 0.68f, pw * 0.08f, ph * 0.565f, 0f, ph * 0.50f)
        close()
    }
    // Тень под щитом — объём
    translate(top = ph * 0.012f) {
        drawPath(shield, Color.Black.copy(alpha = 0.35f))
    }
    drawPath(
        shield,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF39393C), Color(0xFF222224), Color(0xFF161617)),
            start = Offset(0f, 0f), end = Offset(pw * 0.6f, ph * 0.75f)
        )
    )

    // 3. Голубая подсветка по нижней кромке щита
    val edge = Path().apply {
        moveTo(0f, ph * 0.50f)
        cubicTo(pw * 0.08f, ph * 0.565f, pw * 0.38f, ph * 0.68f, pw * 0.5f, ph * 0.735f)
        cubicTo(pw * 0.62f, ph * 0.68f, pw * 0.92f, ph * 0.565f, pw, ph * 0.50f)
    }
    val glow = Color(0xFF3D9BFF)
    drawPath(edge, glow.copy(alpha = 0.22f), style = Stroke(width = pw * 0.05f, cap = StrokeCap.Round))
    drawPath(edge, glow.copy(alpha = 0.55f), style = Stroke(width = pw * 0.022f, cap = StrokeCap.Round))
    drawPath(edge, Color(0xFFBFE0FF), style = Stroke(width = pw * 0.007f, cap = StrokeCap.Round))

    // 4. Чип
    val chipW = pw * 0.2f
    val chipH = pw * 0.22f
    val chipX = pw * 0.48f
    val chipY = ph * 0.11f
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(Color(0xFFF2F2F2), Color(0xFFA9AAAC), Color(0xFFE2E3E4)),
            start = Offset(chipX, chipY), end = Offset(chipX + chipW, chipY + chipH)
        ),
        topLeft = Offset(chipX, chipY), size = Size(chipW, chipH),
        cornerRadius = CornerRadius(chipW * 0.18f)
    )
    val line = Color(0xFF6F7174)
    val sw = pw * 0.006f
    // контакты чипа
    drawLine(line, Offset(chipX + chipW / 2, chipY), Offset(chipX + chipW / 2, chipY + chipH * 0.35f), sw)
    drawLine(line, Offset(chipX + chipW / 2, chipY + chipH * 0.65f), Offset(chipX + chipW / 2, chipY + chipH), sw)
    for (k in 1..2) {
        val y = chipY + chipH * k / 3f
        drawLine(line, Offset(chipX, y), Offset(chipX + chipW * 0.3f, y), sw)
        drawLine(line, Offset(chipX + chipW * 0.7f, y), Offset(chipX + chipW, y), sw)
    }
    drawRoundRect(
        line, topLeft = Offset(chipX + chipW * 0.3f, chipY + chipH * 0.35f),
        size = Size(chipW * 0.4f, chipH * 0.3f), cornerRadius = CornerRadius(sw * 2),
        style = Stroke(sw)
    )

    // 5. Логотип-щит «Т»
    val lx = pw * 0.07f
    val ly = ph * 0.05f
    val ls = pw * 0.14f
    val logo = Path().apply {
        moveTo(lx, ly)
        lineTo(lx + ls, ly)
        lineTo(lx + ls, ly + ls * 0.55f)
        quadraticTo(lx + ls, ly + ls * 0.95f, lx + ls / 2, ly + ls * 1.1f)
        quadraticTo(lx, ly + ls * 0.95f, lx, ly + ls * 0.55f)
        close()
    }
    drawPath(
        logo,
        Brush.linearGradient(
            listOf(Color(0xFFF4F4F4), Color(0xFF9C9EA1)),
            start = Offset(lx, ly), end = Offset(lx + ls, ly + ls)
        )
    )
    val t = tm.measure(
        "T", TextStyle(color = Color(0xFF1C1C1E), fontSize = (ls * 0.72f).toSp(), fontWeight = FontWeight.Black)
    )
    drawText(t, topLeft = Offset(lx + (ls - t.size.width) / 2f, ly + (ls * 1.0f - t.size.height) / 2f))

    // 6. MIR / SUPREME
    val mir = tm.measure(
        "MIR",
        TextStyle(
            color = Color(0xFF2E2F31), fontSize = (pw * 0.16f).toSp(),
            fontWeight = FontWeight.Black, letterSpacing = (pw * 0.004f).toSp()
        )
    )
    val mirY = ph * 0.80f
    drawText(mir, topLeft = Offset(pw * 0.07f, mirY))
    val supreme = tm.measure(
        "SUPREME",
        TextStyle(
            color = Color(0xFF3A3B3D), fontSize = (pw * 0.05f).toSp(),
            fontWeight = FontWeight.Medium, letterSpacing = (pw * 0.012f).toSp()
        )
    )
    drawText(supreme, topLeft = Offset(pw * 0.075f, mirY + mir.size.height * 0.9f))

    // 7. Плашка PREMIUM
    val prem = tm.measure(
        "PREMIUM",
        TextStyle(color = Color.White, fontSize = (pw * 0.038f).toSp(), fontWeight = FontWeight.Bold)
    )
    val padX = pw * 0.025f
    val padY = pw * 0.008f
    val bw = prem.size.width + padX * 2
    val bh = prem.size.height + padY * 2
    val bx = pw * 0.93f - bw
    val by = ph * 0.915f
    drawRoundRect(
        Color(0xFF1E1E20), topLeft = Offset(bx, by), size = Size(bw, bh),
        cornerRadius = CornerRadius(bh / 2)
    )
    drawText(prem, topLeft = Offset(bx + padX, by + padY))

    // 8. Тонкий блик по краю всей карты
    drawRoundRect(
        Color.White.copy(alpha = 0.18f), size = Size(pw, ph), cornerRadius = corner,
        style = Stroke(width = pw * 0.006f)
    )
}
