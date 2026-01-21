package com.example.memorymapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memorymapp.ui.theme.MemoryMappTheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.memorymapp.data.Memory
import com.example.memorymapp.ui.*
import com.example.memorymapp.viewmodel.MemoryViewModel
import com.google.firebase.auth.FirebaseAuth
import org.osmdroid.config.Configuration
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = "com.example.memorymapp"

        createNotificationChannel()
        askPermissions()

        enableEdgeToEdge()
        setContent {
            MemoryMappTheme {
                val navController = rememberNavController()
                val memoryViewModel: MemoryViewModel = viewModel()
                val memories by memoryViewModel.memories.collectAsState()

                LaunchedEffect(memories) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null && memories.isNotEmpty()) {
                        checkAnniversary(memories)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "welcome",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("welcome") { WelcomeScreen(navController) }
                        composable("login") { LoginScreen(navController, memoryViewModel) }
                        composable("signup") { SignUpScreen(navController) }
                        composable("map") { MapScreen(navController) }
                        composable("account") { AccountScreen(navController, memoryViewModel) }
                        composable("memories") { MemoriesScreen(navController) }

                        composable(
                            route = "add_memory?lat={lat}&lng={lng}",
                            arguments = listOf(
                                navArgument("lat") { type = NavType.StringType; defaultValue = "0.0" },
                                navArgument("lng") { type = NavType.StringType; defaultValue = "0.0" }
                            )
                        ) { backStackEntry ->
                            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
                            AddMemoryScreen(navController, lat, lng, memoryViewModel)
                        }

                        composable("detail/{memoryId}") { backStackEntry ->
                            val memoryId = backStackEntry.arguments?.getString("memoryId")
                            DetailScreen(navController, memoryId)
                        }
                    }
                }
            }
        }
    }

    private fun askPermissions() {
        val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (results[Manifest.permission.ACCESS_FINE_LOCATION] == false) {
                Toast.makeText(this, "Location permission is needed.", Toast.LENGTH_SHORT).show()
            }
        }
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        launcher.launch(permissions.toTypedArray())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("ANNIVERSARY_CHANNEL", "Memories", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun checkAnniversary(memories: List<Memory>) {
        val sharedPrefs = getSharedPreferences("MemoryPrefs", Context.MODE_PRIVATE)
        val lastNotifyDate = sharedPrefs.getString("last_anniversary_notify", "")
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayStr = sdf.format(Date())

        // Günde sadece bir kez bildirim
        if (lastNotifyDate == todayStr) return

        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)
        val targetDate = sdf.format(cal.time)

        val anniversaryMemory = memories.find { it.date == targetDate }
        if (anniversaryMemory != null) {
            sendNotification(anniversaryMemory)
            sharedPrefs.edit().putString("last_anniversary_notify", todayStr).apply()
        }
    }

    private fun sendNotification(memory: Memory) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "ANNIVERSARY_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Exactly one year ago today!")
            .setContentText("You were at ${memory.address}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}