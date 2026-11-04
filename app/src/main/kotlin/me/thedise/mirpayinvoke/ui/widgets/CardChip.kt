package me.thedise.mirpayinvoke.ui.widgets

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import me.thedise.mirpayinvoke.R
import me.thedise.mirpayinvoke.common.Card

/** Карта: открывает экран выбора с превью (раньше был ввод сырых имён вроде TINKOFF_BLACK) */
fun ScalingLazyListScope.cardChip(currentCard: Card, onClick: () -> Unit) =
    actionChip(
        title = R.string.settings_card_title,
        secondary = currentCard.titleId,
        icon = R.drawable.ic_credit_card_24,
        onClick = onClick
    )

fun ScalingLazyListScope.actionChip(
    @StringRes title: Int, @StringRes secondary: Int, @DrawableRes icon: Int, onClick: () -> Unit
) = item {
    Chip(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        label = { Text(text = stringResource(title), maxLines = 1) },
        secondaryLabel = { Text(text = stringResource(secondary), maxLines = 1) },
        colors = ChipDefaults.secondaryChipColors(),
        icon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier
                    .size(ChipDefaults.SmallIconSize)
                    .wrapContentSize(align = Alignment.Center),
                tint = MaterialTheme.colors.primary
            )
        }
    )
}
