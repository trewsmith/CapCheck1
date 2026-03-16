package com.trewsmith.capcheck

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TermsScreen(termsViewModel: TermsViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val favoritedTerms by termsViewModel.favoritedTerms.collectAsState()

    val categories = listOf("Valuation", "Income Statement", "Balance Sheet", "Market Data")
    val termsByCat = FinancialTerms.terms.values.groupBy { it.category }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search terms") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = showOnlyFavorites,
                onClick = { showOnlyFavorites = !showOnlyFavorites },
                label = { Text("Favorites") },
                leadingIcon = {
                    if (showOnlyFavorites) Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            var index = 0
                            for (cat in categories) {
                                if (cat == category) break
                                index += 1 + (termsByCat[cat]?.size ?: 0)
                            }
                            scope.launch { listState.animateScrollToItem(index) }
                        },
                        label = { Text(category) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
            categories.forEach { category ->
                val termsInCategory = FinancialTerms.terms.filter { it.value.category == category }
                val filteredTerms = termsInCategory.filter { (name, _) ->
                    val isMatch = name.contains(searchQuery, ignoreCase = true)
                    val isFav = favoritedTerms.contains(name)
                    isMatch && (!showOnlyFavorites || isFav)
                }

                if (filteredTerms.isNotEmpty()) {
                    stickyHeader {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(filteredTerms.keys.toList()) { name ->
                        val isFav = favoritedTerms.contains(name)
                        TappableTerm(term = name) {
                            ListItem(
                                headlineContent = { Text(name) },
                                trailingContent = {
                                    IconButton(onClick = { termsViewModel.toggleFavorite(name) }) {
                                        Icon(
                                            if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = if (isFav) "Remove from favorites" else "Add to favorites",
                                            tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
