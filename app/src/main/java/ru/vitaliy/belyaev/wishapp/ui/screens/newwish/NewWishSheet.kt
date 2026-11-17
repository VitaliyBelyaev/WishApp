package ru.vitaliy.belyaev.wishapp.ui.screens.newwish

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.first
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.ui.components.wish.toPaperTone
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.AppSnackMessage
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS
import ru.vitaliy.belyaev.wishapp.ui.theme.titleL

/**
 * Шторка новой хотелки и всё вокруг неё: снэкбары, выбор фото и ярлыков. [onCreated] вызывается
 * после записи, чтобы список показал новую карточку в начале. ViewModel тот же, что у экрана
 * списка: он открывает шторку через [NewWishViewModel.open].
 */
@Composable
fun NewWishSheetHost(
    onShowMessage: (AppSnackMessage) -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
    origin: NewWishOrigin = remember { NewWishOrigin() },
    viewModel: NewWishViewModel = hiltViewModel(),
) {
    val isOpen by viewModel.isOpen.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)
    val currentOnCreated by rememberUpdatedState(onCreated)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is NewWishEvent.Created -> currentOnCreated()
                NewWishEvent.NotCreated -> currentOnShowMessage(
                    AppSnackMessage(
                        text = resources.getString(R.string.snack_not_created),
                        actionLabel = resources.getString(R.string.snack_go_back),
                        onAction = viewModel::onReturnToDraft,
                        duration = SnackbarDuration.Short,
                    ),
                )
                NewWishEvent.ImagesFailed -> currentOnShowMessage(
                    AppSnackMessage(resources.getString(R.string.photo_add_failed)),
                )
            }
        }
    }
    NewWishPanel(
        visible = isOpen,
        onDismiss = { viewModel.onAction(NewWishAction.Dismissed) },
        origin = origin,
        modifier = modifier,
    ) {
        NewWishContent(
            draft = draft,
            tags = tags,
            onAction = viewModel::onAction,
            modifier = Modifier.testTag(NewWishTestTags.SHEET),
        )
    }
}

@Composable
private fun NewWishContent(
    draft: NewWishDraft,
    tags: List<TagEntity>,
    onAction: (NewWishAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isTagPickerOpen by rememberSaveable { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_DRAFT_IMAGES),
        onResult = { uris -> onAction(NewWishAction.ImagesPicked(uris)) },
    )
    // Название и комментарий листаются вместе, когда не помещаются. Фото, ссылка и кнопки ниже
    // стоят на месте, панель растёт до статус-бара
    val textsScroll = rememberScrollState()
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScrollbar(textsScroll, MaterialTheme.colorScheme.outline)
                .verticalScroll(textsScroll),
        ) {
            DraftTexts(draft = draft, onAction = onAction)
        }
        DraftAttachments(draft = draft, onAction = onAction)
        ChipsRow(
            draft = draft,
            tags = tags,
            onAction = onAction,
            onPhotoClick = {
                if (draft.canAddImages) {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            },
            onTagsClick = { isTagPickerOpen = true },
        )
        // «Очистить» только у черновика, с которым панель открылась: стереть его не вернуть. У новой
        // хотелки, которую начали писать сейчас, его нет, закрытие и так сохранит написанное
        val openedWithDraft = remember { draft.isClearable }
        if (openedWithDraft && draft.isClearable) {
            ClearButton(onClear = { onAction(NewWishAction.Cleared) })
        }
        // Выбор ярлыков — шторка в своём окне поверх панели, панель под ней остаётся как есть
        if (isTagPickerOpen) {
            NewWishTagPicker(
                tags = tags,
                selectedIds = draft.tagIds,
                onToggle = { onAction(NewWishAction.TagToggled(it)) },
                onCreate = { onAction(NewWishAction.TagCreated(it)) },
                onDismiss = { isTagPickerOpen = false },
            )
        }
    }
}

