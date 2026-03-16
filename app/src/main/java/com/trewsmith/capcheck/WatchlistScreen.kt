package com.trewsmith.capcheck

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WatchlistScreen(
    watchlistViewModel: WatchlistViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onNavigateToDetail: (String) -> Unit
) {
    val watchlist by watchlistViewModel.watchlist.collectAsState()
    val isLoading by watchlistViewModel.isLoading.collectAsState()
    val sortOrder by watchlistViewModel.sortOrder.collectAsState()
    val haptic = LocalHapticFeedback.current
    
    var symbolToDelete by remember { mutableStateOf<String?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedSymbols = remember { mutableStateListOf<String>() }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { watchlistViewModel.loadWatchlist() }
    )

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyColumnState(lazyListState) { from, to ->
        watchlistViewModel.reorderWatchlist(from.index, to.index)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Scaffold(
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text("${selectedSymbols.size} selected") },
                    actions = {
                        IconButton(onClick = {
                            selectedSymbols.forEach { watchlistViewModel.removeFromWatchlist(it) }
                            selectionMode = false
                            selectedSymbols.clear()
                            scope.launch { snackbarHostState.showSnackbar("Removed from watchlist") }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                        }
                        TextButton(onClick = { 
                            selectionMode = false
                            selectedSymbols.clear()
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .pullRefresh(pullRefreshState)) {
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = sortOrder == WatchlistSortOrder.A_Z,
                        onClick = { watchlistViewModel.setSortOrder(WatchlistSortOrder.A_Z) },
                        label = { Text("A-Z") }
                    )
                    FilterChip(
                        selected = sortOrder == WatchlistSortOrder.DATE_ADDED,
                        onClick = { watchlistViewModel.setSortOrder(WatchlistSortOrder.DATE_ADDED) },
                        label = { Text("Date Added") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (watchlist.isEmpty() && !isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nothing on your watchlist yet", fontWeight = FontWeight.Bold)
                        Text("Search for a stock and tap Watchlist to add it", fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(watchlist, key = { it.symbol }) { entry ->
                            ReorderableItem(reorderableState, key = entry.symbol) { isDragging ->
                                val elevation = if (isDragging) 8.dp else 0.dp
                                
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        if (it == SwipeToDismissBoxValue.EndToStart) {
                                            symbolToDelete = entry.symbol
                                            true
                                        } else false
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        val color = when (dismissState.dismissDirection) {
                                            SwipeToDismissBoxValue.EndToStart -> Color.Red
                                            else -> Color.Transparent
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(color)
                                                .padding(horizontal = 24.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.White
                                            )
                                        }
                                    },
                                    enableDismissFromStartToEnd = false
                                ) {
                                    Card(
                                        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .combinedClickable(
                                                onClick = {
                                                    if (selectionMode) {
                                                        if (selectedSymbols.contains(entry.symbol)) {
                                                            selectedSymbols.remove(entry.symbol)
                                                            if (selectedSymbols.isEmpty()) selectionMode = false
                                                        } else {
                                                            selectedSymbols.add(entry.symbol)
                                                        }
                                                    } else {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        onNavigateToDetail(entry.symbol)
                                                    }
                                                },
                                                onLongClick = {
                                                    if (!selectionMode) {
                                                        selectionMode = true
                                                        selectedSymbols.add(entry.symbol)
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    }
                                                }
                                            )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (selectionMode) {
                                                Checkbox(
                                                    checked = selectedSymbols.contains(entry.symbol),
                                                    onCheckedChange = null,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                            }
                                            ListItem(
                                                headlineContent = { Text(entry.symbol, fontWeight = FontWeight.Bold) },
                                                supportingContent = {
                                                    val priceText = entry.currentPrice?.let {
                                                        FormatUtils.formatPrice(it)
                                                    } ?: "-"
                                                    Text("Current Price: $priceText")
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (!selectionMode) {
                                                IconButton(
                                                    modifier = Modifier.draggableHandle(),
                                                    onClick = {}
                                                ) {
                                                    Icon(Icons.Default.Menu, contentDescription = "Reorder")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            PullRefreshIndicator(isLoading, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }

    if (symbolToDelete != null) {
        AlertDialog(
            onDismissRequest = { symbolToDelete = null },
            title = { Text("Remove from Watchlist") },
            text = { Text("Remove $symbolToDelete from watchlist?") },
            confirmButton = {
                Button(onClick = {
                    symbolToDelete?.let { 
                        watchlistViewModel.removeFromWatchlist(it)
                        scope.launch { snackbarHostState.showSnackbar("Removed from watchlist") }
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    symbolToDelete = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { symbolToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
