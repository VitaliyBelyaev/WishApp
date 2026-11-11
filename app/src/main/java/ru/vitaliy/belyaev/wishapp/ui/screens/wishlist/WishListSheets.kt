package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.DoneSortMode
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishSortMode
import ru.vitaliy.belyaev.wishapp.ui.components.wish.toPaperTone
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS
import ru.vitaliy.belyaev.wishapp.ui.theme.titleM

/** С какого числа ярлыков в шторке появляется поиск. */
private const val TAGS_SEARCH_THRESHOLD = 8
private const val SELECTED_TAG_ROWS_ABOVE = 2

private val TagRowHeight = 44.dp
private val SheetRowHeight = 52.dp
private val RowShape = RoundedCornerShape(16.dp)

/**
 * Шторка по макету `hk-sheet`: фон экрана, скругление 32 dp, ручка 36×4, поля 16 dp.
 * Затемнение под ней `rgba(20,12,8,.42)`.
 *
 * Шторка поднимается максимум до статус-бара: на весь экран под него она не заходит, поэтому нет
 * перехода в полноэкранный вид со сдвигом ручки. Снизу системная навигация не отступается: контент
 * уходит под неё, отступ снизу задаёт каждая шторка сама через [sheetBottomPadding].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WishSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    // Статус-бар берём из окна экрана: в окне шторки отступы приходят не в первом кадре,
    // и шторка на миг заезжала под статус-бар
    val statusBar = with(LocalDensity.current) { WindowInsets.safeDrawing.getTop(this).toDp() }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(top = statusBar),
        contentWindowInsets = { WindowInsets(0) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        scrimColor = SheetScrim,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 14.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = WishAppSpacing.s4),
            content = content,
        )
    }
}

/** Отступ под последней строкой шторки: системная навигация и 28 dp по макету. */
@Composable
private fun sheetBottomPadding(): Dp {
    val navBar = with(LocalDensity.current) { WindowInsets.navigationBars.getBottom(this).toDp() }
    return navBar + SheetBottomGap
}

private val SheetBottomGap = 28.dp

/** Затемнение под шторкой и меню «Поделиться». */
internal val SheetScrim = Color(0x6B140C08)

/** Шторка ярлыков: нажатие выбирает ярлык и закрывает шторку, «Управление» ведёт к редактированию. */
@Composable
internal fun TagsSheet(
    tags: List<TagSheetItem>,
    selectedTagId: String?,
    onTagClick: (String) -> Unit,
    onManageClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    WishSheet(onDismiss = onDismiss) {
        var query by rememberSaveable { mutableStateOf("") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.nav_tags),
                style = MaterialTheme.typography.titleM,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = onManageClick,
                contentPadding = PaddingValues(horizontal = WishAppSpacing.s1),
                modifier = Modifier.height(44.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ms_tune),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(WishAppSpacing.s1))
                Text(text = stringResource(R.string.tags_manage), style = MaterialTheme.typography.label)
            }
        }
        if (tags.size >= TAGS_SEARCH_THRESHOLD) {
            SheetSearchField(
                query = query,
                hint = stringResource(R.string.tags_find),
                onQueryChange = { query = it },
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        val visibleTags = remember(tags, query) {
            val q = query.trim()
            if (q.isEmpty()) tags else tags.filter { it.tag.title.contains(q, ignoreCase = true) }
        }
        // Высота области списка запоминается при открытии со всеми ярлыками и при поиске не меняется:
        // шторка не прыгает, когда список сужается или выезжает клавиатура
        val density = LocalDensity.current
        var listHeight by remember { mutableStateOf<Dp?>(null) }
        Box(
            modifier = Modifier
                .then(listHeight?.let { Modifier.height(it) } ?: Modifier)
                .onSizeChanged {
                    if (listHeight == null && query.isEmpty()) listHeight = with(density) { it.height.toDp() }
                },
        ) {
            if (visibleTags.isEmpty()) {
                Text(
                    text = stringResource(R.string.tags_not_found),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = WishAppSpacing.s3, vertical = WishAppSpacing.s4),
                )
            }
            // Строки прокручиваются под системную навигацию и клавиатуру, последняя останавливается над ними
            val ime = with(density) { WindowInsets.ime.getBottom(this).toDp() }
            val navBar = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
            // Шторка открывается так, чтобы выбранный ярлык был виден: над ним пара строк для ориентира.
            // Сразу на месте, без анимации прокрутки
            val listState = rememberLazyListState(
                initialFirstVisibleItemIndex = remember {
                    val selectedIndex = visibleTags.indexOfFirst { it.tag.id == selectedTagId }
                    (selectedIndex - SELECTED_TAG_ROWS_ABOVE).coerceAtLeast(0)
                },
            )
            // Новый запрос показывает совпадения с начала, а не с места прошлой прокрутки
            var scrolledForQuery by rememberSaveable { mutableStateOf(query) }
            LaunchedEffect(query) {
                if (query != scrolledForQuery) {
                    scrolledForQuery = query
                    listState.scrollToItem(0)
                }
            }
            // В макете список шире полей шторки на 4 dp с каждой стороны
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .layoutWider(WishAppSpacing.s1)
                    .nestedScroll(KeepFlingInList),
                contentPadding = PaddingValues(bottom = maxOf(ime, navBar) + SheetBottomGap),
            ) {
                items(visibleTags, key = { it.tag.id }) { item ->
                    TagRow(
                        item = item,
                        isSelected = item.tag.id == selectedTagId,
                        onClick = { onTagClick(item.tag.id) },
                    )
                }
            }
        }
    }
}

