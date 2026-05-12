package com.example.barbershop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.barbershop.ui.theme.BarberShopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var shopData = JsonUtils().loadShopData(this, "simulation_input.json")

        setContent {
            BarberShopTheme {
                Scaffold(modifier = Modifier.fillMaxSize().systemBarsPadding()) { innerPadding ->
                    BarbershopDashboard(
                        modifier = Modifier.padding(innerPadding), shopData
                    )
                }
            }
        }
    }
}

@Composable
fun BarbershopDashboard(modifier: Modifier, shopData: Shop?) {
    val viewModel: BarberViewModel = viewModel(factory = BarberViewModelFactory(shopData!!))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        bottomBar = {
            // This keeps the controls "sticky" at the bottom
            Surface(tonalElevation = 8.dp, shadowElevation = 10.dp) {
                ControlPanel(viewModel, state)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues) // Respects the space taken by the bottomBar
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Stellar Salon", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.displaySmall)
            Text(
                state.currentTime,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF666666)
            )
        }

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp) // Adds spacing between sections
            ) {
                item {
                    Text("Stylist Stations", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if(state.activeBarbers.isEmpty()) {repeat(4){
                            BarberChair(null)
                        }

                        }
                        state.activeBarbers.forEach { barber ->
                            BarberChair(barber)
                        }
                    }
                }

                // Waiting Room Header
                item {
                    Text(
                        "Waiting Room (${state.waitingRoom.size}/4)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(state.waitingRoom) { customer ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "👤 ${customer.name}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

            }

        }
    }
}

@Composable
fun BarberChair(barber: Barber?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if(barber != null) {
            Icon(
                Icons.Default.AccountBox,
                contentDescription = "Barber",
                tint = if (barber.isFirstShift) Color.Red else Color.Green
            )
            barber?.name?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
        }

        // Show Customer if chair is occupied
        Box(if(barber != null) Modifier.size(80.dp).background(
            if(barber.currentCustomer != null) Color.Green else Color.Red,
            RoundedCornerShape(8.dp))
        else Modifier.size(80.dp).background(Color.Gray , RoundedCornerShape(8.dp)) ) {
            barber?.currentCustomer?.let {
                    Text(it.name, modifier = Modifier.align(Alignment.Center).padding(8.dp))

            }
        }
    }
}
@Composable
fun ControlPanel(viewModel: BarberViewModel, state: ShopState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row: Play/Pause/Restart ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Restart Button (Secondary)
                IconButton(
                    onClick = { viewModel.restartSimulation() },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                }

                // Main Play/Pause (Primary)

                FloatingActionButton(
                    onClick = { viewModel.togglePlayPause() },
                    containerColor = if (state.isPaused) Color(0xFF007AFF) else Color(0xFFFF3B30),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Close,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(26.dp)
                    )
                }


                // Speed indicator (Informational)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SPEED", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        "${viewModel.timeScale.toInt()}x",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }


            // --- Bottom Row: Speed Slider ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = viewModel.timeScale,
                    onValueChange = { viewModel.timeScale = it },
                    valueRange = 60f..10000f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFF007AFF),
                        inactiveTrackColor = Color.DarkGray
                    )
                )
            }
        }
    }
}