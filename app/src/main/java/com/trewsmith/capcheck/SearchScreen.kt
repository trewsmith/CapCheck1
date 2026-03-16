package com.trewsmith.capcheck

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SearchNavigation(
    stockViewModel: StockViewModel,
    watchlistViewModel: WatchlistViewModel,
    portfolioViewModel: PortfolioViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val navController = rememberNavController()
    val selectedStock by stockViewModel.selectedStock.collectAsState()
    val watchlist by watchlistViewModel.watchlist.collectAsState()
    val bookmarks by stockViewModel.bookmarks.collectAsState()
    val priceAlerts by stockViewModel.priceAlerts.collectAsState()

    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            SearchScreen(stockViewModel = stockViewModel, onStockSelected = {
                navController.navigate("detail")
            })
        }
        composable("detail") {
            selectedStock?.let { stock ->
                CompanyDetailScreen(
                    stock = stock,
                    onBack = { navController.popBackStack() },
                    onAddToWatchlist = { 
                        watchlistViewModel.addToWatchlist(stock.symbol ?: "", "")
                        scope.launch { snackbarHostState.showSnackbar("Added to watchlist") }
                    },
                    portfolioViewModel = portfolioViewModel,
                    isInWatchlist = watchlist.any { it.symbol == stock.symbol },
                    isBookmarked = bookmarks.contains(stock.symbol),
                    onToggleBookmark = { stockViewModel.toggleBookmark(stock.symbol ?: "") },
                    hasPriceAlert = priceAlerts.containsKey(stock.symbol),
                    onSetPriceAlert = {
                        if (priceAlerts.containsKey(stock.symbol)) {
                            stockViewModel.removePriceAlert(stock.symbol ?: "")
                        } else {
                            stockViewModel.setPriceAlert(stock.symbol ?: "", stock.close)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(stockViewModel: StockViewModel, onStockSelected: () -> Unit) {
    val searchResults by stockViewModel.searchResults.collectAsState()
    val selectedStock by stockViewModel.selectedStock.collectAsState()
    val isLoading by stockViewModel.isLoading.collectAsState()
    val error by stockViewModel.error.collectAsState()
    val searchHistory by stockViewModel.searchHistory.collectAsState()
    val marketFilter by stockViewModel.marketFilter.collectAsState()
    val bookmarks by stockViewModel.bookmarks.collectAsState()

    var query by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    val markets = listOf("stocks", "fx", "crypto")
    val shortcuts = listOf("AAPL", "TSLA", "MSFT", "GOOGL", "AMZN", "NVDA", "SPY")

    LaunchedEffect(query) {
        val sanitizedQuery = query.filter { it.isLetterOrDigit() }
        if (sanitizedQuery.length >= 2) {
            delay(400)
            stockViewModel.searchTickers(sanitizedQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isOpen = MarketStatus.isMarketOpen()
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isOpen) Color.Green else Color.Red, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isOpen) "Market Open" else "Market Closed",
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(shortcuts) { ticker ->
                FilterChip(
                    selected = query == ticker,
                    onClick = {
                        query = ticker
                        stockViewModel.fetchStockPrice(ticker)
                    },
                    label = { Text(ticker) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                markets.forEach { market ->
                    FilterChip(
                        selected = marketFilter == market,
                        onClick = { stockViewModel.setMarketFilter(market) },
                        label = { Text(market.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            IconButton(onClick = { isGridView = !isGridView }) {
                Icon(
                    imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                    contentDescription = "Toggle Layout"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search ticker or company") },
            placeholder = { Text("Enter a ticker or company name") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { 
                stockViewModel.searchTickers(query.filter { it.isLetterOrDigit() })
                focusManager.clearFocus()
            }),
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { 
                        query = ""
                        stockViewModel.clearSearch()
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            }
        )

        if (searchResults.isNotEmpty()) {
            Text(
                text = "${searchResults.size} results for $query",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            SearchSkeleton()
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { ticker ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    stockViewModel.fetchStockPrice(ticker.ticker)
                                    onStockSelected()
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(ticker.ticker, fontWeight = FontWeight.Bold)
                                    if (bookmarks.contains(ticker.ticker)) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star, 
                                            contentDescription = "Bookmarked", 
                                            modifier = Modifier.size(12.dp), 
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Text(ticker.name, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchResults) { ticker ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    stockViewModel.fetchStockPrice(ticker.ticker)
                                    onStockSelected()
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            ListItem(
                                headlineContent = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(ticker.ticker, fontWeight = FontWeight.Bold, fontSize = 18.sp) 
                                        if (bookmarks.contains(ticker.ticker)) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Star, 
                                                contentDescription = "Bookmarked", 
                                                modifier = Modifier.size(14.dp), 
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                supportingContent = { 
                                    Text(ticker.name, color = Color.Gray, fontSize = 14.sp) 
                                },
                                trailingContent = {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View Details")
                                }
                            )
                        }
                    }
                }
            }

            if (searchResults.isEmpty() && query.length >= 2) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No results found", fontWeight = FontWeight.Medium)
                }
            }
        }

        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
