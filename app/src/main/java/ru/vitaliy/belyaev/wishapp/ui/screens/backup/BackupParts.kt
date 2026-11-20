package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.DisplayFontFamily
import ru.vitaliy.belyaev.wishapp.ui.theme.PaperTone
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppRadius
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.bodyS
import ru.vitaliy.belyaev.wishapp.ui.theme.displayM
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS
import ru.vitaliy.belyaev.wishapp.ui.theme.titleM

/** Текст карточек и окон копии: 14 sp, на ступень крупнее `body-s`, как в прототипе. */
internal val Typography.cardText: TextStyle get() = bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp)

/** Тон карточки статуса, как в прототипе: `plain`, `warn` (копии нет), `err`, `done` (всё в сохранности). */
internal enum class CardTone { PLAIN, WARN, ERROR, DONE }

@Composable
private fun CardTone.colors(): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        CardTone.PLAIN -> scheme.surfaceContainerLow to scheme.onSurface
        CardTone.WARN -> AppThemeExtras.colors.paper(PaperTone.BUTTER) to scheme.onSurface
        CardTone.ERROR -> scheme.surfaceContainerHigh to scheme.onSurface
        CardTone.DONE -> scheme.tertiaryContainer to scheme.onTertiaryContainer
    }
}

/**
 * Карточка статуса копии: скругление 32 dp, значок, заголовок 22 sp Unbounded, текст и действие.
 * [onRefresh] — круглый значок обновления в правом верхнем углу, на одной линии с иконкой.
 */
@Composable
internal fun StatusCard(
    tone: CardTone,
    @DrawableRes icon: Int,
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    onRefresh: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val (container, onContainer) = tone.colors()
    val muted = if (tone == CardTone.DONE) onContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WishAppRadius.xl))
            .background(container)
            .testTag(BackupTestTags.STATUS),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(painter = painterResource(icon), contentDescription = null, tint = onContainer)
            Text(
                text = title,
                style = MaterialTheme.typography.displayM,
                color = onContainer,
                modifier = Modifier
                    .padding(top = WishAppSpacing.s2, bottom = 2.dp, end = if (onRefresh != null) 40.dp else 0.dp)
                    .semantics { heading() },
            )
            if (text != null) {
                Text(text = text, style = MaterialTheme.typography.cardText, color = muted)
            }
            content()
        }
        if (onRefresh != null) {
            // Зона касания 48 dp, видимый кружок 36 dp: IconButton растянул бы фон до 48 dp
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(RefreshButtonInset - (RefreshTouchSize - RefreshButtonSize) / 2)
                    .size(RefreshTouchSize)
                    .clickable(
                        interactionSource = null,
                        indication = ripple(bounded = false, radius = RefreshButtonSize / 2),
                        role = Role.Button,
                        onClick = onRefresh,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(RefreshButtonSize)
                        .background(onContainer.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ms_refresh),
                        contentDescription = stringResource(R.string.cd_backup_refresh),
                        tint = onContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/** Главная кнопка карточки на всю ширину, 52 dp. [isDone] — зелёная «Сохранить сейчас». */
@Composable
internal fun CardButton(text: String, onClick: () -> Unit, isDone: Boolean = false, enabled: Boolean = true) {
    val scheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .padding(top = WishAppSpacing.s4)
            .fillMaxWidth()
            .height(ButtonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDone) scheme.tertiary else scheme.primary,
            contentColor = if (isDone) scheme.onTertiary else scheme.onPrimary,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.label)
    }
}

/**
 * Полоса загрузки 8 dp. [fraction] `null` — размер ещё неизвестен, полоса бежит.
 * На карточке дорожка `surface-card`, на весь экран `surface-high`.
 */
