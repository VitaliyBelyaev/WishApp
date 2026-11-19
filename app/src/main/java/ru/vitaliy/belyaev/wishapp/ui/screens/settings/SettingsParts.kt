package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.ui.components.reveal.RevealOrigin
import ru.vitaliy.belyaev.wishapp.ui.components.reveal.revealOrigin
import ru.vitaliy.belyaev.wishapp.ui.theme.TokenColors
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppRadius
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.bodyS
import ru.vitaliy.belyaev.wishapp.ui.theme.isWallpaperColorsAvailable
import ru.vitaliy.belyaev.wishapp.ui.theme.label

/** Подпись блока: 13 sp, приглушённая, 20 dp сверху, у первого блока 4 dp. */
@Composable
internal fun SectionLabel(text: String, isFirst: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyS.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(
            start = WishAppSpacing.s1,
            end = WishAppSpacing.s1,
            top = if (isFirst) WishAppSpacing.s1 else 20.dp,
            bottom = WishAppSpacing.s2,
        ),
    )
}

/** Выбранная плитка обведена `primary` изнутри, как в прототипе. */
private fun Modifier.choiceTile(selected: Boolean, shape: Shape, outline: Color, background: Color) = this
    .clip(shape)
    .background(background)
    .then(if (selected) Modifier.border(2.dp, outline, shape) else Modifier)

/**
 * «Вид приложения»: две плитки со снимками и подсказка. Экран открыт в новом виде, поэтому выбран «Новый»;
 * «Классический» переключает всё приложение, стек начинается со списка прежнего вида.
 */
@Composable
internal fun LookTiles(onClassic: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
        ) {
            LookTile(
                image = R.drawable.look_preview_new,
                title = stringResource(R.string.look_new),
                note = stringResource(R.string.look_new_sub),
                selected = true,
                onClick = {},
                modifier = Modifier.weight(1f).testTag(SettingsTestTags.LOOK_NEW),
            )
            LookTile(
                image = R.drawable.look_preview_classic,
                title = stringResource(R.string.look_classic),
                note = stringResource(R.string.look_classic_sub),
                selected = false,
                onClick = onClassic,
                modifier = Modifier.weight(1f).testTag(SettingsTestTags.LOOK_CLASSIC),
            )
        }
        Text(
            text = stringResource(R.string.look_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = WishAppSpacing.s1, end = WishAppSpacing.s1, top = WishAppSpacing.s2),
        )
    }
}

