package com.example.memorymapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.navigation.NavController
import com.example.memorymapp.viewmodel.MemoryViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun AccountScreen(
    navController: NavController,
    memoryViewModel: MemoryViewModel
) {
    val primaryBlue = Color(0xFF003399)
    val lightBlueBg = Color(0xFFE6EBF5)
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var userName by remember { mutableStateOf(user?.displayName ?: "User") }
    val userEmail = user?.email ?: "Not signed in"

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
            dbRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val nameFromDb = snapshot.child("name").value.toString()
                    if (nameFromDb != "null" && nameFromDb.isNotEmpty()) {
                        userName = nameFromDb
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp), // Dikey padding azaltıldı
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = primaryBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp)) // Boşluk daraltıldı

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = primaryBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Your Account", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = {},
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = primaryBlue,
                unfocusedContainerColor = lightBlueBg,
                focusedContainerColor = lightBlueBg,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userEmail,
            onValueChange = {},
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = primaryBlue,
                unfocusedContainerColor = lightBlueBg,
                focusedContainerColor = lightBlueBg,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "● Sync: Connected to Firebase Cloud",
            fontSize = 12.sp,
            color = Color(0xFF2E7D32),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).padding(start = 4.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = primaryBlue, modifier = Modifier.size(110.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Keep your memories alive!", color = Color.DarkGray, fontWeight = FontWeight.Medium, fontSize = 16.sp)
        }

        Button(
            onClick = {
                memoryViewModel.clearData()
                auth.signOut()
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInClient.signOut().addOnCompleteListener {
                    navController.navigate("welcome") {
                        popUpTo("map") { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Log Out", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}