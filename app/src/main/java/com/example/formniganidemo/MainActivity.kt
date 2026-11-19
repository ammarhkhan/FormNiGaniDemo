package com.example.formniganidemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.formniganidemo.attendee.AttendeeMainScreen
import com.example.formniganidemo.planner.PlannerMainScreen
import com.example.formniganidemo.serviceprovider.ServiceProviderMainScreen
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, "AIzaSyAWVGBPU8Z4oxxR16O-DuQe1rs2aVkerOg")
        }
        setContent {
            FormNiGaniTheme {
                RootNavigation()
            }
        }
    }
}

@Composable
fun FormNiGaniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF0D3B36),
            secondary = Color(0xFF14534D),
            background = Color(0xFFF8F9FA)
        ),
        content = content
    )
}

sealed class UserRole {
    object None : UserRole()
    object Attendee : UserRole()
    object Planner : UserRole()
    object ServiceProvider : UserRole()
}

@Composable
fun RootNavigation() {
    var loggedInRole by remember { mutableStateOf<UserRole>(UserRole.None) }

    when (loggedInRole) {
        is UserRole.None -> LoginScreen(
            onLoginSuccess = { role -> loggedInRole = role }
        )
        is UserRole.Attendee -> AttendeeMainScreen(
            onLogout = { loggedInRole = UserRole.None }
        )
        is UserRole.Planner -> PlannerMainScreen(
            onLogout = { loggedInRole = UserRole.None }
        )
        is UserRole.ServiceProvider -> ServiceProviderMainScreen(
            onLogout = { loggedInRole = UserRole.None }
        )
    }
}

@Composable
fun LoginScreen(onLoginSuccess: (UserRole) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D3B36))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Form Ni Gani",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF0D3B36)
                )
                Text(
                    text = "What's the plan?",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username (attendee1, planner1, sp1)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        when {
                            username == "a1" && password == "a1" -> onLoginSuccess(UserRole.Attendee)
                            username == "p1" && password == "p1" -> onLoginSuccess(UserRole.Planner)
                            username == "sp1" && password == "sp1" -> onLoginSuccess(UserRole.ServiceProvider)
                            else -> errorMessage = "Invalid username or password"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3B36)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Login", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Try: attendee1 / planner1 / sp1",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EmptyPlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$title Page (Coming Soon)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    }
}

data class VendorItem(val name: String, val category: String, val rating: String)