@Composable
private fun LookTile(
    @DrawableRes image: Int,
    title: String,
    note: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .choiceTile(selected, RoundedCornerShape(20.dp), colors.primary, colors.surfaceContainerLow)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(start = WishAppSpacing.s2, end = WishAppSpacing.s2, top = WishAppSpacing.s2, bottom = 10.dp),
    ) {
        // Снимок верхней части экрана, 108 × 140 dp: видно шапку и первые карточки
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceContainerHigh),
            contentAlignment = Alignment.TopCenter,
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.width(108.dp).height(140.dp),
            )
        }
        Row(
            modifier = Modifier.padding(top = WishAppSpacing.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_ms_check_circle_filled),
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(WishAppSpacing.s1))
            }
            Text(text = title, style = MaterialTheme.typography.label)
        }
        Text(text = note, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

/** Тема тремя сегментами: крайние скруглены полностью, между ними 2 dp, выбранный залит `primary`. */
@Composable
internal fun ThemeSegments(selected: Theme, onSelect: (Theme, origin: Offset) -> Unit) {
    val options = listOf(
        Triple(Theme.SYSTEM, R.drawable.ic_ms_brightness_auto, R.string.theme_auto),
        Triple(Theme.LIGHT, R.drawable.ic_ms_light_mode, R.string.theme_light),
        Triple(Theme.DARK, R.drawable.ic_ms_dark_mode, R.string.theme_dark),
    )
    Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        options.forEachIndexed { index, (theme, icon, label) ->
            ThemeSegment(
                icon = icon,
                label = stringResource(label),
                selected = theme == selected,
                shape = segmentShape(index, options.size),
                onClick = { origin -> onSelect(theme, origin) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun segmentShape(index: Int, count: Int): Shape {
    val round = CornerSize(50)
    val inner = CornerSize(8.dp)
    val start = if (index == 0) round else inner
    val end = if (index == count - 1) round else inner
    return RoundedCornerShape(topStart = start, bottomStart = start, topEnd = end, bottomEnd = end)
}

@Composable
private fun ThemeSegment(
    @DrawableRes icon: Int,
    label: String,
    selected: Boolean,
    shape: Shape,
    onClick: (origin: Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val content = if (selected) colors.onPrimary else colors.onSurface
    val origin = remember { RevealOrigin() }
    Row(
        modifier = modifier
            .revealOrigin(origin)
            .height(52.dp)
            .clip(shape)
            .background(if (selected) colors.primary else colors.surfaceContainerHigh)
            .selectable(selected = selected, role = Role.RadioButton, onClick = { onClick(origin.center) })
            .padding(horizontal = WishAppSpacing.s1),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.label.copy(fontSize = 13.sp),
            color = content,
            maxLines = 1,
        )
    }
}

/**
 * Цвет: «Хурма» или «Как обои». Кружок «Как обои» берёт настоящий цвет из обоев, чтобы было видно,
 * что получится. Вызывать только на Android 12+, см. [isWallpaperColorsAvailable].
 */
@Composable
internal fun ColorTiles(useWallpaperColors: Boolean, onSelect: (Boolean, origin: Offset) -> Unit) {
    val context = LocalContext.current
    val wallpaperPrimary = remember(context) {
        if (isWallpaperColorsAvailable()) dynamicLightColorScheme(context).primary else WALLPAPER_FALLBACK
    }
    Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2)) {
        ColorTile(
            swatch = TokenColors.Light.primary,
            idleIcon = null,
            title = stringResource(R.string.color_persimmon),
            note = stringResource(R.string.color_persimmon_sub),
            selected = !useWallpaperColors,
            onClick = { origin -> onSelect(false, origin) },
            modifier = Modifier.weight(1f),
        )
        ColorTile(
            swatch = wallpaperPrimary,
            idleIcon = R.drawable.ic_ms_palette,
            title = stringResource(R.string.color_wallpaper),
            note = stringResource(R.string.color_wallpaper_sub),
            selected = useWallpaperColors,
            onClick = { origin -> onSelect(true, origin) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ColorTile(
    swatch: Color,
    @DrawableRes idleIcon: Int?,
    title: String,
    note: String,
    selected: Boolean,
    onClick: (origin: Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(WishAppRadius.md)
    // Круг расходится от кружка с цветом
    val origin = remember { RevealOrigin() }
    Row(
        modifier = modifier
            .choiceTile(selected, shape, colors.primary, colors.surfaceContainerLow)
            .selectable(selected = selected, role = Role.RadioButton, onClick = { onClick(origin.center) })
            .padding(WishAppSpacing.s2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .revealOrigin(origin)
                .size(40.dp)
                .clip(CircleShape)
                .background(swatch),
            contentAlignment = Alignment.Center,
        ) {
            val icon = if (selected) R.drawable.ic_ms_check else idleIcon
            if (icon != null) {
                Icon(painter = painterResource(icon), contentDescription = null, tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.label)
            Text(text = note, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

/** Блок строк на `surface-low`, скругление 24 dp. */
@Composable
internal fun SettingsList(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WishAppRadius.lg))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        content = content,
    )
}

/** Строка настроек от 56 dp. [isExternal] — уходит из приложения: стрелка наружу справа. */
@Composable
internal fun SettingsRow(
    @DrawableRes icon: Int,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    isExternal: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ROW_MIN_HEIGHT)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = WishAppSpacing.s3, vertical = WishAppSpacing.s2),
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = painterResource(icon), contentDescription = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.body)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (isExternal) {
            Icon(
                painter = painterResource(R.drawable.ic_ms_arrow_outward),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private val ROW_MIN_HEIGHT: Dp = 56.dp

/** Цвет кружка «Как обои», если обоев не прочитать: тот же, что в прототипе. */
private val WALLPAPER_FALLBACK = Color(0xFF4A5C92)
