package com.aether.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

private val Dark = Color(0xFF101D2C)
private val Cyan = Color(0xFF8BD9E8)
private val Panel = Color(0xEE172B3B)

@Composable
fun AetherApp() {
    var page by remember { mutableStateOf("home") }
    var ram by remember { mutableStateOf("2048 MB") }
    var version by remember { mutableStateOf("1.21.10") }
    var playerName by remember { mutableStateOf("Player") }
    var message by remember { mutableStateOf("") }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF142C46),
                            Color(0xFF386B82),
                            Dark
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "✦ AETHER",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 3.sp
                        )
                        Text(
                            "MINECRAFT JAVA LAUNCHER",
                            color = Cyan,
                            fontSize = 9.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    Text(
                        "⚙ SETTINGS",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                page = "settings"
                                message = ""
                            }
                            .padding(10.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                when (page) {
                    "home" -> HomePage(
                        version = version,
                        ram = ram,
                        onPlay = {
                            message =
                                "Minecraft belum terhubung. Fitur Play akan dikembangkan berikutnya."
                        }
                    )

                    "settings" -> SettingsPage(
                        ram = ram,
                        version = version,
                        onRamChange = { ram = it },
                        onVersionChange = { version = it }
                    )

                    "account" -> AccountPage(
                        name = playerName,
                        onNameChange = { playerName = it }
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Panel, RoundedCornerShape(14.dp))
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NavItem("⌂ HOME", page == "home") {
                        page = "home"
                        message = ""
                    }

                    NavItem("⚙ SETTINGS", page == "settings") {
                        page = "settings"
                        message = ""
                    }

                    NavItem("♙ ACCOUNT", page == "account") {
                        page = "account"
                        message = ""
                    }
                }

                if (message.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        message,
                        color = Cyan,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ColumnScope.HomePage(
    version: String,
    ram: String,
    onPlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "WELCOME BACK",
            color = Cyan,
            fontSize = 11.sp,
            letterSpacing = 4.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Your world awaits.",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(22.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Panel, RoundedCornerShape(20.dp))
                .border(
                    1.dp,
                    Color(0x668BD9E8),
                    RoundedCornerShape(20.dp)
                )
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "DEFAULT INSTANCE",
                color = Cyan,
                fontSize = 10.sp,
                letterSpacing = 3.sp
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Minecraft $version",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Memory: $ram",
                color = Color.LightGray,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onPlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cyan,
                    contentColor = Dark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "▶ PLAY NOW",
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            "CRAFT YOUR OWN WORLD",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun ColumnScope.SettingsPage(
    ram: String,
    version: String,
    onRamChange: (String) -> Unit,
    onVersionChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "SETTINGS",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Customize your launcher",
            color = Cyan,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(20.dp))

        SectionTitle("MEMORY ALLOCATION")

        listOf(
            "1024 MB",
            "2048 MB",
            "3072 MB",
            "4096 MB"
        ).forEach {
            ChoiceItem(
                title = it,
                selected = ram == it
            ) {
                onRamChange(it)
            }
        }

        Spacer(Modifier.height(18.dp))

        SectionTitle("MINECRAFT VERSION")

        listOf(
            "1.21.10",
            "1.21.8",
            "1.20.1",
            "1.19.4"
        ).forEach {
            ChoiceItem(
                title = it,
                selected = version == it
            ) {
                onVersionChange(it)
            }
        }

        Spacer(Modifier.height(15.dp))

        Text(
            "Changes are saved while the app is open.",
            color = Color.LightGray,
            fontSize = 11.sp
        )
    }
}

@Composable
fun ColumnScope.AccountPage(
    name: String,
    onNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "PLAYER ACCOUNT",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "♙",
            color = Cyan,
            fontSize = 60.sp
        )

        Text(
            "Player Name",
            color = Cyan,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            label = {
                Text("Username")
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Cyan,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Cyan,
                unfocusedLabelColor = Color.LightGray
            )
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Local profile only - Microsoft login belum tersedia",
            color = Color.LightGray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        color = Cyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )

    Spacer(Modifier.height(8.dp))
}

@Composable
fun ChoiceItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                if (selected) Color(0xFF285365) else Panel,
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (selected) Cyan else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable {
                onClick()
            }
            .padding(15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Text(
            if (selected) "✓ SELECTED" else "SELECT",
            color = if (selected) Cyan else Color.LightGray,
            fontSize = 10.sp
        )
    }
}

@Composable
fun NavItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        title,
        color = if (selected) Cyan else Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 12.dp)
    )
}
