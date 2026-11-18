package ru.vitaliy.belyaev.wishapp.ui.screens.newwish

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.textclassifier.TextClassifier
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS

internal object NewWishTestTags {
    const val SHEET = "new_wish_sheet"
    const val TITLE = "new_wish_title"
    const val COMMENT = "new_wish_comment"
    const val COMMENT_CHIP = "new_wish_comment_chip"
    const val LINK_CHIP = "new_wish_link_chip"
    const val LINK_FIELD = "new_wish_link_field"
    const val PASTE_LINK = "new_wish_paste_link"
    const val TAG_CHIP = "new_wish_tag_chip"
    const val CREATE = "new_wish_create"
    const val CLEAR = "new_wish_clear"
    const val TAG_PICKER = "new_wish_tag_picker"
}

/** Поле без рамки, похожее на текст, с подсказкой, пока пусто. */
@Composable
internal fun DraftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    style: TextStyle,
    maxLines: Int,
    modifier: Modifier = Modifier,
    onDone: (() -> Unit)? = null,
) {
    // Курсор и выделение живут здесь, текст — в черновике. Открытый черновик ставит курсор в конец
    // текста, а не в начало, чтобы сразу дописывать
    var selection by remember { mutableStateOf(TextRange(value.length)) }
    var composition by remember { mutableStateOf<TextRange?>(null) }
    val fieldValue = TextFieldValue(
        text = value,
        selection = TextRange(selection.start.coerceIn(0, value.length), selection.end.coerceIn(0, value.length)),
        composition = composition?.takeIf { it.max <= value.length },
    )
    // Поле — корень: тег и фокус на обёртке не дали бы ни фокуса, ни ввода в тестах
    BasicTextField(
        value = fieldValue,
        onValueChange = { new ->
            selection = new.selection
            composition = new.composition
            if (new.text != value) onValueChange(new.text)
        },
        textStyle = style.copy(color = style.color.takeOrElse { MaterialTheme.colorScheme.onSurface }),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = if (onDone != null) ImeAction.Done else ImeAction.Default,
        ),
        keyboardActions = KeyboardActions(onDone = onDone?.let { { it() } }),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(text = hint, style = style, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                innerTextField()
            }
        },
    )
}

internal enum class ChipStyle { TONE, ON, HINT }

