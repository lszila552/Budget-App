package com.vrijgeld.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrijgeld.data.model.DetectedSubscription
import com.vrijgeld.data.model.RecurrenceFrequency
import com.vrijgeld.domain.annualCost
import com.vrijgeld.domain.monthlyCost
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.AmberWarn
import com.vrijgeld.ui.theme.Background
import com.vrijgeld.ui.theme.Surface as SurfaceColor
import com.vrijgeld.ui.theme.SurfaceVar
import com.vrijgeld.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsTab(
    subscriptions: List<DetectedSubscription>,
    totalMonthlySubCost: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: (Long) -> Unit
) {
    Column(Modifier.fillMaxSize().background(Background)) {
        // Header
        Column(
            modifier            = Modifier.fillMaxWidth().background(SurfaceColor).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = "€${totalMonthlySubCost / 100}/month",
                fontSize   = 32.sp,
                fontWeight = FontWeight.Bold,
                color      = AmberWarn
            )
            Text(
                text  = "€${totalMonthlySubCost * 12 / 100}/year",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        if (subscriptions.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No subscriptions detected yet.\nImport 3+ months of transactions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            return@Column
        }

        LazyColumn(
            contentPadding      = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(subscriptions, key = { it.id }) { sub ->
                SubscriptionRow(sub, onConfirm, onDismiss)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionRow(
    sub: DetectedSubscription,
    onConfirm: (Long) -> Unit,
    onDismiss: (Long) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { onConfirm(sub.id); true }
                SwipeToDismissBoxValue.EndToStart -> { onDismiss(sub.id); true }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state             = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> Accent
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                else -> Color.Transparent
            }
            val label = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> "✓ Confirmed"
                SwipeToDismissBoxValue.EndToStart -> "✗ Not a subscription"
                else -> ""
            }
            Box(
                Modifier.fillMaxSize().background(color).padding(horizontal = 20.dp),
                contentAlignment = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd)
                    Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Text(label, color = Color.Black, fontWeight = FontWeight.SemiBold)
            }
        }
    ) {
        val dateFmt = SimpleDateFormat("d MMM", Locale("nl"))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color    = SurfaceVar
        ) {
            Row(
                modifier            = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(sub.merchantName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        if (sub.isConfirmed) {
                            Spacer(Modifier.width(6.dp))
                            Text("✓", color = Accent, fontSize = 12.sp)
                        }
                    }
                    Text(
                        "€${sub.estimatedAmount / 100} · ${sub.frequency.label()} · next ${dateFmt.format(Date(sub.nextExpectedDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Text(
                    "€${sub.annualCost() / 100}/yr",
                    style      = MaterialTheme.typography.bodySmall,
                    color      = AmberWarn,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun RecurrenceFrequency.label() = when (this) {
    RecurrenceFrequency.WEEKLY    -> "weekly"
    RecurrenceFrequency.MONTHLY   -> "monthly"
    RecurrenceFrequency.QUARTERLY -> "quarterly"
    RecurrenceFrequency.YEARLY    -> "yearly"
}