@Composable
private fun ColumnScope.DraftTexts(draft: NewWishDraft, onAction: (NewWishAction) -> Unit) {
    val titleFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    // Курсор в названии и клавиатура сразу: панель выезжает и встаёт на клавиатуру.
    // Если пока панель появлялась, уже открыли поле ссылки, фокус у него не забираем
    val isLinkFieldOpen by rememberUpdatedState(draft.linkInput != null)
    LaunchedEffect(Unit) {
        // Поле должно появиться на экране, иначе фокус не встаёт
        withFrameNanos { }
        if (isLinkFieldOpen) return@LaunchedEffect
        titleFocus.requestFocus()
        keyboard?.show()
    }
    DraftTextField(
        value = draft.title,
        onValueChange = { value ->
            // Enter с аппаратной клавиатуры приходит переводом строки: создаёт, как галочка клавиатуры
            onAction(NewWishAction.TitleChanged(value.replace("\n", "")))
            if ('\n' in value) onAction(NewWishAction.Create(keepOpen = true))
        },
        hint = stringResource(R.string.quick_title_hint),
        style = MaterialTheme.typography.titleL,
        maxLines = Int.MAX_VALUE,
        // Галочка на клавиатуре создаёт и оставляет панель для следующей хотелки
        onDone = { onAction(NewWishAction.Create(keepOpen = true)) },
        modifier = Modifier
            .focusRequester(titleFocus)
            .testTag(NewWishTestTags.TITLE),
    )
    draft.comment?.let { comment ->
        val commentFocus = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            withFrameNanos { }
            if (comment.isEmpty()) commentFocus.requestFocus()
        }
        Spacer(modifier = Modifier.height(6.dp))
        DraftTextField(
            value = comment,
            onValueChange = { onAction(NewWishAction.CommentChanged(it)) },
            hint = stringResource(R.string.add_comment),
            style = MaterialTheme.typography.body,
            maxLines = Int.MAX_VALUE,
            modifier = Modifier
                .focusRequester(commentFocus)
                .testTag(NewWishTestTags.COMMENT),
        )
    }
}

@Composable
private fun DraftAttachments(draft: NewWishDraft, onAction: (NewWishAction) -> Unit) {
    if (draft.images.isNotEmpty()) {
        Thumbnails(images = draft.images, onRemove = { onAction(NewWishAction.ImageRemoved(it)) })
    }
    draft.linkInput?.let { input ->
        LinkField(
            value = input,
            isInvalid = draft.isLinkInvalid,
            onValueChange = { onAction(NewWishAction.LinkInputChanged(it)) },
            onDone = { onAction(NewWishAction.LinkInputDone) },
            onPaste = { onAction(NewWishAction.Pasted(it)) },
            modifier = Modifier.padding(top = WishAppSpacing.s2),
        )
    }
}

