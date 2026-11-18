package ru.vitaliy.belyaev.wishapp.ui.screens.wish

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity
import ru.vitaliy.belyaev.wishapp.ui.components.wish.linkHost
import ru.vitaliy.belyaev.wishapp.ui.screens.newwish.DraftTextField
import ru.vitaliy.belyaev.wishapp.ui.screens.newwish.LinkField
import ru.vitaliy.belyaev.wishapp.ui.screens.newwish.NewWishTagPicker
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.BulkAction
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS
import ru.vitaliy.belyaev.wishapp.ui.theme.titleL

/** Контент под фото: наезжает на него со скруглением, листается, с клавиатурой поднимается над ней. */
@Composable
internal fun WishSheetBody(
    wish: WishEntity,
    fields: WishFields,
    editor: WishEditor,
    scrollState: ScrollState,
    connection: NestedScrollConnection,
    topPx: () -> Float,
    onOpenLink: (String) -> Unit,
    onOverlay: (WishOverlay) -> Unit,
) {
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    val navBar = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .layout { measurable, constraints ->
                val top = topPx().toInt().coerceIn(0, constraints.maxHeight)
                val height = constraints.maxHeight - top
                val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(0, top) }
            }
            .clip(RoundedCornerShape(topStart = SheetOverlap, topEnd = SheetOverlap))
            .background(MaterialTheme.colorScheme.surface)
            .imePadding()
            // Текст уходит под край листа не у самого скругления, а чуть ниже и плавно гаснет
            .padding(top = SheetScrollInset)
            .fadingTopEdge(scrollState)
            .nestedScroll(connection)
            .verticalScroll(scrollState)
            .padding(start = WishAppSpacing.s4, end = WishAppSpacing.s4, top = 22.dp - SheetScrollInset),
    ) {
        WishTexts(fields = fields, editor = editor)
        WishTags(
            tags = wish.tags,
            shownTagId = wish.shownTag()?.id,
            onTagClick = { onOverlay(WishOverlay.Tag(it)) },
            onAddTag = { onOverlay(WishOverlay.TagPicker) },
            modifier = Modifier.padding(top = 14.dp),
        )
        WishLinks(links = wish.links, fields = fields, editor = editor, onOpenLink = onOpenLink) {
            onOverlay(WishOverlay.Link(it))
        }
        Text(
            text = wish.datesLine(LocalResources.current),
            style = MaterialTheme.typography.labelS,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = WishAppSpacing.s1, top = WishAppSpacing.s4),
        )
        // Под последней строкой место для кнопок действий, с клавиатурой они уходят
        val bottom = if (isImeVisible) WishAppSpacing.s4 else ActionsSpace + navBar + WishAppSpacing.s6
        Spacer(modifier = Modifier.height(bottom))
    }
}

/** Название и комментарий: выглядят как текст, при фокусе подчёркнуты. */
@Composable
private fun WishTexts(fields: WishFields, editor: WishEditor) {
    val focusManager = LocalFocusManager.current
    Column {
        WishTitleField(fields.title, editor, onDone = { focusManager.clearFocus() })
        DraftTextField(
            value = fields.comment,
            onValueChange = editor::onCommentChange,
            hint = stringResource(R.string.add_comment),
            style = MaterialTheme.typography.body.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            maxLines = Int.MAX_VALUE,
            modifier = Modifier
                .padding(top = 6.dp)
                .focusUnderline()
                .testTag(WishScreenTestTags.COMMENT),
        )
    }
}

@Composable
private fun WishTitleField(title: String, editor: WishEditor, onDone: () -> Unit) {
    DraftTextField(
        value = title,
        onValueChange = editor::onTitleChange,
        hint = stringResource(R.string.enter_title),
        style = MaterialTheme.typography.titleL,
        maxLines = Int.MAX_VALUE,
        onDone = onDone,
        modifier = Modifier
            .focusUnderline()
            .testTag(WishScreenTestTags.TITLE),
    )
}

/** Подчёркивание 2 dp `primary` у поля в фокусе. */
@Composable
private fun Modifier.focusUnderline(): Modifier {
    var isFocused by remember { mutableStateOf(false) }
    val color = MaterialTheme.colorScheme.primary
    return onFocusChanged { isFocused = it.isFocused }
        .drawBehind {
            if (isFocused) {
                drawRect(color, topLeft = Offset(0f, size.height), size = Size(size.width, 2.dp.toPx()))
            }
        }
        .padding(vertical = 2.dp)
}

