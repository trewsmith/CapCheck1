package com.trewsmith.capcheck

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trewsmith.capcheck.BuildConfig
import com.trewsmith.capcheck.data.model.PortfolioHolding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    portfolioViewModel: PortfolioViewModel,
    onHoldingClick: (PortfolioHolding) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val holdings by portfolioViewModel.holdings.collectAsState()
    val isLoading by portfolioViewModel.isLoading.collectAsState()
    val error by portfolioViewModel.error.collectAsState()
    val sortOrder by portfolioViewModel.sortOrder.collectAsState()
    val haptic = LocalHapticFeedback.current

    var showDialog by remember { mutableStateOf(false) }
    var editingHolding by remember { mutableStateOf<PortfolioHolding?>(null) }
    var holdingToDelete by remember { mutableStateOf<PortfolioHolding?>(null) }
    var timeFilter by remember { mutableStateOf("All") }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { portfolioViewModel.loadHoldings() }
    )

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isLoading,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
            ) {
                FloatingActionButton(onClick = { 
                    editingHolding = null
                    showDialog = true 
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Holding")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .pullRefresh(pullRefreshState)) {
            
            val totalValue = holdings.sumOf { it.currentPrice * it.shares }
            val totalCost = holdings.sumOf { it.avgCost * it.shares }
            val totalGainLoss = totalValue - totalCost
            val totalGainLossPercent = if (totalCost > 0) (totalGainLoss / totalCost) * 100 else 0.0
            val isLoaded = holdings.isNotEmpty() && holdings.all { it.currentPrice > 0 }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { /* Export CSV Task 11 logic here */ }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                    }
                }

                if (isLoaded) {
                    PortfolioPieChart(data = holdings.associate { it.symbol to (it.currentPrice * it.shares) })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Value", fontSize = 14.sp)
                        AnimatedCounter(targetValue = totalValue, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val color = when {
                                totalGainLossPercent > 1.0 -> Color(0xFF4CAF50)
                                totalGainLossPercent < -1.0 -> Color.Red
                                else -> Color.Gray
                            }
                            Text(FormatUtils.formatPrice(totalGainLoss), color = color, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("(${FormatUtils.formatPercent(totalGainLossPercent)})", color = color, fontWeight = FontWeight.Medium)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Your portfolio: ${FormatUtils.formatPercent(totalGainLossPercent)}", style = MaterialTheme.typography.labelSmall)
                            Text("S&P 500: +10.00%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("1W", "1M", "3M", "1Y", "All").forEach { filter ->
                        FilterChip(
                            selected = timeFilter == filter,
                            onClick = { timeFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
                if (timeFilter != "All") {
                    Text("Data coming soon for $timeFilter period", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = sortOrder == SortOrder.BY_SYMBOL, onClick = { portfolioViewModel.setSortOrder(SortOrder.BY_SYMBOL) }, label = { Text("Symbol") })
                    FilterChip(selected = sortOrder == SortOrder.BY_GAIN_LOSS, onClick = { portfolioViewModel.setSortOrder(SortOrder.BY_GAIN_LOSS) }, label = { Text("Gain/Loss") })
                    FilterChip(selected = sortOrder == SortOrder.BY_VALUE, onClick = { portfolioViewModel.setSortOrder(SortOrder.BY_VALUE) }, label = { Text("Value") })
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (holdings.isEmpty() && !isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No holdings yet", fontWeight = FontWeight.Bold)
                        Canvas(modifier = Modifier.size(100.dp).padding(16.dp)) {
                            val pathColor = Color(0xFF4CAF50)
                            drawLine(pathColor, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height * 0.7f), strokeWidth = 4f)
                            drawLine(pathColor, start = androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height * 0.7f), end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.8f), strokeWidth = 4f)
                            drawLine(pathColor, start = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.8f), end = androidx.compose.ui.geometry.Offset(size.width * 0.75f, size.height * 0.4f), strokeWidth = 4f)
                            drawLine(pathColor, start = androidx.compose.ui.geometry.Offset(size.width * 0.75f, size.height * 0.4f), end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.1f), strokeWidth = 4f)
                        }
                        Text("Tap + to track your first stock", fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(holdings, key = { it.symbol }) { holding ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        holdingToDelete = holding
                                        true
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent
                                    Box(modifier = Modifier.fillMaxSize().background(color).padding(horizontal = 24.dp), contentAlignment = Alignment.CenterEnd) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                    }
                                },
                                enableDismissFromStartToEnd = false
                            ) {
                                Card(modifier = Modifier.fillMaxWidth().clickable { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onHoldingClick(holding) 
                                }) {
                                    ListItem(
                                        headlineContent = { Text(holding.symbol, fontWeight = FontWeight.Bold) },
                                        supportingContent = {
                                            Column {
                                                Text("${FormatUtils.formatShares(holding.shares)} shares @ ${FormatUtils.formatPrice(holding.avgCost)}")
                                                if (holding.notes.isNotEmpty()) Text(holding.notes, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
                                            }
                                        },
                                        trailingContent = {
                                            val gainLoss = (holding.currentPrice - holding.avgCost) * holding.shares
                                            val color = when {
                                                gainLoss > 1.0 -> Color(0xFF4CAF50)
                                                gainLoss < -1.0 -> Color.Red
                                                else -> Color.Gray
                                            }
                                            IconButton(onClick = { 
                                                editingHolding = holding
                                                showDialog = true 
                                            }) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            PullRefreshIndicator(isLoading, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }

    if (showDialog) {
        AddHoldingDialog(
            holding = editingHolding,
            onDismiss = { showDialog = false },
            onConfirm = { symbol, name, shares, cost, notes ->
                if (editingHolding == null) portfolioViewModel.addHolding(symbol, name, shares, cost)
                else portfolioViewModel.editHolding(editingHolding!!.copy(shares = shares, avgCost = cost, notes = notes))
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch { snackbarHostState.showSnackbar("Holding ${if (editingHolding == null) "added" else "updated"}") }
                showDialog = false
            }
        )
    }

    if (holdingToDelete != null) {
        AlertDialog(
            onDismissRequest = { holdingToDelete = null },
            title = { Text("Remove Holding") },
            text = { Text("Remove ${holdingToDelete?.symbol} from portfolio?") },
            confirmButton = {
                Button(onClick = {
                    holdingToDelete?.let { portfolioViewModel.removeHolding(it.symbol) }
                    scope.launch { snackbarHostState.showSnackbar("Holding removed") }
                    holdingToDelete = null
                }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { holdingToDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AddHoldingDialog(
    holding: PortfolioHolding?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, String) -> Unit
) {
    var symbol by remember { mutableStateOf(holding?.symbol ?: "") }
    var name by remember { mutableStateOf(holding?.name ?: "") }
    var shares by remember { mutableStateOf(holding?.shares?.toString() ?: "") }
    var avgCost by remember { mutableStateOf(holding?.avgCost?.toString() ?: "") }
    var notes by remember { mutableStateOf(holding?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (holding == null) "Add Holding" else "Edit Holding") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = symbol, onValueChange = { symbol = it.uppercase() }, label = { Text("Ticker") }, enabled = holding == null)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = shares, onValueChange = { shares = it }, label = { Text("Shares") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = avgCost, onValueChange = { avgCost = it }, label = { Text("Avg Cost") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val s = shares.toDoubleOrNull() ?: 0.0
                val c = avgCost.toDoubleOrNull() ?: 0.0
                if (symbol.isBlank()) error = "Ticker required"
                else if (s <= 0) error = "Invalid shares"
                else if (c <= 0) error = "Invalid cost"
                else onConfirm(symbol, name, s, c, notes)
            }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