@Composable
private fun Thumbnails(images: List<DraftImage>, onRemove: (String) -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = 10.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
    ) {
        val removeDescription = stringResource(R.string.photo_remove)
        images.forEach { image ->
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp)),
            ) {
                AsyncImage(
                    model = image.data,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(ThumbCloseScrim)
                        .clickable(role = Role.Button) { onRemove(image.id) }
                        .semantics { contentDescription = removeDescription },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ms_close),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsRow(
    draft: NewWishDraft,
    tags: List<TagEntity>,
    onAction: (NewWishAction) -> Unit,
    onPhotoClick: () -> Unit,
    onTagsClick: () -> Unit,
) {
    val clipboard = rememberClipboardState()
    Row(
        modifier = Modifier.padding(top = WishAppSpacing.s3),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Чипы переносятся на столько строк, сколько нужно, прокрутки вбок нет
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Фото первым: им пользуются чаще всего
            DraftChip(
                text = if (draft.images.isEmpty()) {
                    stringResource(R.string.chip_photo)
                } else {
                    stringResource(R.string.chip_photo_count, draft.images.size)
                },
                icon = R.drawable.ic_ms_add_photo_alternate,
                style = if (draft.images.isEmpty()) ChipStyle.TONE else ChipStyle.ON,
                onClick = onPhotoClick,
            )
            draft.links.forEach { link ->
                LinkChip(link = link, onRemove = { onAction(NewWishAction.LinkRemoved(link)) })
            }
            when {
                draft.linkInput != null -> Unit
                clipboard.hasLink -> DraftChip(
                    text = stringResource(R.string.quick_paste_link),
                    icon = R.drawable.ic_ms_content_paste,
                    style = ChipStyle.HINT,
                    onClick = { onAction(NewWishAction.Pasted(clipboard.readText())) },
                    modifier = Modifier.testTag(NewWishTestTags.PASTE_LINK),
                )
                else -> DraftChip(
                    text = stringResource(R.string.chip_link),
                    icon = R.drawable.ic_ms_link,
                    onClick = { onAction(NewWishAction.LinkFieldOpened) },
                    modifier = Modifier.testTag(NewWishTestTags.LINK_CHIP),
                )
            }
            TagChip(tagIds = draft.tagIds, tags = tags, onClick = onTagsClick)
            if (draft.comment == null) {
                DraftChip(
                    text = stringResource(R.string.chip_comment),
                    icon = R.drawable.ic_ms_notes,
                    onClick = { onAction(NewWishAction.CommentOpened) },
                    modifier = Modifier.testTag(NewWishTestTags.COMMENT_CHIP),
                )
            }
        }
        SendButton(isEnabled = draft.canCreate, onClick = { onAction(NewWishAction.Create(keepOpen = false)) })
    }
}

@Composable
private fun TagChip(tagIds: List<String>, tags: List<TagEntity>, onClick: () -> Unit) {
    val first = tagIds.firstOrNull()?.let { id -> tags.firstOrNull { it.id == id } }
    DraftChip(
        text = when {
            first == null -> stringResource(R.string.chip_tag)
            tagIds.size > 1 -> "${first.title} +${tagIds.size - 1}"
            else -> first.title
        },
        icon = R.drawable.ic_ms_label,
        style = if (first == null) ChipStyle.TONE else ChipStyle.ON,
        onClick = onClick,
        modifier = Modifier.testTag(NewWishTestTags.TAG_CHIP),
    )
}

/**
 * «Очистить» внизу слева, в другом углу от галочки: стёртый черновик не вернуть, промах по нему
 * дороже промаха по закрытию. Поле нажатия выше текста, попасть легко.
 */
@Composable
private fun ClearButton(onClear: () -> Unit) {
    Text(
        text = stringResource(R.string.quick_clear),
        style = MaterialTheme.typography.labelS.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = WishAppSpacing.s1)
            // Текст на одной линии с чипами, а поле нажатия выходит за него влево
            .offset(x = -WishAppSpacing.s2)
            // Кнопка заходит в нижний отступ панели наполовину: до края 6 dp вместо 12
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height - CLEAR_INTO_BOTTOM_PADDING.roundToPx()) {
                    placeable.place(0, 0)
                }
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClear)
            .padding(vertical = 8.dp, horizontal = WishAppSpacing.s2)
            .testTag(NewWishTestTags.CLEAR),
    )
}

@Composable
private fun SendButton(isEnabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(if (isEnabled) colors.primary else colors.surfaceContainerHigh)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick)
            .testTag(NewWishTestTags.CREATE),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_ms_check),
            contentDescription = stringResource(R.string.add),
            tint = if (isEnabled) colors.onPrimary else colors.onSurfaceVariant,
        )
    }
}

private val CLEAR_INTO_BOTTOM_PADDING = 6.dp

/** Затемнение под крестиком на миниатюре: читается на любом фото. */
private val ThumbCloseScrim = Color(0xAD140C08)

/** Цвет кружка ярлыка как в шторке ярлыков списка. */
@Composable
internal fun tagDotColor(tag: TagEntity): Color = AppThemeExtras.colors.paper(tag.color.toPaperTone())
