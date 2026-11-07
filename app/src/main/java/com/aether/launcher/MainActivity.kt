package com.aether.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AetherApp()
        }
    }
}

private val DarkBlue = Color(0xFF101D2C)
private val LightBlue = Color(0xFF8BD9E8)
private val Gold = Color(0xFFFFD98A)

@Composable
fun AetherApp() {
    MaterialTheme {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBlue)
        ) {
            ScenicBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 35.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "✦ AETHER",
                            color = Color.White,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 3.sp
                        )
                        Text(
                            text = "MINECRAFT JAVA LAUNCHER",
                            color = LightBlue,
                            fontSize = 9.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    Text(
                        text = "SETTINGS  ⚙",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WELCOME BACK",
                        color = LightBlue,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your world awaits.",
                        color = Color.White,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .widthIn(max = 390.dp)
                            .fillMaxWidth()
                            .background(
                                Color(0xCC142638),
                                RoundedCornerShape(18.dp)
                            )
                            .border(
                                1.dp,
                                Color(0x668BD9E8),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DEFAULT INSTANCE",
                            color = LightBlue,
                            fontSize = 10.sp,
                            letterSpacing = 3.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Minecraft 1.21.10",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(49.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF70C7D7),
                                contentColor = Color(0xFF102333)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "▶   PLAY NOW",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingTile(
                            title = "GAME VERSION",
                            value = "1.21.10",
                            modifier = Modifier.weight(1f)
                        )

                        SettingTile(
                            title = "MEMORY",
                            value = "2048 MB",
                            modifier = Modifier.weight(1f)
                        )

                        SettingTile(
                            title = "RENDER",
                            value = "Default",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AETHER LAUNCHER  •  CRAFT YOUR OWN WORLD",
                        color = Color(0xFFB5CBD5),
                        fontSize = 9.sp,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ScenicBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF142C46),
                    Color(0xFF386B82),
                    Color(0xFF172E3A)
                )
            )
        )

        drawCircle(
            color = Color(0xFFFFD99A),
            radius = size.width * 0.065f,
            center = androidx.compose.ui.geometry.Offset(
                size.width * 0.78f,
                size.height * 0.28f
            )
        )

        val farMountain = Path().apply {
            moveTo(0f, size.height * 0.65f)
            lineTo(size.width * 0.20f, size.height * 0.32f)
            lineTo(size.width * 0.38f, size.height * 0.62f)
            lineTo(size.width * 0.58f, size.height * 0.35f)
            lineTo(size.width * 0.82f, size.height * 0.65f)
            lineTo(size.width, size.height * 0.42f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = farMountain,
            color = Color(0xFF294B58)
        )

        val nearMountain = Path().apply {
            moveTo(0f, size.height * 0.75f)
            lineTo(size.width * 0.25f, size.height * 0.48f)
            lineTo(size.width * 0.47f, size.height * 0.78f)
            lineTo(size.width * 0.70f, size.height * 0.50f)
            lineTo(size.width, size.height * 0.73f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = nearMountain,
            color = Color(0xFF183B3B)
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF397D83),
                    Color(0xFF102E3B)
                ),
                startY = size.height * 0.72f,
                endY = size.height
            ),
            topLeft = androidx.compose.ui.geometry.Offset(
                0f,
                size.height * 0.72f
            ),
            size = androidx.compose.ui.geometry.Size(
                size.width,
                size.height * 0.28f
            )
        )

        for (i in 0..12) {
            val x = size.width * (i / 12f)
            drawLine(
                color = Color(0x447BD0D0),
                start = androidx.compose.ui.geometry.Offset(
                    x,
                    size.height * 0.82f
                ),
                end = androidx.compose.ui.geometry.Offset(
                    x + size.width * 0.04f,
                    size.height
                ),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun SettingTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Color(0xCC172B3B),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                Color(0x447CC5D5),
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 13.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = LightBlue,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