/**
 * Скорость, которую список не израсходовал, не уходит в шторку. Иначе короткий рывок вниз по списку
 * у его начала закрывал шторку целиком. Шторку по-прежнему можно стянуть пальцем: протащенная
 * дальше порога она закрывается, ближе возвращается наверх.
 */
private val KeepFlingInList = object : NestedScrollConnection {
    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}

/** Растягивает элемент на [extra] за поля родителя с обеих сторон. */
private fun Modifier.layoutWider(extra: Dp): Modifier = layout { measurable, constraints ->
    val extraPx = extra.roundToPx()
    val wider = constraints.copy(
        minWidth = constraints.minWidth + extraPx * 2,
        maxWidth = constraints.maxWidth + extraPx * 2,
    )
    val placeable = measurable.measure(wider)
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extraPx, 0) }
}

@Composable
private fun TagRow(item: TagSheetItem, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val contentColor = if (isSelected) colors.onPrimaryContainer else colors.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TagRowHeight)
            .clip(RowShape)
            .background(if (isSelected) colors.primaryContainer else Color.Transparent)
            .selectable(selected = isSelected, onClick = onClick)
            .padding(horizontal = WishAppSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3),
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(AppThemeExtras.colors.paper(item.tag.color.toPaperTone()), CircleShape)
                .border(1.dp, colors.outlineVariant, CircleShape),
        )
        Text(
            text = item.tag.title,
            style = MaterialTheme.typography.body,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_ms_check),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = item.wishesCount.toString(),
            style = MaterialTheme.typography.labelS,
            color = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun SheetSearchField(
    query: String,
    hint: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp))
            .padding(horizontal = WishAppSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_ms_search),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(WishAppSpacing.s2))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.body.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            // Кнопка шире значка, край зоны нажатия заходит в поле справа
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier
                    .offset(x = WishAppSpacing.s2)
                    .size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ms_close),
                    contentDescription = stringResource(R.string.cd_clear_search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** Варианты сортировки раздела. У «Сбылось» свой набор без «Моего порядка». */
internal fun sortOptions(current: SectionSort): List<SectionSort> = when (current) {
    is SectionSort.Main -> listOf(
        WishSortMode.Default,
        WishSortMode.CreatedDateNewest,
        WishSortMode.CreatedDateOldest,
        WishSortMode.TitleAZ,
        WishSortMode.TitleZA,
    ).map { SectionSort.Main(it) }

    is SectionSort.Done -> DoneSortMode.entries.map { SectionSort.Done(it) }
}

@Composable
internal fun sortLabel(sort: SectionSort): String = stringResource(
    when (sort) {
        is SectionSort.Main -> when (sort.mode) {
            WishSortMode.Default -> R.string.sort_my_order
            WishSortMode.CreatedDateNewest -> R.string.sort_created_newest
            WishSortMode.CreatedDateOldest -> R.string.sort_created_oldest
            WishSortMode.TitleAZ -> R.string.sort_az
            WishSortMode.TitleZA -> R.string.sort_za
        }

        is SectionSort.Done -> when (sort.mode) {
            DoneSortMode.NEWEST -> R.string.sort_created_newest
            DoneSortMode.OLDEST -> R.string.sort_created_oldest
            DoneSortMode.TITLE_AZ -> R.string.sort_az
            DoneSortMode.TITLE_ZA -> R.string.sort_za
        }
    },
)

@Composable
internal fun SortSheet(
    current: SectionSort,
    onSelect: (SectionSort) -> Unit,
    onDismiss: () -> Unit,
) {
    WishSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.menu_sort),
            style = MaterialTheme.typography.titleM,
            modifier = Modifier.padding(bottom = WishAppSpacing.s2),
        )
        sortOptions(current).forEach { option ->
            SortRow(
                text = sortLabel(option),
                isSelected = option == current,
                onClick = { onSelect(option) },
            )
        }
        Spacer(modifier = Modifier.height(sheetBottomPadding()))
    }
}

/** Строка шторки сортировки по макету `hk-row`: 52 dp, выбранная на `primary-container` с галочкой. */
@Composable
private fun SortRow(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val contentColor = if (isSelected) colors.onPrimaryContainer else colors.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SheetRowHeight)
            .clip(RowShape)
            .background(if (isSelected) colors.primaryContainer else Color.Transparent)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = WishAppSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_ms_check),
                contentDescription = null,
                tint = contentColor,
            )
        }
    }
}
