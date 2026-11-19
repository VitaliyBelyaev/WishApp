package ru.vitaliy.belyaev.wishapp.ui.screens.tags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagColor
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.normalizeTagTitle
import ru.vitaliy.belyaev.wishapp.ui.components.header.PageHeader
import ru.vitaliy.belyaev.wishapp.ui.components.header.PageHeaderCollapse
import ru.vitaliy.belyaev.wishapp.ui.components.header.headerHeight
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.AppSnackMessage
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.SheetSearchField
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.utils.trackScreenShow

class TagsManageActions(
    val onBack: () -> Unit,
    val onShowMessage: (AppSnackMessage) -> Unit,
)

/**
 * Управление ярлыками нового вида (`tagManage` в прототипе): поиск, который и создаёт, число хотелок,
 * переименование на месте, цвет из слотов `paper-*`, удаление без окна, со снэкбаром «Вернуть».
 * Открывается из настроек и из шторки ярлыков.
 */
@Composable
fun TagsManageScreen(actions: TagsManageActions, viewModel: TagsManageViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val currentActions by rememberUpdatedState(actions)

    trackScreenShow { viewModel.trackScreenShow() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                TagsManageEvent.DuplicateTitle -> AppSnackMessage(text = resources.getString(R.string.tag_exists))
                is TagsManageEvent.Deleted -> AppSnackMessage(
                    text = resources.getString(R.string.snack_tag_deleted, event.title),
                    actionLabel = resources.getString(R.string.snack_undo),
                    onAction = event.onUndo,
                    onDismiss = event.onConfirm,
                )
            }
            currentActions.onShowMessage(message)
        }
    }

    TagsManageContent(
        state = state,
        callbacks = object : TagsManageCallbacks {
            override fun onBack() = currentActions.onBack()
            override fun onRename(tagId: String, title: String) = viewModel.onRename(tagId, title)
            override fun onColor(tagId: String, color: TagColor) = viewModel.onColor(tagId, color)
            override fun onCreate(title: String) = viewModel.onCreate(title)
            override fun onDelete(tag: TagEntity) = viewModel.onDelete(tag)
        },
    )
}

internal interface TagsManageCallbacks {
    fun onBack()
    fun onRename(tagId: String, title: String)
    fun onColor(tagId: String, color: TagColor)
    fun onCreate(title: String)
    fun onDelete(tag: TagEntity)
}

/** @param state `null`, пока ярлыки не прочитаны: виден только поиск. */
@Composable
internal fun TagsManageContent(state: TagsManageState?, callbacks: TagsManageCallbacks) {
    val ui = rememberSaveable(saver = TagsEditState.Saver) { TagsEditState() }
    SideEffect { ui.callbacks = callbacks }
    val tags = state?.tags.orEmpty()
    val query = ui.query.trim()
    val visible = if (query.isEmpty()) tags else tags.filter { it.tag.title.contains(query, ignoreCase = true) }
    val canCreate = state != null && query.isNotEmpty() &&
        tags.none { normalizeTagTitle(it.tag.title) == normalizeTagTitle(query) }
    val create = { if (canCreate) ui.create() }
    BackHandler(enabled = ui.editingId != null, onBack = ui::onSubmitEdit)

    val listState = rememberLazyListState()
    val collapsePx = with(LocalDensity.current) { PageHeaderCollapse.toPx() }
    val progress: () -> Float = {
        val isPastSearch = listState.firstVisibleItemIndex > 0
        if (isPastSearch) 1f else (listState.firstVisibleItemScrollOffset / collapsePx).coerceIn(0f, 1f)
    }
    val rowsCount = visible.size + if (canCreate) 1 else 0

    // Фон свой: при предиктивном «назад» экран уезжает, и без фона сквозь него был виден прежний
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                // Список кончается над клавиатурой, а не уходит под неё: тогда прокрутка сама
                // поднимает строку с полем названия из нижней части списка, как в прежнем экране
                .windowInsetsPadding(WindowInsets.ime.only(WindowInsetsSides.Bottom))
                .testTag(TagsManageTestTags.LIST),
            contentPadding = listPadding(),
        ) {
            item(key = KEY_SEARCH) {
                SheetSearchField(
                    query = ui.query,
                    hint = stringResource(R.string.tag_find_or_create),
                    onQueryChange = ui::onQueryChange,
                    onDone = create,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .testTag(TagsManageTestTags.SEARCH),
                )
            }
            itemsIndexed(visible, key = { _, item -> item.tag.id }) { index, item ->
                TagManageRow(
                    row = TagRowUi(
                        item = item,
                        isEditing = item.tag.id == ui.editingId,
                        isColorOpen = item.tag.id == ui.colorOpenId,
                        shape = groupShape(index, rowsCount),
                    ),
                    editText = ui.editText,
                    callbacks = ui,
                    modifier = Modifier.animateItem(),
                )
            }
            if (canCreate && state != null) {
                item(key = KEY_CREATE) {
                    CreateTagRow(query, state.nextColor, groupShape(rowsCount - 1, rowsCount), onClick = create)
                }
            }
            if (state != null) {
                item(key = KEY_HINT) { ManageHint(isEmpty = tags.isEmpty()) }
            }
        }
        PageHeader(title = stringResource(R.string.tags_manage_title), progress = progress, onBack = ui::onBack)
    }
}

