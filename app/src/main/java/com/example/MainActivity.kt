package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    MoaMainScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MoaMainScreen(
    modifier: Modifier = Modifier,
    viewModel: MoaViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val liveAmplitude by viewModel.amplitude.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val messages by viewModel.messageHistory.collectAsStateWithLifecycle()

    val pMin = android.Manifest.permission.RECORD_AUDIO
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.toggleVoiceInput(context)
        } else {
            Toast.makeText(
                context,
                "Microphone permission is required for hardware capture. Moa is starting in beautiful demo simulator instead!",
                Toast.LENGTH_LONG
            ).show()
            viewModel.toggleVoiceInput(context) // Falls back to beautiful wave simulator
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            Toast.makeText(context, "Image captured!", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission required.", Toast.LENGTH_SHORT).show()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            Toast.makeText(context, "${uris.size} files selected!", Toast.LENGTH_SHORT).show()
        }
    }

    // High quality primary blended four-color gradient
    val moaBlendedGradient = Brush.linearGradient(
        colors = listOf(
            DeepBlue,
            RoyalPurple,
            VibrantGold,
            RichRed
        )
    )

    var showProfileDialog by remember { mutableStateOf(false) }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("User Profile", fontWeight = FontWeight.Bold, color = OnPrimaryText) },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Name: moa User", color = OnPrimaryText)
                    Text("Status: Connected to Gemini Pro", color = Color.Green)
                    Text("API Configured: ${BuildConfig.GEMINI_API_KEY.isNotEmpty()}", color = OnSecondaryText)
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Close", color = RoyalPurple)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    Box(
        modifier = modifier
            .background(DarkSpaceBackground)
    ) {
        // Step 1: Decorative Ambient Glowing Orbs in Background for true luxury interface
        AmbientGlowingOrbs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // STEP 2: Header
            MoaHeader(moaBlendedGradient) {
                showProfileDialog = true
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP 3: Large, circular, fluid 3D-like avatar centerpiece
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FluidIntelligentBubble(
                    isRecording = isRecording,
                    amplitude = liveAmplitude,
                    gradient = moaBlendedGradient
                )
            }

            // STEP 4: Tagline
            Text(
                text = "Your massive intelligence, made personal.",
                color = OnPrimaryText,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .testTag("app_tagline")
            )

            // STEP 5: Scrolling interactive connection log + chat screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, BorderFrostedGlass, RoundedCornerShape(16.dp))
                    .background(SurfaceFrostedGlass)
                    .padding(12.dp)
            ) {
                val lazyListState = rememberLazyListState()
                
                // Keep list automatically scrolled to newest messages
                LaunchedEffect(messages.size) {
                    if (messages.isNotEmpty()) {
                        lazyListState.animateScrollToItem(messages.size - 1)
                    }
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = messages, key = { it.id }) { msg ->
                        ChatBubbleRow(message = msg)
                    }
                }

                // Small indicator of live connections
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            color = Color(0x3D000000),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = when (connectionStatus) {
                                    AudioRepository.ConnectionStatus.CONNECTED -> Color.Green
                                    AudioRepository.ConnectionStatus.CONNECTING -> VibrantGold
                                    else -> Color.Gray
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (connectionStatus) {
                            AudioRepository.ConnectionStatus.CONNECTED -> "Live WebSocket"
                            AudioRepository.ConnectionStatus.CONNECTING -> "Connecting..."
                            else -> "Offline Voice"
                        },
                        color = OnSecondaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // If logs get crowded, allow resetting history easily
                IconButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(32.dp)
                        .background(Color(0x22FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Chat",
                        tint = OnSecondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Moa Button (Floating Action Button layered on top of the chat)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 8.dp, end = 8.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(moaBlendedGradient)
                        .clickable {
                            // Can add additional actions here like expanding an AI tool menu
                            val audioPermState = ContextCompat.checkSelfPermission(context, pMin)
                            if (audioPermState == PackageManager.PERMISSION_GRANTED) {
                                viewModel.toggleVoiceInput(context)
                            } else {
                                recordAudioPermissionLauncher.launch(pMin)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop voice assistant" else "Start voice assistant",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP 6: Grid Cards (2x2 rounded semi-transparent frosted glass)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GridTaskCard(
                            text = "Draft detailed business proposal",
                            icon = { Icon(Icons.Outlined.Edit, "Edit", tint = OnPrimaryText.copy(alpha=0.6f), modifier = Modifier.size(16.dp)) },
                            timeText = "55m",
                            viewsText = "1.2k",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectTaskPreset("Draft detailed business proposal") }
                        )
                        GridTaskCard(
                            text = "Analyze image and give feedback",
                            icon = { Icon(Icons.Outlined.CameraAlt, "Camera", tint = OnPrimaryText.copy(alpha=0.6f), modifier = Modifier.size(16.dp)) },
                            timeText = "1D",
                            viewsText = "980",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectTaskPreset("Analyze image and give feedback") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GridTaskCard(
                            text = "Draft detailed planning",
                            icon = { Icon(Icons.Outlined.PieChart, "Planning", tint = OnPrimaryText.copy(alpha=0.6f), modifier = Modifier.size(16.dp)) },
                            timeText = "70m",
                            viewsText = "12k",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectTaskPreset("Draft detailed planning") }
                        )
                        GridTaskCard(
                            text = "Schedule my month with deep work focus",
                            icon = { Icon(Icons.Outlined.DateRange, "Schedule", tint = OnPrimaryText.copy(alpha=0.6f), modifier = Modifier.size(16.dp)) },
                            timeText = "20m",
                            viewsText = "8k",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectTaskPreset("Schedule my month with deep work focus") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP 7: Bottom wide, pill-shaped input bar
            VoiceInputPillBar(
                inputText = inputText,
                onTextChange = { viewModel.updateInputText(it) },
                onSendText = {
                    viewModel.submitTextQuery(inputText)
                    focusManager.clearFocus()
                },
                isRecording = isRecording,
                onMicClick = {
                    val audioPermState = ContextCompat.checkSelfPermission(context, pMin)
                    if (audioPermState == PackageManager.PERMISSION_GRANTED) {
                        viewModel.toggleVoiceInput(context)
                    } else {
                        recordAudioPermissionLauncher.launch(pMin)
                    }
                },
                onAttachClick = {
                    filePickerLauncher.launch("*/*")
                },
                onCameraClick = {
                    val camPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (camPerm == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                gradient = moaBlendedGradient
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun MoaHeader(moaGradient: Brush, onProfileClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // User Avatar on left
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceFrostedGlass)
                .border(1.dp, BorderFrostedGlass, CircleShape)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_moa_profile_avatar_1780059938850),
                contentDescription = "User Avatar",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        }

        // Exact lowercase branding "moa" centered with subtle gradient tint
        Text(
            text = "moa",
            style = TextStyle(
                brush = moaGradient,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            modifier = Modifier.testTag("app_title")
        )

        // Notification bell on right
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceFrostedGlass)
                .border(1.dp, BorderFrostedGlass, CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = OnPrimaryText,
                modifier = Modifier.size(20.dp)
            )
            // Beautiful active notification badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(6.dp)
                    .background(RichRed, CircleShape)
            )
        }
    }
}

/**
 * Large circular centerpiece portraying a high-end neural 3D fluid bubble.
 * Morph and pulses beautifully relative to the input speech recording amplitude or breathing state!
 */
@Composable
fun FluidIntelligentBubble(
    isRecording: Boolean,
    amplitude: Float,
    gradient: Brush
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    
    // Smooth natural floating expansion scale
    val slowPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slowPulse"
    )

    // Slow organic canvas background angle rotations for fluid morph styling
    val fluidRotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotations"
    )

    // Direct physics state tracking for immediate visual amplitude responsiveness
    val activeScaleTarget = if (isRecording) {
        1.0f + (amplitude * 0.45f) // Morph aggressively representing higher active audio
    } else {
        slowPulseScale
    }

    val bubbleColorAccent by animateColorAsState(
        targetValue = if (isRecording) RoyalPurple else DeepBlue,
        animationSpec = tween(500),
        label = "bubbleColor"
    )

    val haloPulseSize by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloAlpha"
    )

    Box(
        modifier = Modifier
            .size(240.dp)
            .testTag("3d_neural_bubble"),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing energy waves
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(haloPulseSize)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            RoyalPurple.copy(alpha = haloAlpha),
                            VibrantGold.copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Backdrop glowing visual glass plate
        Box(
            modifier = Modifier
                .size(190.dp)
                .scale(activeScaleTarget)
                .shadow(elevation = 24.dp, shape = CircleShape, clip = false)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            bubbleColorAccent.copy(alpha = 0.65f),
                            DeepBlue.copy(alpha = 0.9f)
                        )
                    ),
                    shape = CircleShape
                )
                .border(2.dp, BorderFrostedGlass, CircleShape)
        )

        // Custom High-End Vector Canvas to render beautiful detailed dynamic audio wavy lines
        Canvas(
            modifier = Modifier
                .size(160.dp)
                .scale(activeScaleTarget)
        ) {
            val center = size.width / 2f
            val maxRadius = size.width * 0.45f
            
            // Draw continuous 3D overlapping rings with slightly offset rotations
            val ringCount = 3
            for (i in 0 until ringCount) {
                val cyclePhase = (fluidRotationDegrees + i * (360f / ringCount)) % 360f
                val relativeAmp = if (isRecording) amplitude else 0.12f
                val dynamicRadius = maxRadius * (0.85f + (relativeAmp * 0.18f) * Math.sin(Math.toRadians(cyclePhase.toDouble())).toFloat())

                drawCircle(
                    brush = gradient,
                    radius = dynamicRadius,
                    style = Stroke(width = 1.5.dp.toPx() + (relativeAmp * 3f)),
                    alpha = 0.4f + (0.2f * i)
                )
            }

            // Draw center intelligent Core Dot
            drawCircle(
                color = OnPrimaryText,
                radius = 16f + (amplitude * 25f),
                alpha = 0.85f
            )
        }

        // Voice state feedback subtitle overlay, beautifully layered
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 110.dp)
        ) {
            AnimatedVisibility(
                visible = isRecording,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "LISTENING LIVE",
                    color = VibrantGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

/**
 * Clean layout representing individual chat items
 */
@Composable
fun ChatBubbleRow(message: MoaViewModel.MessageItem) {
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    }

    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bg = if (message.isUser) {
        Brush.linearGradient(colors = listOf(RoyalPurple, DeepBlue))
    } else {
        Brush.linearGradient(colors = listOf(Color(0x33475569), Color(0x1F1E293B)))
    }

    val border = if (message.isUser) {
        BorderStroke(1.dp, Color(0x4DFFFFFF))
    } else {
        BorderStroke(1.dp, BorderFrostedGlass)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bg, bubbleShape)
                .border(border, bubbleShape)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = OnPrimaryText,
                fontSize = 13.5.sp,
                fontWeight = if (message.isUser) FontWeight.Medium else FontWeight.Normal,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * 2x2 Glassmorphism preset tasks cards layout
 */
@Composable
fun GridTaskCard(
    text: String,
    icon: @Composable () -> Unit,
    timeText: String,
    viewsText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1F1E293B))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .height(115.dp) // Adjusted height for image-like cards
            .testTag("task_card_${text.replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.dp, BorderFrostedGlass, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = OnSecondaryText.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = text,
                color = OnPrimaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Outlined.Schedule, "Time", tint = OnSecondaryText.copy(alpha=0.6f), modifier = Modifier.size(12.dp))
                Text(timeText, color = OnSecondaryText.copy(alpha=0.6f), fontSize = 10.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Outlined.Visibility, "Views", tint = OnSecondaryText.copy(alpha=0.6f), modifier = Modifier.size(12.dp))
                Text(viewsText, color = OnSecondaryText.copy(alpha=0.6f), fontSize = 10.sp)
            }
        }
    }
}