@Composable
private fun WishLinks(
    links: List<String>,
    fields: WishFields,
    editor: WishEditor,
    onOpenLink: (String) -> Unit,
    onMenu: (String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(top = 18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        links.forEach { link ->
            LinkRow(link = link, onOpen = { onOpenLink(link) }, onMenu = { onMenu(link) })
        }
        LinkField(
            value = fields.linkInput,
            isInvalid = fields.isLinkInvalid,
            onValueChange = editor::onLinkInputChange,
            onDone = editor::onLinkSubmit,
            onPaste = editor::onLinkPasted,
            autoFocus = false,
            onFocusLost = editor::onLinkFieldLeft,
        )
    }
}

/** «Назад» и «⋮» кружками на линии кнопок главного экрана. Без фото между ними «Добавить фото». */
@Composable
internal fun TopButtons(
    hasPhotos: Boolean,
    onBack: () -> Unit,
    onAddPhoto: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val top = with(LocalDensity.current) { WindowInsets.safeDrawing.getTop(this).toDp() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Запас снизу под тень кнопок: пока кнопки проявляются, слой рисуется в буфер своего
            // размера, и тень за его краем обрезалась тёмной полосой
            .padding(
                top = top + ButtonsTop,
                start = WishAppSpacing.s3,
                end = WishAppSpacing.s3,
                bottom = ButtonShadowRoom,
            ),
    ) {
        FloatingCircleButton(
            icon = R.drawable.ic_ms_arrow_back,
            description = stringResource(R.string.cd_back),
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        if (!hasPhotos) {
            AddPhotoCapsule(onClick = onAddPhoto, modifier = Modifier.align(Alignment.Center))
        }
        MenuButton(onShare = onShare, onDelete = onDelete, modifier = Modifier.align(Alignment.CenterEnd))
    }
}

/** Тень кнопок уходит ниже них на смещение и размытие (`WishAppElevation.dock`). */
private val ButtonShadowRoom = 28.dp

/** «⋮»: «Поделиться этой хотелкой» и «Удалить». Удаление без диалога, с «Вернуть» на списке. */
@Composable
private fun MenuButton(onShare: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    var isOpen by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        FloatingCircleButton(
            icon = R.drawable.ic_ms_more_vert,
            description = stringResource(R.string.cd_more),
            onClick = { isOpen = true },
            modifier = Modifier.testTag(WishScreenTestTags.MENU),
        )
        DropdownMenu(
            expanded = isOpen,
            onDismissRequest = { isOpen = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = AppThemeExtras.colors.surfaceCard,
            offset = DpOffset(0.dp, WishAppSpacing.s2),
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.wish_share), style = MaterialTheme.typography.label) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_ms_share), contentDescription = null) },
                onClick = {
                    isOpen = false
                    onShare()
                },
            )
            val danger = MaterialTheme.colorScheme.error
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete), style = MaterialTheme.typography.label) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_ms_delete), contentDescription = null) },
                onClick = {
                    isOpen = false
                    onDelete()
                },
                colors = MenuDefaults.itemColors(textColor = danger, leadingIconColor = danger),
            )
        }
    }
}

@Composable
internal fun BottomActions(
    wish: WishEntity,
    onOpenLink: () -> Unit,
    onLeave: (BulkAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // С открытой клавиатурой кнопки уходят, чтобы не перекрывать поле
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    AnimatedVisibility(visible = !isImeVisible, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        WishActions(
            isCompleted = wish.isCompleted,
            hasLink = wish.links.isNotEmpty(),
            onOpenLink = onOpenLink,
            onCameTrue = { onLeave(BulkAction.CAME_TRUE) },
            onWishAgain = { onLeave(BulkAction.WISH_AGAIN) },
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = WishAppSpacing.s3, end = WishAppSpacing.s3, bottom = WishAppSpacing.s3),
        )
    }
}

