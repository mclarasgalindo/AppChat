package com.example.appchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appchat.ui.theme.AppChatTheme
import kotlinx.coroutines.launch


// ── Cores do WhatsApp ────────────────────────────────────────────────────────
private val WaGreen       = Color(0xFF25D366)
private val WaDarkGreen   = Color(0xFF128C7E)
private val WaTeal        = Color(0xFF075E54)
private val WaBubbleOut   = Color(0xFFDCF8C6)   // balão enviado
private val WaBubbleIn    = Color(0xFFFFFFFF)    // balão recebido
private val WaChatBg      = Color(0xFFECE5DD)    // fundo conversa
private val WaTimestamp   = Color(0xFF999999)
private val WaTopBar      = WaTeal
private val WaIconTint    = Color.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppChatTheme {
                Main()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Chat : Screen("chat", "Chat", Icons.Default.Call)
}

@Composable
fun Main() {
    val navController = rememberNavController()
    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Chat.route) {
                val viewModel: ChatViewModel = viewModel()
                ChatScreen(viewModel = viewModel)
            }
        }
    }
}

// ── Tela principal ───────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Rola para a última mensagem automaticamente
    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top Bar estilo WhatsApp ──────────────────────────────────────────

        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar circular
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(WaGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AI",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Assistente",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "online",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = WaTopBar
            )
        )

        // ── Área de mensagens ────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .background(WaChatBg)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(viewModel.messages) { message ->
                MessageBubble(sender = message.first, text = message.second)
            }
        }

        // ── Barra de entrada ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .background(Color(0xFFF0F0F0))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = textState,
                onValueChange = { textState = it },
                placeholder = { Text("Mensagem", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (textState.isNotBlank()) {
                        viewModel.sendMessage(textState)
                        textState = ""
                        coroutineScope.launch {
                            if (viewModel.messages.isNotEmpty())
                                listState.animateScrollToItem(viewModel.messages.size - 1)
                        }
                    }
                })
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Botão de enviar circular verde
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(WaDarkGreen),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = {
                    if (textState.isNotBlank()) {
                        viewModel.sendMessage(textState)
                        textState = ""
                        coroutineScope.launch {
                            if (viewModel.messages.isNotEmpty())
                                listState.animateScrollToItem(viewModel.messages.size - 1)
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = WaIconTint
                    )
                }
            }
        }
    }
}

// ── Balão de mensagem ────────────────────────────────────────────────────────
@Composable
fun MessageBubble(sender: String, text: String) {
    val isUser = sender.lowercase() == "você" || sender.lowercase() == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 12.dp
                    )
                )
                .background(if (isUser) WaBubbleOut else WaBubbleIn)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                if (!isUser) {
                    Text(
                        text = sender,
                        color = WaDarkGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = text,
                    color = Color(0xFF111111),
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Horário simulado (substituir por timestamp real se quiser)
                Text(
                    text = getCurrentTime(),
                    color = WaTimestamp,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// Hora atual formatada
fun getCurrentTime(): String {
    val cal = java.util.Calendar.getInstance()
    return "%02d:%02d".format(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
}