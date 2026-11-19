package ru.vitaliy.belyaev.wishapp.ui.screens.tags

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagColor
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagWithWishCount
import ru.vitaliy.belyaev.wishapp.ui.components.wish.toPaperTone
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS

internal class TagRowUi(
    val item: TagWithWishCount,
    val isEditing: Boolean,
    val isColorOpen: Boolean,
    val shape: Shape,
)

internal interface TagRowCallbacks {
    fun onStartEdit(tag: TagEntity)
    fun onEditTextChange(value: TextFieldValue)
    fun onSubmitEdit()
    fun onToggleColor(tag: TagEntity)
    fun onColor(tag: TagEntity, color: TagColor)
    fun onDelete(tag: TagEntity)
}

/**
 * Строка ярлыка (`hk-tm`): кружок цвета, название, число хотелок и удаление. Нажатие на название
 * открывает поле с галочкой, на кружок — цвета под строкой.
 */
@Composable
internal fun TagManageRow(
    row: TagRowUi,
    editText: TextFieldValue,
    callbacks: TagRowCallbacks,
    modifier: Modifier = Modifier,
) {
    val tag = row.item.tag
    val colors = MaterialTheme.colorScheme
    val bringIntoView = remember { BringIntoViewRequester() }
    val rowSize = remember { RowSize() }
    if (row.isEditing) KeepInViewWithKeyboard(bringIntoView, rowSize)
    Column(
        modifier = modifier
            .bringIntoViewRequester(bringIntoView)
            .onSizeChanged { rowSize.value = it }
            .fillMaxWidth()
            .clip(row.shape)
            .background(colors.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(RowHeight)
                .padding(start = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val colorLabel = stringResource(R.string.cd_tag_color, tag.title)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { callbacks.onToggleColor(tag) }
                    .semantics { contentDescription = colorLabel },
                contentAlignment = Alignment.Center,
            ) {
                ColorDot(color = tag.color, size = 28.dp)
            }
            if (row.isEditing) {
                NameField(value = editText, callbacks = callbacks, modifier = Modifier.weight(1f))
                IconButton(onClick = callbacks::onSubmitEdit) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ms_check),
                        contentDescription = stringResource(R.string.done),
                        tint = colors.primary,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { callbacks.onStartEdit(tag) },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = tag.title,
                        style = MaterialTheme.typography.body,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = row.item.wishesCount.toString(),
                    style = MaterialTheme.typography.labelS,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = 10.dp),
                )
                IconButton(onClick = { callbacks.onDelete(tag) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ms_delete),
                        contentDescription = stringResource(R.string.cd_delete_tag, tag.title),
                        tint = colors.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        AnimatedVisibility(visible = row.isColorOpen) {
            ColorSwatches(selected = tag.color, onSelect = { callbacks.onColor(tag, it) })
        }
    }
}

@Composable
private fun NameField(value: TextFieldValue, callbacks: TagRowCallbacks, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    val label = stringResource(R.string.tag_new_name)
    BasicTextField(
        value = value,
        onValueChange = callbacks::onEditTextChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, color = colors.onSurface),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { callbacks.onSubmitEdit() }),
        modifier = modifier
            .focusRequester(focusRequester)
            .drawBehind {
                val stroke = UnderlineWidth.toPx()
                drawLine(colors.primary, Offset(0f, size.height), Offset(size.width, size.height), stroke)
            }
            .padding(vertical = 3.dp)
            .semantics { contentDescription = label }
            .testTag(TagsManageTestTags.NAME_FIELD),
    )
    // Фокус в том же кадре, в котором ушло поле прежнего ярлыка: иначе клавиатура на миг
    // закрывается, прокрутка в конце списка сбрасывается и всё съезжает вниз
    DisposableEffect(focusRequester) {
        focusRequester.requestFocus()
        onDispose { }
    }
}

/** Размер строки без состояния: нужен только в момент прокрутки, перерисовки не вызывает. */
private class RowSize(var value: IntSize = IntSize.Zero)

/**
 * Строка с полем названия целиком над клавиатурой и с запасом вокруг, чтобы была видна черта.
 * Сразу при открытии поля, в том числе когда клавиатура уже открыта, и на каждом шаге её выезда:
 * поднимать раньше нельзя, список в этот момент ещё во весь экран.
 */
@Composable
private fun KeepInViewWithKeyboard(requester: BringIntoViewRequester, rowSize: RowSize) {
    val ime = WindowInsets.ime
    val density = LocalDensity.current
    val marginPx = with(density) { RowViewMargin.toPx() }
    LaunchedEffect(requester) {
        snapshotFlow { ime.getBottom(density) }.collectLatest {
            // Кадр спустя: поле, получив фокус, само просит показать себя, и наш запрос
            // со строкой и запасом должен прийти после него, а не раствориться в нём
            withFrameNanos { }
            val size = rowSize.value
            requester.bringIntoView(Rect(0f, -marginPx, size.width.toFloat(), size.height + marginPx))
        }
    }
}

/** Цвета ярлыка (`hk-tm__sw`): слоты `paper-*`, выбранный обведён и с галочкой. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSwatches(selected: TagColor, onSelect: (TagColor) -> Unit) {
    val colors = MaterialTheme.colorScheme
    FlowRow(
        modifier = Modifier.padding(start = 50.dp, end = WishAppSpacing.s3, top = 2.dp, bottom = WishAppSpacing.s3),
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
        verticalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
    ) {
        TagColor.entries.forEachIndexed { index, color ->
            val isSelected = color == selected
            val label = stringResource(R.string.a11y_color_position, index + 1, TagColor.entries.size)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppThemeExtras.colors.paper(color.toPaperTone()))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) colors.onSurface else colors.outlineVariant,
                        shape = CircleShape,
                    )
                    .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(color) }
                    .semantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ms_check),
                        contentDescription = null,
                        tint = colors.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/** «Создать «…»», когда в поиске нет точного совпадения; кружок — цвет, который получит ярлык. */
@Composable
internal fun CreateTagRow(title: String, color: TagColor, shape: Shape, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(RowHeight)
            .clip(shape)
            .background(colors.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(start = WishAppSpacing.s3, end = WishAppSpacing.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ColorDot(color = color, size = 28.dp)
        Text(
            text = stringResource(R.string.tag_create, title),
            style = MaterialTheme.typography.body,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(R.drawable.ic_ms_add),
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ColorDot(color: TagColor, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(AppThemeExtras.colors.paper(color.toPaperTone()), CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
    )
}

private val RowHeight = 52.dp
private val UnderlineWidth = 2.dp
private val RowViewMargin = 12.dp
