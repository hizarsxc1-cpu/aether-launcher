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

private val Purple = Color(0xFFB58AFF)
private val White = Color.White

@Composable
fun AetherApp() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Purple,
            background = Color(0xFF101027)
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            ScenicBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "✦ AETHER LAUNCHER",
                            color = White,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Play Minecraft, Better Experience",
                            color = White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        "⚙     ⓘ",
                        color = White,
                        fontSize = 25.sp
                    )
                }

                // PLAY BUTTON
                Button(
                    onClick = { },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(340.dp)
                        .height(76.dp)
                        .border(
                            1.dp,
                            Purple,
                            RoundedCornerShape(18.dp)
                        ),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xAA542A91)
                    )
                ) {
                    Text(
                        "▶   PLAY",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 3.sp
                    )
                }

                // SETTINGS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingTile(
                        "▣",
                        "Game Version",
                        "Select Version",
                        Modifier.weight(1f)
                    )
                    SettingTile(
                        "▦",
                        "Memory Allocation",
                        "2 GB",
                        Modifier.weight(1f)
                    )
                    SettingTile(
                        "▤",
                        "Render",
                        "Aether Render",
                        Modifier.weight(1f)
                    )
                }

                // FOOTER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "●  Aether Launcher v1.0.0",
                        color = White,
                        fontSize = 12.sp
                    )
                    Text(
                        "♥ Built with Passion",
                        color = Purple,
                        fontSize = 12.sp
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
                listOf(
                    Color(0xFF29285D),
                    Color(0xFFB66E9C),
                    Color(0xFFFFB56E),
                    Color(0xFF182A43)
                )
            )
        )

        // SUN
        drawCircle(
            color = Color(0xFFFFE5A0),
            radius = 34f,
            center = androidx.compose.ui.geometry.Offset(
                size.width * 0.78f,
                size.height * 0.43f
            )
        )

        // BACK MOUNTAINS
        val mountain1 = Path().apply {
            moveTo(0f, size.height * 0.63f)
            lineTo(size.width * 0.20f, size.height * 0.25f)
            lineTo(size.width * 0.38f, size.height * 0.59f)
            lineTo(size.width * 0.58f, size.height * 0.30f)
            lineTo(size.width * 0.82f, size.height * 0.62f)
            lineTo(size.width, size.height * 0.40f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(mountain1, Color(0xFF353B65))

        // FRONT MOUNTAINS
        val mountain2 = Path().apply {
            moveTo(0f, size.height * 0.72f)
            lineTo(size.width * 0.30f, size.height * 0.46f)
            lineTo(size.width * 0.48f, size.height * 0.68f)
            lineTo(size.width * 0.72f, size.height * 0.48f)
            lineTo(size.width, size.height * 0.70f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(mountain2, Color(0xFF202D48))

        // LAKE
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xAA765A83),
                    Color(0xEE142D49)
                ),
                startY = size.height * 0.67f
            ),
            topLeft = androidx.compose.ui.geometry.Offset(
                0f,
                size.height * 0.67f
            ),
            size = androidx.compose.ui.geometry.Size(
                size.width,
                size.height * 0.33f
            )
        )

        // WATER REFLECTION
        for (i in 0..8) {
            val y = size.height * (0.73f + i * 0.028f)
            drawRect(
                color = Color(0x44FFC98D),
                topLeft = androidx.compose.ui.geometry.Offset(
                    size.width * 0.55f,
                    y
                ),
                size = androidx.compose.ui.geometry.Size(
                    size.width * 0.35f,
                    3f
                )
            )
        }
    }
}

@Composable
fun SettingTile(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .border(
                1.dp,
                Purple.copy(alpha = 0.7f),
                RoundedCornerShape(15.dp)
            ),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xAA17172F)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                icon,
                color = Purple,
                fontSize = 25.sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    title,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    description,
                    color = Purple,
                    fontSize = 11.sp
                )
            }
        }
    }
}
