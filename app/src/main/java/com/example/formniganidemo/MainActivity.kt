package com.example.formniganidemo

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.formniganidemo.attendee.AttendeeMainScreen
import com.example.formniganidemo.planner.PlannerMainScreen
import com.example.formniganidemo.serviceprovider.ServiceProviderMainScreen
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                applicationContext,
                "AIzaSyAWVGBPU8Z4oxxR16O-DuQe1rs2aVkerOg"
            )
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

enum class ProfileType(val key: String, val label: String, val subtitle: String) {
    ATTENDEE("attendee", "Attendee", "Discover & attend events"),
    PLANNER("planner", "Event Planner", "Create & manage events"),
    SERVICE_PROVIDER("service_provider", "Service Provider", "Offer venue, media & vendor services");

    companion object {
        fun fromKey(key: String): ProfileType? =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
    }
}

sealed class ActiveDestination {
    data object Auth : ActiveDestination()
    data class RoleLanding(val roles: List<ProfileType>) : ActiveDestination()
    data class Attendee(val canSwitchRole: Boolean) : ActiveDestination()
    data class Planner(val canSwitchRole: Boolean) : ActiveDestination()
    data class ServiceProvider(val canSwitchRole: Boolean) : ActiveDestination()
}

private fun routeForRoles(roles: List<ProfileType>): ActiveDestination {
    val distinctRoles = roles.distinct()
    return when {
        distinctRoles.size > 1 -> ActiveDestination.RoleLanding(distinctRoles)
        distinctRoles.firstOrNull() == ProfileType.PLANNER -> ActiveDestination.Planner(canSwitchRole = false)
        distinctRoles.firstOrNull() == ProfileType.SERVICE_PROVIDER -> ActiveDestination.ServiceProvider(canSwitchRole = false)
        else -> ActiveDestination.Attendee(canSwitchRole = false)
    }
}

@Composable
fun RootNavigation() {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var destination by remember { mutableStateOf<ActiveDestination>(ActiveDestination.Auth) }
    var userRoles by remember { mutableStateOf<List<ProfileType>>(emptyList()) }
    var isFetchingProfile by remember { mutableStateOf(false) }

    fun loadUserRoles(uid: String, onLoaded: (List<ProfileType>) -> Unit) {
        isFetchingProfile = true
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                isFetchingProfile = false
                @Suppress("UNCHECKED_CAST")
                val rawRoles = (doc.get("roles") as? List<*>)?.mapNotNull { it as? String }.orEmpty()
                val parsed = rawRoles.mapNotNull { ProfileType.fromKey(it) }.distinct()
                onLoaded(parsed.ifEmpty { listOf(ProfileType.ATTENDEE) })
            }
            .addOnFailureListener {
                isFetchingProfile = false
                onLoaded(listOf(ProfileType.ATTENDEE))
            }
    }

    fun handleLogout() {
        auth.signOut()
        userRoles = emptyList()
        destination = ActiveDestination.Auth
    }

    if (isFetchingProfile) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D3B36)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    when (val current = destination) {
        is ActiveDestination.Auth -> LoginScreen(
            onAuthenticated = { uid, selectedSignupRoles, isNewAccount ->
                if (isNewAccount) {
                    val chosen = selectedSignupRoles.distinct().ifEmpty { listOf(ProfileType.ATTENDEE) }
                    userRoles = chosen
                    destination = routeForRoles(chosen)
                } else {
                    loadUserRoles(uid) { loadedRoles ->
                        userRoles = loadedRoles
                        destination = routeForRoles(loadedRoles)
                    }
                }
            }
        )

        is ActiveDestination.RoleLanding -> RoleSelectionLandingScreen(
            roles = current.roles,
            onSelectRole = { picked ->
                destination = when (picked) {
                    ProfileType.ATTENDEE -> ActiveDestination.Attendee(canSwitchRole = true)
                    ProfileType.PLANNER -> ActiveDestination.Planner(canSwitchRole = true)
                    ProfileType.SERVICE_PROVIDER -> ActiveDestination.ServiceProvider(canSwitchRole = true)
                }
            },
            onLogout = ::handleLogout
        )

        is ActiveDestination.Attendee -> AttendeeMainScreen(
            onLogout = {
                if (current.canSwitchRole && userRoles.size > 1) {
                    destination = ActiveDestination.RoleLanding(userRoles)
                } else {
                    handleLogout()
                }
            }
        )

        is ActiveDestination.Planner -> PlannerMainScreen(
            onLogout = {
                if (current.canSwitchRole && userRoles.size > 1) {
                    destination = ActiveDestination.RoleLanding(userRoles)
                } else {
                    handleLogout()
                }
            }
        )

        is ActiveDestination.ServiceProvider -> ServiceProviderMainScreen(
            onLogout = {
                if (current.canSwitchRole && userRoles.size > 1) {
                    destination = ActiveDestination.RoleLanding(userRoles)
                } else {
                    handleLogout()
                }
            }
        )
    }
}