/**
 * Elegant floating background ambient balls mimicking real light refraction
 */
@Composable
fun AmbientGlowingOrbs() {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")

    val floatOffset1 by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float1"
    )

    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 80f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .blur(80.dp) // Generates pure beautiful dynamic ambient clouds
    ) {
        // Red glowing ball bottom left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(RichRed.copy(alpha = 0.25f), Color.Transparent)
            ),
            radius = 260.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(
                x = size.width * 0.15f + floatOffset1,
                y = size.height * 0.85f + floatOffset2
            )
        )

        // Gold glowing ball top right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(VibrantGold.copy(alpha = 0.18f), Color.Transparent)
            ),
            radius = 290.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(
                x = size.width * 0.85f + floatOffset2,
                y = size.height * 0.15f + floatOffset1
            )
        )

        // Purple glow behind centerpiece
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(RoyalPurple.copy(alpha = 0.22f), Color.Transparent)
            ),
            radius = 320.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(
                x = size.width * 0.5f,
                y = size.height * 0.40f
            )
        )
    }
}

/**
 * Bottom Input Bar (wide pill-shaped field with Voice-input keyboard prompt and Floating microphone)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputPillBar(
    inputText: String,
    onTextChange: (String) -> Unit,
    onSendText: () -> Unit,
    isRecording: Boolean,
    onMicClick: () -> Unit,
    onAttachClick: () -> Unit,
    onCameraClick: () -> Unit,
    gradient: Brush
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Text field container as a sleek Glassmorphic pill
        TextField(
            value = inputText,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    text = "Type intent...",
                    color = OnSecondaryText.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSendText() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceFrostedGlass,
                unfocusedContainerColor = SurfaceFrostedGlass,
                disabledContainerColor = SurfaceFrostedGlass,
                focusedTextColor = OnPrimaryText,
                unfocusedTextColor = OnPrimaryText,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .border(1.dp, BorderFrostedGlass, RoundedCornerShape(26.dp))
                .testTag("text_query_field"),
            leadingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAttachClick) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach file",
                            tint = OnSecondaryText.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onCameraClick) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take picture",
                            tint = OnSecondaryText.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            trailingIcon = {
                if (inputText.isNotBlank()) {
                    IconButton(onClick = onSendText) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send text query",
                            tint = RoyalPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(onClick = onMicClick) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop voice input" else "Start voice input",
                            tint = if (isRecording) RichRed else OnSecondaryText.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        )
    }
}