@Composable
internal fun BackupProgressBar(fraction: Float?, color: Color, track: Color) {
    val modifier = Modifier
        .padding(top = 14.dp)
        .fillMaxWidth()
        .height(8.dp)
    if (fraction == null) {
        LinearProgressIndicator(
            modifier = modifier,
            color = color,
            trackColor = track,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
        )
    } else {
        LinearProgressIndicator(
            progress = { fraction },
            modifier = modifier,
            color = color,
            trackColor = track,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    }
}

/** Блок строк на `surface-low`, скругление 24 dp. [enabled] `false` — пока идёт сохранение. */
@Composable
internal fun BackupList(enabled: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WishAppRadius.lg))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .alpha(if (enabled) 1f else DISABLED_ALPHA),
        content = content,
    )
}

/** Строка от 56 dp с подписью. Без [onClick] просто сведения, как адрес аккаунта. */
@Composable
internal fun BackupRow(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    isDanger: Boolean = false,
) {
    val color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = WishAppSpacing.s3, vertical = WishAppSpacing.s2),
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = color)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.body, color = color)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelS.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Шапка блока «Аккаунт»: аватар, имя и почта, под ней разделитель. Не нажимается и поэтому
 * выглядит не как строка-действие. Без фото — первая буква имени или почты, без имени — только
 * почта. Имя и фото есть, только если при входе просили профиль. Где лежит копия, говорят
 * карточки статуса и «Отвязать аккаунт», в шапке сервис не называем.
 */
@Composable
internal fun AccountHeader(email: String, name: String?, photoUrl: String?) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WishAppSpacing.s3, vertical = WishAppSpacing.s3),
            horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AccountAvatar(letter = (name ?: email).take(1).uppercase(), photoUrl = photoUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name ?: email,
                    style = MaterialTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (name != null) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyS,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = WishAppSpacing.s3),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun AccountAvatar(letter: String, photoUrl: String?) {
    Box(
        modifier = Modifier
            .size(AvatarSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.titleM.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        if (photoUrl != null) {
            // Без дискового кэша: политика обещает, что фото профиля приложение не хранит
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(photoUrl)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

/** Подпись блока: 13 sp, приглушённая, 20 dp сверху. */
@Composable
internal fun BackupSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyS.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(
            start = WishAppSpacing.s1,
            end = WishAppSpacing.s1,
            top = 20.dp,
            bottom = WishAppSpacing.s2,
        ),
    )
}

/**
 * Окно подтверждения: данные заменяются безвозвратно, отмены через снэкбар тут быть не может.
 * Действие красное, «Отмена» обычная.
 */
@Composable
internal fun BackupConfirmDialog(
    title: String,
    text: String,
    confirm: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppThemeExtras.colors.surfaceCard,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, lineHeight = 24.sp))
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.cardText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.testTag(BackupTestTags.CONFIRM)) {
                Text(text = confirm, style = MaterialTheme.typography.label, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel), style = MaterialTheme.typography.label)
            }
        },
    )
}

/** «Возвращаем данные» на весь экран: экран заблокирован до перезапуска. */
@Composable
internal fun RestoringScreen(progressText: String, fraction: Float?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = WishAppSpacing.s8, end = WishAppSpacing.s8, bottom = 60.dp)
            .testTag(BackupTestTags.RESTORING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_ms_history),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = stringResource(R.string.backup_restoring),
            style = MaterialTheme.typography.displayM,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = WishAppSpacing.s2)
                .semantics { heading() },
        )
        Text(
            text = progressText,
            style = MaterialTheme.typography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BackupProgressBar(
            fraction = fraction,
            color = MaterialTheme.colorScheme.primary,
            track = MaterialTheme.colorScheme.surfaceContainerHigh,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.backup_restoring_text),
            style = MaterialTheme.typography.bodyS,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private val ButtonHeight: Dp = 52.dp

private val AvatarSize: Dp = 48.dp

// Центр кружка совпадает с центром скругления угла (32 dp) и с иконкой слева: не жмётся к краю
private val RefreshButtonSize: Dp = 36.dp
private val RefreshButtonInset: Dp = 14.dp
private val RefreshTouchSize: Dp = 48.dp
private val RowMinHeight: Dp = 56.dp
private const val DISABLED_ALPHA = 0.38f
