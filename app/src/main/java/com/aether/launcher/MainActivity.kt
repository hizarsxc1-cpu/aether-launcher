
package com.aether.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    Image(
        painter = painterResource(
            id = R.drawable.aether_background
        ),
        contentDescription = "Aether Minecraft Landscape",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
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