@Composable
fun RoleSelectionLandingScreen(
    roles: List<ProfileType>,
    onSelectRole: (ProfileType) -> Unit,
    onLogout: () -> Unit
) {
    val deepTeal = Color(0xFF0D3B36)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Form Ni Gani",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = deepTeal
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Choose how you want to continue",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your account has multiple profile types enabled.",
                fontSize = 13.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                roles.forEach { role ->
                    val icon: ImageVector = when (role) {
                        ProfileType.ATTENDEE -> Icons.Default.Explore
                        ProfileType.PLANNER -> Icons.Default.EventAvailable
                        ProfileType.SERVICE_PROVIDER -> Icons.Default.Storefront
                    }

                    Card(
                        onClick = { onSelectRole(role) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(deepTeal.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = role.label,
                                    tint = deepTeal
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = role.label,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = role.subtitle,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Sign Out", color = deepTeal, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LoginScreen(
    onAuthenticated: (uid: String, selectedSignupRoles: List<ProfileType>, isNewAccount: Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val credentialManager = remember { CredentialManager.create(context) }

    val deepTeal = Color(0xFF0D3B36)

    // false = Sign In, true = Create Account
    var isSignUpMode by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRoles by remember { mutableStateOf(setOf(ProfileType.ATTENDEE)) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    fun toggleRole(role: ProfileType) {
        selectedRoles = if (role in selectedRoles) {
            if (selectedRoles.size > 1) selectedRoles - role else selectedRoles
        } else {
            selectedRoles + role
        }
    }

    fun persistUserDoc(uid: String, emailAddr: String, handle: String, roles: List<ProfileType>, mergeIfExists: Boolean, onDone: () -> Unit) {
        val data = hashMapOf<String, Any>(
            "uid" to uid,
            "email" to emailAddr,
            "username" to handle.ifBlank { emailAddr.substringBefore("@") },
            "roles" to roles.map { it.key },
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        val docRef = db.collection("users").document(uid)
        if (mergeIfExists) {
            docRef.set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener { onDone() }
                .addOnFailureListener { e ->
                    isLoading = false
                    errorMessage = e.localizedMessage ?: "Failed to save profile"
                }
        } else {
            docRef.set(data)
                .addOnSuccessListener { onDone() }
                .addOnFailureListener { e ->
                    isLoading = false
                    errorMessage = e.localizedMessage ?: "Failed to save profile"
                }
        }
    }

    fun signInWithFirebaseCredential(credential: AuthCredential, googleEmail: String?, googleName: String?) {
        isLoading = true
        errorMessage = ""
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    isLoading = false
                    errorMessage = "Google sign-in failed."
                    return@addOnSuccessListener
                }
                val docRef = db.collection("users").document(user.uid)
                docRef.get().addOnSuccessListener { snap ->
                    if (!snap.exists()) {
                        // First-time Google user: save chosen roles (defaults to Attendee if none toggled)
                        val rolesToSave = selectedRoles.toList().ifEmpty { listOf(ProfileType.ATTENDEE) }
                        persistUserDoc(
                            uid = user.uid,
                            emailAddr = user.email ?: googleEmail.orEmpty(),
                            handle = user.displayName ?: googleName.orEmpty(),
                            roles = rolesToSave,
                            mergeIfExists = true
                        ) {
                            isLoading = false
                            onAuthenticated(user.uid, rolesToSave, true)
                        }
                    } else {
                        isLoading = false
                        onAuthenticated(user.uid, emptyList(), false)
                    }
                }.addOnFailureListener { e ->
                    isLoading = false
                    errorMessage = e.localizedMessage ?: "Failed to check user profile."
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.localizedMessage ?: "Google sign-in failed."
            }
    }

    fun launchGoogleSignIn() {
        val webClientIdResId = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )
        if (webClientIdResId == 0) {
            errorMessage = "Missing default_web_client_id in strings.xml (check google-services.json)."
            return
        }
        val webClientId = context.getString(webClientIdResId)

        coroutineScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val response = credentialManager.getCredential(
                    context = context as Activity,
                    request = request
                )
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(response.credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                signInWithFirebaseCredential(
                    credential = firebaseCredential,
                    googleEmail = googleIdTokenCredential.id,
                    googleName = googleIdTokenCredential.displayName
                )
            } catch (e: GetCredentialException) {
                isLoading = false
                errorMessage = e.localizedMessage ?: "Google sign-in cancelled."
            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.localizedMessage ?: "Google sign-in error."
            }
        }
    }

    fun handleEmailAuth() {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()
        val cleanUsername = username.trim()

        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
            errorMessage = "Please enter both email and password."
            return
        }

        isLoading = true
        errorMessage = ""

        if (isSignUpMode) {
            val rolesToSave = selectedRoles.toList().ifEmpty { listOf(ProfileType.ATTENDEE) }
            auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid == null) {
                        isLoading = false
                        errorMessage = "Account creation failed."
                        return@addOnSuccessListener
                    }
                    persistUserDoc(
                        uid = uid,
                        emailAddr = cleanEmail,
                        handle = cleanUsername.ifBlank { cleanEmail.substringBefore("@") },
                        roles = rolesToSave,
                        mergeIfExists = false
                    ) {
                        isLoading = false
                        onAuthenticated(uid, rolesToSave, true)
                    }
                }
                .addOnFailureListener { e ->
                    isLoading = false
                    errorMessage = e.localizedMessage ?: "Could not create account."
                }
        } else {
            auth.signInWithEmailAndPassword(cleanEmail, cleanPassword)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid == null) {
                        isLoading = false
                        errorMessage = "Sign in failed."
                        return@addOnSuccessListener
                    }
                    isLoading = false
                    onAuthenticated(uid, emptyList(), false)
                }
                .addOnFailureListener { e ->
                    isLoading = false
                    errorMessage = e.localizedMessage ?: "Invalid email or password."
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(deepTeal)
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
                    color = deepTeal
                )
                Text(
                    text = if (isSignUpMode) "Create your account" else "What's the plan?",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isSignUpMode) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                if (isSignUpMode) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Select profile type(s) (pick one or more):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ProfileType.entries.forEach { role ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = role in selectedRoles,
                                onCheckedChange = { toggleRole(role) },
                                colors = CheckboxDefaults.colors(checkedColor = deepTeal)
                            )
                            Text(
                                text = role.label,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = ::handleEmailAuth,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = deepTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Text(
                            text = if (isSignUpMode) "Create Account" else "Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = ::launchGoogleSignIn,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isSignUpMode) "Continue with Google" else "Sign in with Google",
                        fontWeight = FontWeight.SemiBold,
                        color = deepTeal
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        isSignUpMode = !isSignUpMode
                        errorMessage = ""
                    }
                ) {
                    Text(
                        text = if (isSignUpMode) {
                            "Already have an account? Sign in"
                        } else {
                            "Don't have an account? Create one"
                        },
                        fontSize = 12.sp,
                        color = deepTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
        Text(
            text = "$title Page (Coming Soon)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
    }
}

data class VendorItem(val name: String, val category: String, val rating: String)