/** Чип шторки по `hk-qa__chips .hk-chip`: высота 40 dp, текст 13 sp. */
@Composable
internal fun DraftChip(
    text: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ChipStyle = ChipStyle.TONE,
    trailingClose: Boolean = false,
    clickLabel: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val background = when (style) {
        ChipStyle.ON -> colors.primaryContainer
        ChipStyle.TONE, ChipStyle.HINT -> colors.surfaceContainerHigh
    }
    val content = if (style == ChipStyle.ON) colors.onPrimaryContainer else colors.onSurface
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(background)
            .then(if (style == ChipStyle.HINT) Modifier.border(2.dp, colors.primary, CircleShape) else Modifier)
            .clickable(role = Role.Button, onClickLabel = clickLabel, onClick = onClick)
            .padding(horizontal = WishAppSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.label.copy(fontSize = MaterialTheme.typography.labelS.fontSize),
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailingClose) {
            Icon(
                painter = painterResource(R.drawable.ic_ms_close),
                contentDescription = null,
                tint = content.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

/** Добавленная ссылка: домен и крестик, нажатие её убирает. */
@Composable
internal fun LinkChip(link: String, onRemove: () -> Unit) {
    val domain = remember(link) { link.toUri().host?.removePrefix("www.") ?: link }
    val description = stringResource(R.string.link_remove)
    DraftChip(
        text = domain,
        icon = R.drawable.ic_ms_link,
        style = ChipStyle.ON,
        trailingClose = true,
        onClick = onRemove,
        clickLabel = description,
        modifier = Modifier.semantics { contentDescription = "$description $domain" },
    )
}

/**
 * Поле ссылки по `hk-link`: подложка, обводка в фокусе, ошибка видна сразу под полем.
 * «Вставить» есть, пока в буфере текст: сам текст читается только по нажатию.
 */
@Composable
internal fun LinkField(
    value: String,
    isInvalid: Boolean,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    onPaste: (String?) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = true,
    onFocusLost: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val focus = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val clipboard = rememberClipboardState()
    LaunchedEffect(Unit) {
        withFrameNanos { }
        if (autoFocus && value.isEmpty()) focus.requestFocus()
    }
    val shape = RoundedCornerShape(16.dp)
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(colors.surfaceContainerLow, shape)
                .then(
                    when {
                        isInvalid -> Modifier.border(2.dp, colors.error, shape)
                        isFocused -> Modifier.border(2.dp, colors.primary, shape)
                        else -> Modifier
                    },
                )
                .padding(start = WishAppSpacing.s3, end = WishAppSpacing.s1),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_ms_link),
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.link_hint),
                        style = MaterialTheme.typography.label,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.label.copy(color = colors.onSurface),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onDone() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focus)
                        .onFocusChanged {
                            if (isFocused && !it.isFocused) onFocusLost()
                            isFocused = it.isFocused
                        }
                        .testTag(NewWishTestTags.LINK_FIELD),
                )
            }
            if (clipboard.hasText) {
                TextButton(onClick = { onPaste(clipboard.readText()) }) {
                    Text(text = stringResource(R.string.link_paste), style = MaterialTheme.typography.label)
                }
            }
        }
        if (isInvalid) {
            Text(
                text = stringResource(R.string.link_invalid),
                style = MaterialTheme.typography.labelS,
                color = colors.error,
                modifier = Modifier.padding(start = WishAppSpacing.s3, top = WishAppSpacing.s1),
            )
        }
    }
}

/**
 * Что в буфере, по одному описанию буфера, без чтения текста: так система не показывает
 * уведомление о чтении, пока не нажата «Вставить» (`ScreenLinks`).
 */
@Stable
internal class ClipboardState(private val context: Context, private val manager: ClipboardManager) {
    var hasText by mutableStateOf(false)
        private set
    var hasLink by mutableStateOf(false)
        private set

    fun refresh() {
        val description = manager.primaryClipDescription
        hasText = description != null && description.hasMimeType("text/*")
        hasLink = hasText && description != null && description.looksLikeLink()
    }

    /** Сам текст читается только по нажатию. */
    fun readText(): String? = manager.primaryClip?.takeIf { it.itemCount > 0 }
        ?.getItemAt(0)?.coerceToText(context)?.toString()

    /**
     * Android 12 и новее: система сама оценивает, ссылка ли в буфере. Ниже такой оценки нет,
     * поэтому подсказку показываем при любом тексте.
     */
    private fun ClipDescription.looksLikeLink(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return classificationStatus == ClipDescription.CLASSIFICATION_COMPLETE &&
            getConfidenceScore(TextClassifier.TYPE_URL) >= URL_CONFIDENCE
    }

    private companion object {
        const val URL_CONFIDENCE = 0.5f
    }
}

@Composable
internal fun rememberClipboardState(): ClipboardState {
    val context = LocalContext.current
    val state = remember(context) {
        ClipboardState(context, context.getSystemService(ClipboardManager::class.java)).also { it.refresh() }
    }
    DisposableEffect(state) {
        val manager = context.getSystemService(ClipboardManager::class.java)
        val listener = ClipboardManager.OnPrimaryClipChangedListener { state.refresh() }
        manager.addPrimaryClipChangedListener(listener)
        onDispose { manager.removePrimaryClipChangedListener(listener) }
    }
    return state
}

/** Кружок цвета ярлыка в строке выбора. */
@Composable
internal fun TagDot(color: Color) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(color, CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
    )
}
