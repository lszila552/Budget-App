package com.odyssey.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.odyssey.game.ui.theme.CrewColor
import com.odyssey.game.ui.theme.FavorColor
import com.odyssey.game.ui.theme.ParchmentPanel
import com.odyssey.game.ui.theme.StabilityColor
import com.odyssey.game.ui.theme.SuppliesColor

@Composable
fun ResourceHud(
    crew: Int,
    supplies: Int,
    favor: Int,
    stability: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .sketchPanel(fill = ParchmentPanel, cornerRadius = 12.dp, seed = 4001)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ResourceMeter("Crew", crew, 100, CrewColor, icon = { ShipGlyph(modifier = Modifier.size(20.dp).padding(end = 4.dp)) })
        ResourceMeter("Supplies", supplies, 100, SuppliesColor, icon = { AmphoraGlyph(modifier = Modifier.size(20.dp).padding(end = 4.dp)) })
        ResourceMeter("Favor of the Gods", favor, 100, FavorColor, icon = { LaurelGlyph(modifier = Modifier.size(20.dp).padding(end = 4.dp)) })
        ResourceMeter("Ithaca's Stability", stability, 100, StabilityColor, icon = { ColumnGlyph(modifier = Modifier.size(20.dp).padding(end = 4.dp)) })
    }
}
