package com.vrijgeld.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrijgeld.data.model.Category
import com.vrijgeld.ui.theme.Accent
import com.vrijgeld.ui.theme.SurfaceVar

@Composable
fun CategoryGrid(
    categories: List<Category>,
    selectedId: Long?,
    onSelect: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns              = GridCells.Fixed(4),
        modifier             = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement  = Arrangement.spacedBy(8.dp),
        contentPadding       = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(categories, key = { it.id }) { cat ->
            val selected = cat.id == selectedId
            Card(
                onClick  = { onSelect(cat) },
                colors   = CardDefaults.cardColors(containerColor = SurfaceVar),
                border   = if (selected) BorderStroke(2.dp, Accent) else null,
                modifier = Modifier.aspectRatio(1f)
            ) {
                Column(
                    modifier            = Modifier.fillMaxSize().padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(cat.icon, fontSize = 22.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text      = cat.name,
                        style     = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        maxLines  = 2,
                        overflow  = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
