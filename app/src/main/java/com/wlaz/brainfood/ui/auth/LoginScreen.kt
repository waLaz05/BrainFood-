package com.wlaz.brainfood.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wlaz.brainfood.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onContinueAsGuest: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // Navigate on success
    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedIn) {
            delay(400) // Brief pause for animation
            onLoginSuccess()
        }
    }

    // Staggered entrance animations
    var showLogo by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200); showLogo = true
        delay(300); showTitle = true
        delay(300); showButtons = true
    }

    // Pulsing animation for logo
    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrainFoodBlack,
                        Color(0xFF0A0A0A),
                        BrainFoodBlack
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle decorative circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-200).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.02f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 120.dp, y = 250.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.015f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // ─── Logo ───
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🧠",
                        fontSize = 56.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ─── Title ───
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -20 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BrainFood",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-1).sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tu asistente de cocina inteligente",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(60.dp))

            // ─── Buttons ───
            AnimatedVisibility(
                visible = showButtons,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 40 }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Google Sign-In Button
                    Button(
                        onClick = {
                            viewModel.signInWithGoogle(context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                        }
                        Text(
                            text = if (authState is AuthState.Loading) "Conectando..."
                                   else "Iniciar con Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Guest Button
                    OutlinedButton(
                        onClick = onContinueAsGuest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            )
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        ),
                        enabled = authState !is AuthState.Loading
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Continuar sin cuenta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Error message
                    if (authState is AuthState.Error) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF331111)
                        ) {
                            Text(
                                text = (authState as AuthState.Error).message,
                                color = Color(0xFFFF6B6B),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            // Footer
            AnimatedVisibility(
                visible = showButtons,
                enter = fadeIn(tween(800))
            ) {
                Text(
                    text = "Tus datos se guardan localmente.\nInicia sesión para respaldarlos en la nube ☁️",
                    fontSize = 12.sp,
                    color = TextHint,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
