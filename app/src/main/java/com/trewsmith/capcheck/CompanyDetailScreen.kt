package com.trewsmith.capcheck

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trewsmith.capcheck.data.model.StockResult
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailScreen(
    stock: StockResult,
    onBack: () -> Unit,
    onAddToWatchlist: () -> Unit,
    portfolioViewModel: PortfolioViewModel,
    isInWatchlist: Boolean,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    hasPriceAlert: Boolean,
    onSetPriceAlert: () -> Unit
) {
    val context = LocalContext.current
    var showAddPortfolioDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            CapCheckTopBar(
                title = stock.symbol ?: "Detail",
                showBackButton = true,
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = onSetPriceAlert) {
                        Icon(
                            if (hasPriceAlert) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                            contentDescription = "Set Price Alert"
                        )
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark"
                        )
                    }
                    IconButton(onClick = {
                        val formattedPrice = FormatUtils.formatPrice(stock.close)
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${stock.symbol} is trading at $formattedPrice — checked on CapCheck")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share stock details")
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share stock details")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = stock.symbol ?: "Unknown",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = "Company Name Placeholder",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = FormatUtils.formatPrice(stock.close),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (stock.close > stock.open) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Price up", tint = Color.Green)
                        } else if (stock.close < stock.open) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Price down", tint = Color.Red)
                        }
                    }
                    Text(
                        text = "NASDAQ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val metrics = listOf(
                            "Close" to FormatUtils.formatPrice(stock.close),
                            "Open" to FormatUtils.formatPrice(stock.open),
                            "High" to FormatUtils.formatPrice(stock.high),
                            "Low" to FormatUtils.formatPrice(stock.low),
                            "Volume" to FormatUtils.formatVolume(stock.volume)
                        )

                        metrics.forEachIndexed { index, (label, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TappableTerm(term = label) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End
                                )
                            }
                            if (index < metrics.size - 1) {
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Fundamentals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        val fundamentals = listOf(
                            "P/E Ratio" to "25.4",
                            "Market Cap" to "2.8T",
                            "EPS" to "6.12",
                            "Dividend Yield" to "0.5%",
                            "Beta" to "1.2"
                        )
                        fundamentals.forEachIndexed { index, (label, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TappableTerm(term = label) {
                                    Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            if (index < fundamentals.size - 1) {
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Price History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Chart coming soon", color = Color.Gray)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Column {
                            val historyData = listOf(
                                listOf("May 10", "180.00", "182.50", "+1.3%"),
                                listOf("May 03", "178.50", "180.00", "+0.8%"),
                                listOf("Apr 26", "182.00", "178.50", "-1.9%"),
                                listOf("Apr 19", "185.00", "182.00", "-1.6%"),
                                listOf("Apr 12", "183.00", "185.00", "+1.1%")
                            )
                            historyData.forEachIndexed { index, row ->
                                val bgColor = if (index % 2 == 0) Color.Transparent else Color.LightGray.copy(alpha = 0.1f)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bgColor)
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(row[0], modifier = Modifier.weight(1f))
                                    Text(row[1], modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                    Text(row[2], modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                    Text(row[3], modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = if (row[3].startsWith("+")) Color.Green else Color.Red)
                                }
                            }
                        }
                        Text(
                            text = "Data delayed 15 minutes",
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            item {
                Text("Key Terms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Volume", "Market Cap", "Close", "Open").forEach { term ->
                        TappableTerm(term = term) {
                            SuggestionChip(onClick = {}, label = { Text(term) })
                        }
                    }
                }
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAddToWatchlist,
                        modifier = Modifier.weight(1f),
                        enabled = !isInWatchlist
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isInWatchlist) "In Watchlist" else "Watchlist")
                    }
                    Button(
                        onClick = { showAddPortfolioDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Portfolio")
                    }
                }
            }
        }
    }

    if (showAddPortfolioDialog) {
        var shares by remember { mutableStateOf("") }
        var avgCost by remember { mutableStateOf(stock.close.toString()) }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddPortfolioDialog = false },
            title = { Text("Add to Portfolio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = shares,
                        onValueChange = { shares = it },
                        label = { Text("Shares") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = avgCost,
                        onValueChange = { avgCost = it },
                        label = { Text("Average Cost") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val s = shares.toDoubleOrNull() ?: 0.0
                    val a = avgCost.toDoubleOrNull() ?: stock.close
                    portfolioViewModel.addHolding(stock.symbol ?: "", "", s, a)
                    showAddPortfolioDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPortfolioDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
