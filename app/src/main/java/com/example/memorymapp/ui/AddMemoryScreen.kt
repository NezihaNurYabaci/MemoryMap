package com.example.memorymapp.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.memorymapp.viewmodel.MemoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("MissingPermission")
@Composable
fun AddMemoryScreen(
    navController: NavController,
    lat: Double,
    lng: Double,
    viewModel: MemoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val primaryBlue = Color(0xFF003399)
    val lightBlueBg = Color(0xFFE6EBF5)
    val focusManager = LocalFocusManager.current
    val detectedDate = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    LaunchedEffect(lat, lng) {
        viewModel.lat = lat
        viewModel.lng = lng
        viewModel.fetchAddress(context, lat, lng)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    focusManager.clearFocus()
                    navController.popBackStack()
                }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryBlue)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Add New Memory", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = primaryBlue)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Location Information", fontWeight = FontWeight.Bold, color = primaryBlue)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("📍 ${viewModel.address}", color = Color.Black, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("📅 Date: $detectedDate", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            placeholder = { Text("What happened here?") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = primaryBlue,
                unfocusedContainerColor = lightBlueBg,
                focusedContainerColor = lightBlueBg,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                focusManager.clearFocus()
                if (viewModel.description.isNotBlank() && viewModel.lat != 0.0) {
                    viewModel.saveMemory(detectedDate) {
                        Toast.makeText(context, "Memory saved!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                } else {
                    Toast.makeText(context, "Please write a description", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp).padding(bottom = 16.dp),
            enabled = !viewModel.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Save Memory", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}