package ru.makscorp.project.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.makscorp.project.domain.model.ChatSettings
import ru.makscorp.project.domain.model.GigaChatModel
import ru.makscorp.project.domain.model.OutputFormat
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    settings: ChatSettings,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (ChatSettings) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedModel by remember(settings) { mutableStateOf(settings.model) }
    var temperature by remember(settings) { mutableFloatStateOf(settings.temperature) }
    var maxTokens by remember(settings) { mutableIntStateOf(settings.maxTokens) }
    var systemPrompt by remember(settings) { mutableStateOf(settings.systemPrompt) }
    var selectedOutputFormat by remember(settings) { mutableStateOf(settings.outputFormat) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Настройки чата",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Model selection
            Text(
                text = "Модель",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GigaChatModel.entries.forEach { model ->
                    FilterChip(
                        selected = selectedModel == model,
                        onClick = { selectedModel = model },
                        label = { Text(model.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Temperature
            Text(
                text = "Температура: ${String.format("%.1f", temperature)}",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Низкая = точные ответы, высокая = креативные",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = temperature,
                onValueChange = { temperature = it },
                valueRange = 0f..2f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Max tokens
            Text(
                text = "Максимум токенов: $maxTokens",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Максимальная длина ответа",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = maxTokens.toFloat(),
                onValueChange = { maxTokens = it.roundToInt() },
                valueRange = 256f..4096f,
                steps = 14,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Output format
            Text(
                text = "Формат вывода",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "JSON формат структурирует ответ с датой, вопросом, ответом и тегами",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutputFormat.entries.forEach { format ->
                    FilterChip(
                        selected = selectedOutputFormat == format,
                        onClick = { selectedOutputFormat = format },
                        label = { Text(format.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // System prompt
            Text(
                text = "Системный промпт",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = if (selectedOutputFormat == OutputFormat.JSON)
                    "При JSON формате используется специальный промпт (ваш промпт игнорируется)"
                else
                    "Дополнительные инструкции для модели",
                style = MaterialTheme.typography.bodySmall,
                color = if (selectedOutputFormat == OutputFormat.JSON)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = systemPrompt,
                onValueChange = { systemPrompt = it },
                placeholder = { Text("Например: Ты помощник-программист...") },
                minLines = 3,
                maxLines = 5,
                enabled = selectedOutputFormat != OutputFormat.JSON,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onClearChat,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Очистить чат")
                }

                Button(
                    onClick = {
                        onSave(
                            ChatSettings(
                                model = selectedModel,
                                temperature = temperature,
                                maxTokens = maxTokens,
                                systemPrompt = systemPrompt,
                                outputFormat = selectedOutputFormat
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}
