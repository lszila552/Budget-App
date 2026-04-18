package com.vrijgeld.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.Surface as SurfaceColor
import com.vrijgeld.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ForecastTab(forecast: List<ForecastItem>) {
    if (forecast.isEmpty()) {
        Box(
            Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No upcoming bills in the next 30 days.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        return
    }

    val dateFmt = SimpleDateFormat("EEE d MMM", Locale("nl"))

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(Background),
        contentPadding      = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(forecast, key = { "${it.name}-${it.expectedDate}" }) { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color    = SurfaceColor
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(
                            dateFmt.format(Date(item.expectedDate)),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "€${item.amountCents / 100}",
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "in ${item.daysUntil}d",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.daysUntil <= 3) Accent else TextSecondary
                        )
                    }
                }
            }
        }
    }
}