@Composable
private fun listPadding(): PaddingValues {
    // Клавиатура закрывает и навигацию: при ней снизу только зазор
    val navBar = with(LocalDensity.current) {
        (WindowInsets.navigationBars.getBottom(this) - WindowInsets.ime.getBottom(this)).coerceAtLeast(0).toDp()
    }
    return PaddingValues(
        start = WishAppSpacing.s4,
        end = WishAppSpacing.s4,
        top = headerHeight() + WishAppSpacing.s1,
        bottom = navBar + BottomGap,
    )
}

@Composable
private fun ManageHint(isEmpty: Boolean) {
    Text(
        text = stringResource(if (isEmpty) R.string.tags_manage_empty else R.string.tags_manage_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = WishAppSpacing.s1, vertical = 10.dp),
    )
}

/**
 * Что открыто на экране: запрос, поле названия или цвета. Открыто что-то одно и у одного ярлыка.
 * Новое название сохраняется по галочке, по «Готово» на клавиатуре, по «назад» и когда открывают
 * другой ярлык или его цвета.
 */
@Stable
internal class TagsEditState(
    query: String = "",
    editingId: String? = null,
    editText: TextFieldValue = TextFieldValue(),
    colorOpenId: String? = null,
) : TagRowCallbacks {
    var callbacks: TagsManageCallbacks? = null
    var query by mutableStateOf(query)
        private set
    var editingId by mutableStateOf(editingId)
        private set
    var editText by mutableStateOf(editText)
        private set
    var colorOpenId by mutableStateOf(colorOpenId)
        private set

    /** Поиск может спрятать открытый ярлык: новое название сохраняем сразу, как поиск меняется. */
    fun onQueryChange(value: String) {
        if (value != query) onSubmitEdit()
        query = value
    }

    /** Кнопка «назад» в шапке: открытое поле не теряет новое название. */
    fun onBack() {
        onSubmitEdit()
        callbacks?.onBack()
    }

    fun create() {
        callbacks?.onCreate(query.trim())
        query = ""
    }

    override fun onStartEdit(tag: TagEntity) {
        onSubmitEdit()
        colorOpenId = null
        editingId = tag.id
        editText = TextFieldValue(tag.title, selection = TextRange(tag.title.length))
    }

    override fun onEditTextChange(value: TextFieldValue) {
        editText = value
    }

    override fun onSubmitEdit() {
        editingId?.let { id -> callbacks?.onRename(id, editText.text) }
        editingId = null
    }

    override fun onToggleColor(tag: TagEntity) {
        onSubmitEdit()
        colorOpenId = if (colorOpenId == tag.id) null else tag.id
    }

    override fun onColor(tag: TagEntity, color: TagColor) {
        callbacks?.onColor(tag.id, color)
    }

    override fun onDelete(tag: TagEntity) {
        if (colorOpenId == tag.id) colorOpenId = null
        if (editingId == tag.id) editingId = null
        callbacks?.onDelete(tag)
    }

    companion object {
        val Saver: Saver<TagsEditState, Any> = listSaver(
            save = { listOf(it.query, it.editingId, it.editText.text, it.editText.selection.end, it.colorOpenId) },
            restore = {
                val text = it[2] as String
                TagsEditState(
                    query = it[0] as String,
                    editingId = it[1] as String?,
                    editText = TextFieldValue(text, TextRange(it[3] as Int)),
                    colorOpenId = it[4] as String?,
                )
            },
        )
    }
}

/** Строки ярлыков — одна группа со скруглёнными краями, как `hk-list` в макете. */
private fun groupShape(index: Int, count: Int): Shape {
    val top = if (index == 0) GroupCorner else 0.dp
    val bottom = if (index == count - 1) GroupCorner else 0.dp
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

private val GroupCorner = 24.dp
private val BottomGap = 40.dp
private const val KEY_SEARCH = "search"
private const val KEY_CREATE = "create"
private const val KEY_HINT = "hint"

object TagsManageTestTags {
    const val LIST = "tags_manage_list"
    const val SEARCH = "tags_manage_search"
    const val NAME_FIELD = "tags_manage_name_field"
}
