package ru.vitaliy.belyaev.wishapp.ui.core.snackbar

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withTimeoutOrNull
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppElevation
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.tokenShadow

/**
 * Показывает сообщения из [messages] в [hostState]. Новое сообщение сразу заменяет текущее,
 * а заменённое считается подтверждённым: у него вызывается `onDismiss`.
 */
@Composable
fun CollectAppSnackMessages(messages: Flow<AppSnackMessage>, hostState: SnackbarHostState) {
    LaunchedEffect(messages, hostState) {
        messages.collectLatest { message ->
            var isResolved = false
            try {
                // Снэкбар с отменой по умолчанию висит 6 секунд: свои сроки Material — 4 и 10
                val millis = message.durationMillis
                    ?: if (message.duration == null && message.actionLabel != null) UNDO_SNACK_MILLIS else null
                val duration = when {
                    millis != null -> SnackbarDuration.Indefinite
                    else -> message.duration ?: SnackbarDuration.Short
                }
                val show = suspend {
                    hostState.showSnackbar(
                        message = message.text,
                        actionLabel = message.actionLabel,
                        duration = duration,
                    )
                }
                // Своё время: снэкбар без срока закрываем сами, это то же, что он ушёл сам
                val result = if (millis !=
                    null
                ) {
                    withTimeoutOrNull(millis) { show() } ?: SnackbarResult.Dismissed
                } else {
                    show()
                }
                isResolved = true
                if (result == SnackbarResult.ActionPerformed) {
                    message.onAction?.invoke()
                } else {
                    message.onDismiss?.invoke()
                }
            } finally {
                // collectLatest отменил показ ради нового сообщения или экран ушёл: действие подтверждено
                if (!isResolved) {
                    message.onDismiss?.invoke()
                }
            }
        }
    }
}

/**
 * Единственный снэкбар нового вида: `inverse`, кнопка `inverse-accent`, тень дока.
 * Смахивается в любую сторону: это то же, что снэкбар закрылся сам, действие подтверждено.
 */
@Composable
fun AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        // Своё состояние смахивания у каждого сообщения, иначе следующее появлялось бы уже смахнутым
        key(data) {
            SwipeToDismissBox(
                state = rememberSwipeToDismissBoxState(),
                backgroundContent = {},
                onDismiss = { data.dismiss() },
            ) {
                AppSnackbar(data)
            }
        }
    }
}

@Composable
private fun AppSnackbar(data: SnackbarData) {
    val shape = MaterialTheme.shapes.medium
    Snackbar(
        modifier = Modifier
            .padding(horizontal = WishAppSpacing.s3)
            .widthIn(max = 560.dp)
            .tokenShadow(shape, WishAppElevation.dock(AppThemeExtras.isDark)),
        shape = shape,
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        action = data.visuals.actionLabel?.let { actionLabel ->
            {
                TextButton(onClick = { data.performAction() }) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.inversePrimary,
                    )
                }
            }
        },
    ) {
        Text(
            text = data.visuals.message,
            style = MaterialTheme.typography.label.copy(fontWeight = FontWeight.Medium),
        )
    }
}

/** Сколько висит снэкбар с «Вернуть», если время не задано. */
private const val UNDO_SNACK_MILLIS = 6_000L
