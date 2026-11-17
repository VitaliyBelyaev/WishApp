package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.components.dock.FloatingDockHeight
import ru.vitaliy.belyaev.wishapp.ui.components.dock.floatingDarkOutline
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppElevation
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.tokenShadow

/**
 * Шапка режима выбора по `ScreenSelection`: крестик в круглой капсуле, «Выбрано: N» на всю
 * оставшуюся ширину и «Все». Как у главной шапки, подложки капсул проявляются с прокруткой,
 * в начале списка кнопки стоят прямо на фоне. Текст «Выбрано» не нажимается.
 */
@Composable
internal fun SelectionHeader(
    count: Int,
    progress: () -> Float,
    onClear: () -> Unit,
    onSelectAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        HeaderScrim(progress)
        Row(
            modifier = Modifier
                .windowInsetsPadding(HeaderInsets)
                .fillMaxWidth()
                .height(HeaderRowHeight)
                .padding(horizontal = WishAppSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
        ) {
            Box {
                CapsuleBackdrop(progress)
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .size(HeaderButtonSize)
                        .testTag(WishListTestTags.SELECTION_CLEAR),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ms_close),
                        contentDescription = stringResource(R.string.selection_clear),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(HeaderButtonSize),
                contentAlignment = Alignment.CenterStart,
            ) {
                CapsuleBackdrop(progress)
                Text(
                    text = stringResource(R.string.selection_count, count),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = WishAppSpacing.s3)
                        .testTag(WishListTestTags.SELECTION_COUNT),
                )
            }
            Box {
                CapsuleBackdrop(progress)
                Box(
                    modifier = Modifier
                        .height(HeaderButtonSize)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onSelectAll)
                        .padding(horizontal = WishAppSpacing.s4),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.selection_all),
                        style = MaterialTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * Панель режима выбора вместо дока, той же высоты 60 dp и с теми же отступами: «Сбылось»
 * (в «Сбылось» это «Снова хочу»), «Поделиться» и «Удалить». Плюса и вкладок в режиме нет.
 * Ярлыка для нескольких нет: он ставится на экране хотелки.
 */
@Composable
internal fun SelectionDock(
    isDoneSection: Boolean,
    onCompleteToggle: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(FloatingDockHeight)
            .tokenShadow(CircleShape, WishAppElevation.dock(AppThemeExtras.isDark)),
        shape = CircleShape,
        color = AppThemeExtras.colors.surfaceCard,
        border = floatingDarkOutline(),
    ) {
        // Кнопка слева, значки справа: длинная подпись сжимается многоточием, а не выталкивает значки
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DoneActionButton(
                text = stringResource(if (isDoneSection) R.string.wish_again else R.string.selection_came_true),
                icon = if (isDoneSection) R.drawable.ic_ms_favorite else R.drawable.ic_ms_check_circle,
                onClick = onCompleteToggle,
                modifier = Modifier.weight(1f, fill = false),
            )
            Row {
                DockIconButton(R.drawable.ic_ms_share, stringResource(R.string.menu_share), onShare)
                DockIconButton(
                    icon = R.drawable.ic_ms_delete,
                    description = stringResource(R.string.delete),
                    onClick = onDelete,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag(WishListTestTags.SELECTION_DELETE),
                )
            }
        }
    }
}

@Composable
private fun DoneActionButton(
    text: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = modifier
            .height(DoneButtonHeight)
            .testTag(WishListTestTags.SELECTION_DONE),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = WishAppSpacing.s4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(WishAppSpacing.s2))
            Text(
                text = text,
                style = MaterialTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DockIconButton(
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    IconButton(onClick = onClick, modifier = modifier.size(HeaderButtonSize)) {
        Icon(painter = painterResource(icon), contentDescription = description, tint = tint)
    }
}

private val DoneButtonHeight = 48.dp