@Composable
internal fun WishOverlays(
    overlay: WishOverlay?,
    state: WishScreenState,
    editor: WishEditor,
    tagsEditor: WishTagsEditor,
    onDismiss: () -> Unit,
    onUndoable: (text: Int, arg: String?, undo: () -> Unit) -> Unit,
    onCopied: (String) -> Unit,
) {
    when (overlay) {
        null -> Unit
        is WishOverlay.Tag -> TagActionsSheet(
            tag = overlay.tag,
            isShown = state.wish.shownTag()?.id == overlay.tag.id,
            onShow = {
                tagsEditor.onTagShown(overlay.tag.id)
                onDismiss()
            },
            onRemove = {
                val undo = tagsEditor.onTagRemoved(overlay.tag.id)
                onDismiss()
                onUndoable(R.string.snack_tag_removed, overlay.tag.title, undo)
            },
            onDismiss = onDismiss,
        )
        is WishOverlay.Link -> LinkActionsSheet(
            link = overlay.link,
            onCopy = {
                onCopied(overlay.link)
                onDismiss()
            },
            onEdit = {
                editor.onLinkEdit(overlay.link)
                onDismiss()
            },
            onRemove = {
                val undo = editor.onLinkRemoved(overlay.link)
                onDismiss()
                onUndoable(R.string.snack_link_removed, null, undo)
            },
            onDismiss = onDismiss,
        )
        WishOverlay.TagPicker -> {
            val shown = state.wish.shownTag()?.id
            NewWishTagPicker(
                tags = state.allTags,
                selectedIds = listOfNotNull(shown) + state.wish.tags.map { it.id }.filter { it != shown },
                onToggle = tagsEditor::onTagToggled,
                onCreate = tagsEditor::onTagCreated,
                onDismiss = onDismiss,
            )
        }
    }
}

/** Подпись хотелки в снэкбаре: название, иначе комментарий, домен ссылки или «Фото». */
internal fun WishEntity.label(resources: Resources): String = title.trim().ifEmpty {
    comment.trim().lineSequence().firstOrNull().orEmpty().ifEmpty {
        links.firstOrNull()?.let(::linkHost).orEmpty().ifEmpty { resources.getString(R.string.card_photo_only) }
    }
}

/**
 * Дата в самом конце: «Добавлена 3 сентября · изменена 15 октября». У сбывшейся вместо изменения
 * дата исполнения, у отмеченных до обновления её нет. Дни без «сегодня» и «вчера»: строк меньше.
 */
private fun WishEntity.datesLine(resources: Resources): String {
    val added = resources.getString(R.string.date_added, formatDay(createdTimestamp))
    val second = when {
        isCompleted -> completedTimestamp?.let { resources.getString(R.string.date_came_true, formatDay(it)) }
        !isSameDay(updatedTimestamp, createdTimestamp) ->
            resources.getString(R.string.date_changed, formatDay(updatedTimestamp))
        else -> null
    }
    return listOfNotNull(added, second).joinToString(" · ")
}

/** Правки в день создания — ещё создание, «изменена» не пишем. */
private fun isSameDay(first: Long, second: Long): Boolean {
    val a = Calendar.getInstance().apply { timeInMillis = first }
    val b = Calendar.getInstance().apply { timeInMillis = second }
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}

private fun formatDay(millis: Long): String {
    val locale = Locale.getDefault()
    val date = Calendar.getInstance().apply { timeInMillis = millis }
    val sameYear = date.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
    val pattern = DateFormat.getBestDateTimePattern(locale, if (sameYear) "dMMMM" else "dMMMMyyyy")
    return SimpleDateFormat(pattern, locale).format(Date(millis))
}

/** Ссылка без схемы открывается как `https`. `false`, если открыть нечем. */
internal fun Context.openLink(link: String): Boolean {
    val uri = Uri.parse(if ("://" in link) link else "https://$link")
    return try {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

/** Насколько ниже края листа обрезается прокрученный текст. */
private val SheetScrollInset = 12.dp

/** Высота, на которой прокрученный текст гаснет у верхнего края. */
private val SheetFadeHeight = 20.dp

/** Прокрученный текст у верхнего края гаснет, а не обрезается по линии. В начале текста края нет. */
private fun Modifier.fadingTopEdge(scrollState: ScrollState): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val fadePx = SheetFadeHeight.toPx()
        val visible = (scrollState.value / fadePx).coerceIn(0f, 1f)
        if (visible > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 1f - visible),
                    1f to Color.Black,
                    endY = fadePx,
                ),
                size = Size(size.width, fadePx),
                blendMode = BlendMode.DstIn,
            )
        }
    }
