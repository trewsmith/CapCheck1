package com.trewsmith.capcheck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trewsmith.capcheck.data.model.PortfolioHolding
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingDetailScreen(
    holding: PortfolioHolding,
    onBack: () -> Unit,
    portfolioViewModel: PortfolioViewModel
) {
    var isEditing by remember { mutableStateOf(false) }
    var shares by remember { mutableStateOf(holding.shares.toString()) }
    var avgCost by remember { mutableStateOf(holding.avgCost.toString()) }
    var notes by remember { mutableStateOf(holding.notes) }

    val totalValue = holding.shares * holding.currentPrice
    val totalCost = holding.shares * holding.avgCost
    val gainLoss = totalValue - totalCost
    val gainLossPercent = if (totalCost != 0.0) (gainLoss / totalCost) * 100 else 0.0
    val color = when {
        gainLossPercent > 1.0 -> Color(0xFF4CAF50)
        gainLossPercent < -1.0 -> Color.Red
        else -> Color.Gray
    }

    Scaffold(
        topBar = {
            CapCheckTopBar(
                title = holding.symbol,
                showBackButton = true,
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(if (isEditing) Icons.Default.Save else Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = holding.name, style = MaterialTheme.typography.headlineMedium)
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isEditing) {
                        OutlinedTextField(value = shares, onValueChange = { shares = it }, label = { Text("Shares") })
                        OutlinedTextField(value = avgCost, onValueChange = { avgCost = it }, label = { Text("Avg Cost") })
                    } else {
                        DetailRow("Shares", FormatUtils.formatShares(holding.shares))
                        DetailRow("Average Cost", FormatUtils.formatPrice(holding.avgCost))
                        DetailRow("Current Price", FormatUtils.formatPrice(holding.currentPrice))
                    }
                    HorizontalDivider()
                    DetailRow("Total Value", FormatUtils.formatPrice(totalValue))
                    DetailRow("Total Gain/Loss", FormatUtils.formatPrice(gainLoss), valueColor = color)
                    DetailRow("Gain/Loss %", FormatUtils.formatPercent(gainLossPercent), valueColor = color)
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cost Basis", fontWeight = FontWeight.Bold)
                    DetailRow("Total Cost", FormatUtils.formatPrice(totalCost))
                    DetailRow("Return Multiple", String.format(Locale.US, "%.2fx", holding.currentPrice / holding.avgCost))
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Performance", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.LightGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        Text("Historical performance chart coming soon", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Column {
                Text(text = "Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (isEditing) {
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        val s = shares.toDoubleOrNull() ?: holding.shares
                        val a = avgCost.toDoubleOrNull() ?: holding.avgCost
                        portfolioViewModel.editHolding(holding.copy(shares = s, avgCost = a, notes = notes))
                        isEditing = false
                    }, modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) {
                        Text("Save Changes")
                    }
                } else {
                    Text(text = if (holding.notes.isEmpty()) "No notes added" else holding.notes, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                }
            }

            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
            Text(text = "Last modified: ${sdf.format(Date(holding.lastModified))}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
