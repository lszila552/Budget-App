package com.vrijgeld.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val KEYPAD_ROWS = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf(".", "0", "⌫"),
)

@Composable
fun NumericKeypad(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KEYPAD_ROWS.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { key ->
                    TextButton(
                        onClick  = { onKeyPress(key) },
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text(
                            text       = key,
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

fun applyKeypadInput(current: String, key: String): String = when (key) {
    "⌫"  -> if (current.isEmpty()) "" else current.dropLast(1)
    "."  -> if ("." in current) current else "$current."
    else -> {
        val newVal = current + key
        if (newVal.contains(".")) {
            val parts = newVal.split(".")
            if (parts[1].length > 2) current else newVal
        } else newVal
    }
}
