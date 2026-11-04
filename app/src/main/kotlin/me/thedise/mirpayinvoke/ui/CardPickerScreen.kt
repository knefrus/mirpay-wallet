package me.thedise.mirpayinvoke.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import me.thedise.mirpayinvoke.R
import me.thedise.mirpayinvoke.common.Card
import me.thedise.mirpayinvoke.ui.cards.CARD_RATIO
import me.thedise.mirpayinvoke.ui.cards.CardArt
import me.thedise.mirpayinvoke.ui.widgets.listHeaderItem

/** Выбор карты с живыми превью */
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun CardPickerScreen(selected: Card, onSelect: (Card) -> Unit) {
    val state = rememberScalingLazyListState(initialCenterItemIndex = Card.entries.indexOf(selected) + 1)

    Scaffold(positionIndicator = { PositionIndicator(state) }) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .rotaryWithScroll(scrollableState = state),
            state = state,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listHeaderItem(R.string.settings_choose_card)

            items(Card.entries) { card ->
                val isSelected = card == selected
                val shape = RoundedCornerShape(10.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(card) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .aspectRatio(CARD_RATIO)
                            .clip(shape)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colors.primary else Color.Transparent,
                                shape = shape
                            )
                    ) {
                        CardArt(card = card, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(card.titleId),
                        style = MaterialTheme.typography.caption2,
                        textAlign = TextAlign.Center,
                        color = if (isSelected) MaterialTheme.colors.primary else Color.White
                    )
                }
            }
        }
    }
}
