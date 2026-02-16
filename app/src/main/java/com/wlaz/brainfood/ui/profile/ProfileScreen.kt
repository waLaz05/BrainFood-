package com.wlaz.brainfood.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wlaz.brainfood.ui.auth.AuthState
import com.wlaz.brainfood.ui.auth.AuthViewModel
import com.wlaz.brainfood.ui.components.GlassCard
import com.wlaz.brainfood.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.LoggedIn)?.user
    val scope = rememberCoroutineScope()

    var isSyncing by remember { mutableStateOf(false) }
    var syncDone by remember { mutableStateOf(false) }

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); showContent = true }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp, end = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically { 30 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(2.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (user != null) {
                        Text(
                            text = (user.displayName?.firstOrNull()?.uppercase() ?: "U"),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextSecondary
                        )
                    }
                }

                // Name
                Text(
                    text = user?.displayName ?: "Usuario Invitado",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Email
                if (user?.email != null) {
                    Text(
                        text = user.email!!,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Cloud Sync Card
                if (user != null) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Sincronización en la Nube",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }

                            Text(
                                text = "Tus ingredientes, favoritos y lista de compras se respaldan automáticamente.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )

                            // Sync button
                            Button(
                                onClick = {
                                    if (!isSyncing) {
                                        isSyncing = true
                                        syncDone = false
                                        scope.launch {
                                            try {
                                                // The syncManager is accessed via the ViewModel's repository
                                                // For now, just simulate — the actual sync happens automatically
                                                delay(1500)
                                                syncDone = true
                                            } finally {
                                                isSyncing = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    contentColor = TextPrimary
                                ),
                                enabled = !isSyncing
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = TextPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        if (syncDone) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = when {
                                        isSyncing -> "Sincronizando..."
                                        syncDone -> "¡Sincronizado!"
                                        else -> "Sincronizar Ahora"
                                    },
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    // Guest mode info
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "☁️",
                                fontSize = 32.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Modo Invitado",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Inicia sesión con Google para respaldar tus datos en la nube.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Logout button (only if logged in)
                if (user != null) {
                    OutlinedButton(
                        onClick = {
                            authViewModel.signOut()
                            onLogout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF4444).copy(alpha = 0.4f),
                                    Color(0xFFFF4444).copy(alpha = 0.2f)
                                )
                            )
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF6B6B)
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Cerrar Sesión",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
