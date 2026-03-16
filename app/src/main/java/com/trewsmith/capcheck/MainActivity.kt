package com.trewsmith.capcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trewsmith.capcheck.ui.theme.CapCheckTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val isDarkMode by preferencesManager.isDarkModeFlow.collectAsState(initial = false)

            CapCheckTheme(darkTheme = isDarkMode) {
                var appReady by remember { mutableStateOf(false) }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                
                LaunchedEffect(Unit) {
                    ErrorHandler.errors.collectLatest { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                LaunchedEffect(Unit) {
                    delay(1000)
                    appReady = true
                }

                if (!appReady) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CapCheck", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    val authViewModel: AuthViewModel = viewModel()
                    val user by authViewModel.user.collectAsState()
                    var onboardingCompleted by remember { mutableStateOf(false) }

                    if (user == null) {
                        AuthScreen(authViewModel = authViewModel)
                    } else if (!onboardingCompleted) {
                        OnboardingScreen(onFinished = { onboardingCompleted = true })
                    } else {
                        val stockViewModel: StockViewModel = viewModel(factory = StockViewModelFactory(preferencesManager))
                        val portfolioViewModel: PortfolioViewModel = viewModel(factory = PortfolioViewModelFactory(preferencesManager))
                        val watchlistViewModel: WatchlistViewModel = viewModel()
                        val termsViewModel: TermsViewModel = viewModel()
                        
                        val holdings by portfolioViewModel.holdings.collectAsState()
                        val watchlist by watchlistViewModel.watchlist.collectAsState()
                        val favoritedTerms by termsViewModel.favoritedTerms.collectAsState()
                        
                        var selectedTab by remember { mutableIntStateOf(0) }
                        
                        val screenTitle = when(selectedTab) {
                            0 -> "Search"
                            1 -> "Portfolio"
                            2 -> "Watchlist"
                            3 -> "Learn"
                            4 -> "Settings"
                            else -> "CapCheck"
                        }

                        Scaffold(
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            topBar = {
                                CapCheckTopBar(
                                    title = screenTitle,
                                    showBackButton = false,
                                    onBackClick = null,
                                    actions = {}
                                )
                            },
                            bottomBar = {
                                NavigationBar(modifier = Modifier.animateContentSize()) {
                                    NavigationBarItem(
                                        selected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        icon = { Icon(Icons.Default.Search, contentDescription = "Search stocks") },
                                        label = {
                                            androidx.compose.animation.AnimatedVisibility(visible = selectedTab == 0) {
                                                Text("Search")
                                            }
                                        },
                                        alwaysShowLabel = false
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (holdings.isNotEmpty()) {
                                                        Badge { Text(holdings.size.toString()) }
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "My Portfolio")
                                            }
                                        },
                                        label = {
                                            androidx.compose.animation.AnimatedVisibility(visible = selectedTab == 1) {
                                                Text("Portfolio")
                                            }
                                        },
                                        alwaysShowLabel = false
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 2,
                                        onClick = { selectedTab = 2 },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (watchlist.isNotEmpty()) {
                                                        Badge { Text(watchlist.size.toString()) }
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Star, contentDescription = "Watchlist")
                                            }
                                        },
                                        label = {
                                            androidx.compose.animation.AnimatedVisibility(visible = selectedTab == 2) {
                                                Text("Watchlist")
                                            }
                                        },
                                        alwaysShowLabel = false
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 3,
                                        onClick = { selectedTab = 3 },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (favoritedTerms.isNotEmpty()) {
                                                        Badge { Text(favoritedTerms.size.toString()) }
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Book, contentDescription = "Learn financial terms")
                                            }
                                        },
                                        label = {
                                            androidx.compose.animation.AnimatedVisibility(visible = selectedTab == 3) {
                                                Text("Learn")
                                            }
                                        },
                                        alwaysShowLabel = false
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 4,
                                        onClick = { selectedTab = 4 },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "App settings") },
                                        label = {
                                            androidx.compose.animation.AnimatedVisibility(visible = selectedTab == 4) {
                                                Text("Settings")
                                            }
                                        },
                                        alwaysShowLabel = false
                                    )
                                }
                            }
                        ) { padding ->
                            Box(modifier = Modifier.padding(padding)) {
                                AnimatedContent(
                                    targetState = selectedTab,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                                    },
                                    label = "TabTransition"
                                ) { targetTab ->
                                    when (targetTab) {
                                        0 -> SearchNavigation(
                                            stockViewModel = stockViewModel,
                                            watchlistViewModel = watchlistViewModel,
                                            portfolioViewModel = portfolioViewModel,
                                            snackbarHostState = snackbarHostState,
                                            scope = scope
                                        )
                                        1 -> {
                                            val portfolioNavController = rememberNavController()
                                            NavHost(navController = portfolioNavController, startDestination = "list") {
                                                composable("list") {
                                                    PortfolioScreen(
                                                        portfolioViewModel = portfolioViewModel,
                                                        onHoldingClick = { holding ->
                                                            portfolioNavController.navigate("detail/${holding.symbol}")
                                                        },
                                                        snackbarHostState = snackbarHostState,
                                                        scope = scope
                                                    )
                                                }
                                                composable("detail/{symbol}") { backStackEntry ->
                                                    val symbol = backStackEntry.arguments?.getString("symbol")
                                                    val holding = holdings.find { it.symbol == symbol }
                                                    holding?.let {
                                                        HoldingDetailScreen(
                                                            holding = it, 
                                                            onBack = { portfolioNavController.popBackStack() }, 
                                                            portfolioViewModel = portfolioViewModel
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        2 -> WatchlistScreen(
                                            watchlistViewModel = watchlistViewModel,
                                            snackbarHostState = snackbarHostState,
                                            scope = scope,
                                            onNavigateToDetail = { symbol ->
                                                selectedTab = 0
                                                stockViewModel.fetchStockPrice(symbol)
                                            }
                                        )
                                        3 -> TermsScreen(termsViewModel = termsViewModel)
                                        4 -> SettingsScreen(
                                            isDarkMode = isDarkMode,
                                            onToggleDarkMode = { scope.launch { preferencesManager.setDarkMode(!isDarkMode) } },
                                            onClearHistory = { stockViewModel.clearHistory() },
                                            onSignOut = { authViewModel.signOut() }
                                        )
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
