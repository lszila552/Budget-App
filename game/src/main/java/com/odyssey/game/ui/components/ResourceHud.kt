package com.odyssey.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.odyssey.game.state.Act
import com.odyssey.game.ui.theme.AuthorityColor
import com.odyssey.game.ui.theme.CrewColor
import com.odyssey.game.ui.theme.FavorColor
import com.odyssey.game.ui.theme.LoyaltyColor
import com.odyssey.game.ui.theme.PietyColor
import com.odyssey.game.ui.theme.StabilityColor
import com.odyssey.game.ui.theme.SuppliesColor
import com.odyssey.game.ui.theme.TreasuryColor

// Same four underlying numbers, relabeled per act: Crew/Supplies/Favor/Stability at sea, Loyalty/Treasury/Piety/Authority ruling.
@Composable
fun ResourceHud(
    act: Act,
    crew: Int,
    supplies: Int,
    favor: Int,
    stability: Int,
    modifier: Modifier = Modifier,
) {
    val labels = if (act == Act.VOYAGE) {
        listOf("Crew", "Supplies", "Favor", "Stability")
    } else {
        listOf("Loyalty", "Treasury", "Piety", "Authority")
    }
    val colors = if (act == Act.VOYAGE) {
        listOf(CrewColor, SuppliesColor, FavorColor, StabilityColor)
    } else {
        listOf(LoyaltyColor, TreasuryColor, PietyColor, AuthorityColor)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .formalPanel(cornerRadius = 3.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatGauge(labels[0], crew, 100, colors[0], icon = { statIcon(act, 0) })
        StatGauge(labels[1], supplies, 100, colors[1], icon = { statIcon(act, 1) })
        StatGauge(labels[2], favor, 100, colors[2], icon = { statIcon(act, 2) })
        StatGauge(labels[3], stability, 100, colors[3], icon = { statIcon(act, 3) })
    }
}

@Composable
private fun statIcon(act: Act, slot: Int) {
    val iconModifier = Modifier.size(18.dp)
    if (act == Act.VOYAGE) {
        when (slot) {
            0 -> ShipGlyph(modifier = iconModifier)
            1 -> AmphoraGlyph(modifier = iconModifier)
            2 -> LaurelGlyph(modifier = iconModifier)
            else -> ColumnGlyph(modifier = iconModifier)
        }
    } else {
        when (slot) {
            0 -> SwordGlyph(modifier = iconModifier)
            1 -> CoinGlyph(modifier = iconModifier)
            2 -> LaurelGlyph(modifier = iconModifier)
            else -> CrownGlyph(modifier = iconModifier)
        }
    }
}
