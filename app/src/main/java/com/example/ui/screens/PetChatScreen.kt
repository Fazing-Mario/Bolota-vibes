package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementsList
import com.example.ui.components.BolotaPetView
import com.example.ui.theme.*
import com.example.ui.viewmodel.BolotaUiState
import com.example.ui.viewmodel.BolotaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PetChatScreen(
    uiState: BolotaUiState,
    viewModel: BolotaViewModel,
    modifier: Modifier = Modifier
) {
    var messageInput by remember { mutableStateOf("") }
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Conversa, 1 = Conquistas, 2 = Ajustes
    val chatListState = rememberLazyListState()

    // Auto-scroll on new message
    LaunchedEffect(uiState.chatMessages.size, uiState.isChatThinking) {
        if (uiState.chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(uiState.chatMessages.size)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BolotaPetView(
                    mood = uiState.petMood,
                    level = uiState.level,
                    wearAccessories = uiState.wornAccessories,
                    size = 52.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = uiState.petName,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Nível ${uiState.level} · Companheiro IA",
                        fontSize = 12.sp,
                        color = BolotaAccentDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (selectedSection == 0) {
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Limpar conversa",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section Tabs (Conversa | Conquistas | Ajustes)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) {
                Text("Conversa", fontSize = 12.5.sp)
            }
            SegmentedButton(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) {
                Text("Guarda-roupa", fontSize = 12.5.sp)
            }
            SegmentedButton(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) {
                Text("Ajustes", fontSize = 12.5.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedSection) {
            0 -> {
                // 1. CONVERSATION TAB
                Column(modifier = Modifier.weight(1f)) {
                    // Quick Suggested Prompts
                    val suggestedPrompts = listOf(
                        "Como estou hoje?",
                        "Preciso de foco pro mestrado",
                        "Tô com preguiça de treinar",
                        "Me dá um conselho firme!",
                        "Analisa meu sono"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        items(suggestedPrompts) { prompt ->
                            SuggestionChip(
                                onClick = { viewModel.sendChatMessage(prompt) },
                                label = { Text(prompt, fontSize = 11.5.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }

                    // Chat Messages List
                    LazyColumn(
                        state = chatListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // Intro message if empty
                        if (uiState.chatMessages.isEmpty()) {
                            item {
                                ChatBubble(
                                    isUser = false,
                                    text = "Oi! Sou o ${uiState.petName}. Pode me contar do seu dia, pedir conselho para a rotina de estudos, treino ou desabafar sobre um impulso. Sou carinhoso, mas com hábito ruim eu não passo pano!",
                                    petName = uiState.petName
                                )
                            }
                        }

                        items(uiState.chatMessages) { msg ->
                            ChatBubble(
                                isUser = msg.role == "user",
                                text = msg.content,
                                petName = uiState.petName
                            )
                        }

                        if (uiState.isChatThinking) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "${uiState.petName} está pensando...",
                                        fontSize = 13.sp,
                                        color = BolotaAccentDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Chat Input Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 85.dp, top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            placeholder = { Text("Conversar com ${uiState.petName}...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (messageInput.isNotBlank()) {
                                    val text = messageInput
                                    messageInput = ""
                                    viewModel.sendChatMessage(text)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            1 -> {
                // 2. GUARDA-ROUPA E CUSTOMIZAÇÃO TAB
                var selectedSlotFilter by remember { mutableStateOf<com.example.data.model.AccessorySlot?>(null) }
                val filteredItems = remember(selectedSlotFilter) {
                    if (selectedSlotFilter == null) {
                        com.example.data.model.LevelSystem.ALL_ITEMS
                    } else {
                        com.example.data.model.LevelSystem.ALL_ITEMS.filter { it.slot == selectedSlotFilter }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // LIVE PREVIEW CARD
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                BolotaPetView(
                                    mood = "radiante",
                                    level = uiState.level,
                                    wearAccessories = uiState.wornAccessories,
                                    size = 130.dp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "${uiState.petName} · Nível ${uiState.level}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "⭐ ${uiState.levelTitle} (${uiState.totalXp} XP)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BolotaAccentDark
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Level XP progress bar
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Progresso para o Nível ${uiState.level + 1}",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Faltam ${uiState.xpToNextLevel} XP",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { uiState.levelProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape),
                                        color = BolotaAccentDark,
                                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }

                    // CATEGORY FILTER CHIPS
                    item {
                        Column {
                            Text(
                                text = "Categorias de Itens",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedSlotFilter == null,
                                        onClick = { selectedSlotFilter = null },
                                        label = { Text("Todos", fontSize = 12.sp) }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedSlotFilter == com.example.data.model.AccessorySlot.HEAD,
                                        onClick = { selectedSlotFilter = com.example.data.model.AccessorySlot.HEAD },
                                        label = { Text("Cabeça 👑", fontSize = 12.sp) }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedSlotFilter == com.example.data.model.AccessorySlot.FACE,
                                        onClick = { selectedSlotFilter = com.example.data.model.AccessorySlot.FACE },
                                        label = { Text("Rosto 🕶️", fontSize = 12.sp) }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedSlotFilter == com.example.data.model.AccessorySlot.NECK,
                                        onClick = { selectedSlotFilter = com.example.data.model.AccessorySlot.NECK },
                                        label = { Text("Pescoço 🎀", fontSize = 12.sp) }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedSlotFilter == com.example.data.model.AccessorySlot.HAND,
                                        onClick = { selectedSlotFilter = com.example.data.model.AccessorySlot.HAND },
                                        label = { Text("Mão 🛡️", fontSize = 12.sp) }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedSlotFilter == com.example.data.model.AccessorySlot.SKIN,
                                        onClick = { selectedSlotFilter = com.example.data.model.AccessorySlot.SKIN },
                                        label = { Text("Cores 🎨", fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // ITEMS LIST
                    items(filteredItems) { item ->
                        val isUnlocked = uiState.level >= item.requiredLevel || uiState.unlockedAchievements.contains(item.id)
                        val slotKey = item.slot.name
                        val isWorn = if (item.slot == com.example.data.model.AccessorySlot.SKIN) {
                            (uiState.wornAccessories["SKIN"] ?: "laranja") == item.id
                        } else {
                            uiState.wornAccessories[slotKey] == item.id
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isUnlocked) {
                                    viewModel.equipOrUnequipAccessory(item.id, item.slot)
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isWorn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isWorn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isUnlocked) {
                                        Text(text = item.emoji, fontSize = 24.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Bloqueado",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val slotName = when (item.slot) {
                                            com.example.data.model.AccessorySlot.HEAD -> "Cabeça"
                                            com.example.data.model.AccessorySlot.FACE -> "Rosto"
                                            com.example.data.model.AccessorySlot.NECK -> "Pescoço"
                                            com.example.data.model.AccessorySlot.HAND -> "Mão"
                                            com.example.data.model.AccessorySlot.SKIN -> "Cor"
                                        }
                                        Text(
                                            text = "· $slotName",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = if (isUnlocked) item.description else "🔒 Desbloqueia no Nível ${item.requiredLevel}",
                                        fontSize = 12.sp,
                                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else BolotaWarnDark,
                                        fontWeight = if (isUnlocked) FontWeight.Normal else FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                if (isUnlocked) {
                                    Button(
                                        onClick = { viewModel.equipOrUnequipAccessory(item.id, item.slot) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isWorn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isWorn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (isWorn) "Equipado ✓" else "Equipar",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Nvl ${item.requiredLevel}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // 3. SETTINGS TAB
                var petNameEdit by remember { mutableStateOf(uiState.petName) }
                var screenGoalEdit by remember { mutableStateOf(uiState.screenGoal.toString()) }
                var sleepGoalEdit by remember { mutableStateOf(uiState.sleepGoal.toString()) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Ajustes Gerais", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                                OutlinedTextField(
                                    value = petNameEdit,
                                    onValueChange = { petNameEdit = it },
                                    label = { Text("Nome do bichinho") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = screenGoalEdit,
                                        onValueChange = { screenGoalEdit = it },
                                        label = { Text("Meta tela (h/dia)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = sleepGoalEdit,
                                        onValueChange = { sleepGoalEdit = it },
                                        label = { Text("Meta sono (h)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.updateSettings(
                                            petName = petNameEdit,
                                            theme = uiState.themeMode,
                                            screenGoal = screenGoalEdit.toFloatOrNull() ?: 4f,
                                            sleepGoal = sleepGoalEdit.toFloatOrNull() ?: 7.5f
                                        )
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Salvar Ajustes")
                                }
                            }
                        }
                    }

                    item {
                        Text("Gerenciar Hábitos Bons", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    items(uiState.goodHabits) { habit ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(habit.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                IconButton(onClick = { viewModel.removeGoodHabit(habit.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remover", tint = BolotaDangerDark)
                                }
                            }
                        }
                    }

                    item {
                        Text("Gerenciar Hábitos Para Largar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    items(uiState.badHabits) { habit ->
                        var seInput by remember { mutableStateOf(habit.plan?.se ?: "") }
                        var entaoInput by remember { mutableStateOf(habit.plan?.entao ?: "") }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(habit.label, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                                    IconButton(onClick = { viewModel.removeBadHabit(habit.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = BolotaDangerDark)
                                    }
                                }

                                Text("Plano Se... Então...:", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                OutlinedTextField(
                                    value = seInput,
                                    onValueChange = { seInput = it },
                                    label = { Text("Se...") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = entaoInput,
                                    onValueChange = { entaoInput = it },
                                    label = { Text("então...") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = { viewModel.updateBadHabitPlan(habit.id, seInput, entaoInput) },
                                    modifier = Modifier.align(Alignment.End),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Salvar Plano", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    isUser: Boolean,
    text: String,
    petName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                if (!isUser) {
                    Text(
                        text = petName,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = BolotaPetOrange
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
