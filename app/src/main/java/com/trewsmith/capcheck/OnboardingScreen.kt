package com.trewsmith.capcheck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = listOf(
        OnboardingPageData(
            "CapCheck",
            "Learn and Track",
            "Your all-in-one companion for tracking stocks and learning financial markets in plain English."
        ),
        OnboardingPageData(
            "Smart Search",
            "Insights at your fingertips",
            "Search for any ticker. Tap on terms you don't understand to see instant definitions from our financial dictionary."
        ),
        OnboardingPageData(
            "Track Your Wealth",
            "Portfolio & Watchlist",
            "Manage your portfolio and watchlist with ease. See real-time performance and asset distribution at a glance."
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { index ->
            OnboardingPage(pages[index])
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { index ->
                val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onFinished) {
                Text("Skip")
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinished()
                    }
                }
            ) {
                Text(if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next")
            }
        }
    }
}

data class OnboardingPageData(val title: String, val tagline: String, val description: String)

@Composable
fun OnboardingPage(data: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = data.title,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = data.tagline,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
