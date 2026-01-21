package com.example.memorymapp.ui

import com.example.memorymapp.data.Memory
import com.example.memorymapp.viewmodel.MemoryViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MemoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val memoriesList by viewModel.memories.collectAsState()
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(12.0)
                    controller.setCenter(GeoPoint(53.4285, 14.5528))

                    val receiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            p?.let {
                                navController.navigate("add_memory?lat=${it.latitude}&lng=${it.longitude}")
                            }
                            return true
                        }
                    }
                    overlays.add(MapEventsOverlay(receiver))
                    mapViewInstance = this
                }
            },
            update = { mapView ->
                mapView.overlays.removeAll { it is Marker }
                memoriesList.forEach { memory ->
                    if (memory.lat != 0.0 && memory.lng != 0.0) {
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(memory.lat, memory.lng)
                        marker.title = memory.description
                        marker.subDescription = memory.date
                        marker.icon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.setOnMarkerClickListener { _, _ ->
                            navController.navigate("detail/${memory.id}")
                            true
                        }
                        mapView.overlays.add(marker)
                    }
                }
                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .zIndex(2f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { mapViewInstance?.controller?.zoomIn() },
                containerColor = Color.White,
                contentColor = Color(0xFF003399),
                shape = CircleShape,
                modifier = Modifier.size(45.dp)
            ) { Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold) }

            FloatingActionButton(
                onClick = { mapViewInstance?.controller?.zoomOut() },
                containerColor = Color.White,
                contentColor = Color(0xFF003399),
                shape = CircleShape,
                modifier = Modifier.size(45.dp)
            ) { Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigate("account") }) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF003399), modifier = Modifier.size(32.dp))
            }

            Text(
                "Memory Map",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF003399)
            )

            IconButton(onClick = { navController.navigate("add_memory") }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF003399), modifier = Modifier.size(32.dp))
            }
        }

        Button(
            onClick = { navController.navigate("memories") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
                .padding(bottom = 30.dp)
                .height(50.dp)
                .fillMaxWidth(0.7f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003399)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("View All Memories", fontWeight = FontWeight.Bold)
        }
    }
}