package com.trewsmith.capcheck

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TermDetail(val definition: String, val example: String, val category: String)

object FinancialTerms {
    val terms = mapOf(
        "P/E Ratio" to TermDetail(
            "The Price-to-Earnings ratio relates a company's share price to its earnings per share.",
            "If a stock is trading at $100 and EPS is $5, the P/E ratio is 20.",
            "Valuation"
        ),
        "Market Cap" to TermDetail(
            "The total value of all a company's shares of stock.",
            "A company with 1 million shares at $50 each has a market cap of $50 million.",
            "Valuation"
        ),
        "Price to Sales" to TermDetail(
            "A valuation ratio that compares a company’s stock price to its revenues.",
            "A P/S ratio of 2 means investors are paying $2 for every $1 of sales.",
            "Valuation"
        ),
        "Beta" to TermDetail(
            "A measure of a stock's volatility in relation to the overall market.",
            "A beta of 1.2 means the stock is 20% more volatile than the market.",
            "Valuation"
        ),
        "Revenue" to TermDetail(
            "The total amount of money a company receives from its business activities.",
            "Apple's revenue grew as iPhone sales increased globally.",
            "Income Statement"
        ),
        "Net Income" to TermDetail(
            "The amount of profit remaining after all expenses and taxes have been paid.",
            "A company with $1M revenue and $800k expenses has $200k in net income.",
            "Income Statement"
        ),
        "Gross Margin" to TermDetail(
            "The percentage of revenue that exceeds the cost of goods sold.",
            "High gross margins often indicate a strong brand or unique product.",
            "Income Statement"
        ),
        "Operating Income" to TermDetail(
            "Profit realized from a business's operations after deducting operating expenses.",
            "Operating income shows how well a company manages its core business costs.",
            "Income Statement"
        ),
        "EPS" to TermDetail(
            "Earnings Per Share is the portion of a company's profit allocated to each outstanding share of common stock.",
            "If a company earns $10 million and has 2 million shares, the EPS is $5.",
            "Income Statement"
        ),
        "EBITDA" to TermDetail(
            "Earnings Before Interest, Taxes, Depreciation, and Amortization.",
            "Investors use EBITDA to compare profitability across industries.",
            "Income Statement"
        ),
        "Book Value" to TermDetail(
            "The net value of a firm's assets found on its balance sheet.",
            "If book value is higher than market price, the stock might be undervalued.",
            "Balance Sheet"
        ),
        "Debt to Equity" to TermDetail(
            "A ratio used to evaluate a company's financial leverage.",
            "A ratio of 1.5 means the company has $1.50 in debt for every $1 in equity.",
            "Balance Sheet"
        ),
        "Current Ratio" to TermDetail(
            "A liquidity ratio that measures a company's ability to pay short-term obligations.",
            "A current ratio of 2 means the company has twice as many assets as liabilities.",
            "Balance Sheet"
        ),
        "Return on Equity" to TermDetail(
            "A measure of financial performance calculated by dividing net income by shareholders' equity.",
            "An ROE of 15% means the company generates $0.15 of profit for every $1 of equity.",
            "Balance Sheet"
        ),
        "Volume" to TermDetail(
            "The number of shares of a security traded during a specific period.",
            "High volume during a price surge often suggests strong investor interest.",
            "Market Data"
        ),
        "Close" to TermDetail(
            "The final price at which a stock is traded on a given trading day.",
            "The stock closed at $150.25 at the end of Tuesday's trading session.",
            "Market Data"
        ),
        "Open" to TermDetail(
            "The price at which a stock first trades upon the opening of an exchange.",
            "The market opened at 9:30 AM with the stock priced at $148.00.",
            "Market Data"
        ),
        "High" to TermDetail(
            "The highest price at which a stock traded during the day.",
            "Despite falling later, the stock hit a high of $155.00 today.",
            "Market Data"
        ),
        "Low" to TermDetail(
            "The lowest price at which a stock traded during the day.",
            "The stock found support at its daily low of $145.50.",
            "Market Data"
        ),
        "52-Week High" to TermDetail(
            "The highest price a stock has traded at during the last year.",
            "Investors look at the 52-week high to judge current momentum.",
            "Market Data"
        ),
        "52-Week Low" to TermDetail(
            "The lowest price a stock has traded at during the last year.",
            "A stock trading near its 52-week low may be seen as a bargain.",
            "Market Data"
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TappableTerm(term: String, content: @Composable () -> Unit) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Surface(
        modifier = Modifier.clickable { showSheet = true },
        color = Color.Transparent
    ) {
        content()
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            val detail = FinancialTerms.terms[term]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(text = term, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = detail?.definition ?: "No explanation available.", style = MaterialTheme.typography.bodyMedium)
                
                detail?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Example:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = it.example,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showSheet = false },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
