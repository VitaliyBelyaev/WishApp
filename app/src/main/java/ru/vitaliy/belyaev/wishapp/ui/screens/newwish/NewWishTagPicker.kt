package ru.vitaliy.belyaev.wishapp.ui.screens.newwish

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.normalizeTagTitle
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.KeepFlingInList
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.SheetBottomGap
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.SheetSearchField
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.WishSheet
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS
import ru.vitaliy.belyaev.wishapp.ui.theme.titleM

/**
 * «Ярлыки хотелки» по `tagPicker`: выбор нескольких, поиск, создание из запроса. Первый выбранный
 * подписан «На карточке». Порядок строк задаётся при открытии и потом не прыгает под пальцем:
 * сначала ярлык карточки, потом выбранные, потом остальные по алфавиту.
 */
@Composable
internal fun NewWishTagPicker(
    tags: List<TagEntity>,
    selectedIds: List<String>,
    onToggle: (String) -> Unit,
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    WishSheet(onDismiss = onDismiss) {
        var query by rememberSaveable { mutableStateOf("") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .testTag(NewWishTestTags.TAG_PICKER),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.tag_picker_title),
                style = MaterialTheme.typography.titleM,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onDismiss, contentPadding = PaddingValues(horizontal = WishAppSpacing.s1)) {
                Text(text = stringResource(R.string.done), style = MaterialTheme.typography.label)
            }
        }
        SheetSearchField(
            query = query,
            hint = stringResource(R.string.tag_find_or_create),
            onQueryChange = { query = it },
            modifier = Modifier.padding(bottom = 6.dp),
        )
        // Порядок запоминается при открытии. Ярлык, созданный из поиска, встаёт наверх: после
        // сброса запроса он иначе ушёл бы в алфавитное место, возможно за край шторки
        val orderIds = remember { ArrayList<String>() }
        val listState = rememberLazyListState()
        val initialOrder = remember(tags) {
            val known = orderIds.toSet()
            if (known.isEmpty()) {
                val first = selectedIds.firstOrNull()
                orderIds += tags
                    .sortedWith(
                        compareByDescending<TagEntity> { it.id == first }.thenByDescending {
                            it.id in
                                selectedIds
                        },
                    )
                    .map { it.id }
            } else {
                val created = tags.map { it.id }.filterNot { it in known }
                orderIds.addAll(0, created)
                // Список держит на месте первую видимую строку, и вставленный сверху ярлык
                // остался бы за краем. Просим показать начало списка
                if (created.isNotEmpty()) listState.requestScrollToItem(0)
            }
            val byId = tags.associateBy { it.id }
            orderIds.mapNotNull { byId[it] }
        }
        val q = query.trim()
        val visible = if (q.isEmpty()) initialOrder else initialOrder.filter { it.title.contains(q, ignoreCase = true) }
        val canCreate = q.isNotEmpty() && tags.none { normalizeTagTitle(it.title) == normalizeTagTitle(q) }
        val density = LocalDensity.current
        val bottom = with(density) {
            maxOf(WindowInsets.ime.getBottom(this), WindowInsets.navigationBars.getBottom(this)).toDp()
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.nestedScroll(KeepFlingInList),
            contentPadding = PaddingValues(bottom = bottom + SheetBottomGap),
        ) {
            items(visible, key = { it.id }) { tag ->
                PickerRow(
                    tag = tag,
                    isSelected = tag.id in selectedIds,
                    isOnCard = tag.id == selectedIds.firstOrNull(),
                    onClick = { onToggle(tag.id) },
                )
            }
            if (canCreate) {
                item(key = "create") {
                    CreateRow(
                        title = q,
                        onClick = {
                            onCreate(q)
                            query = ""
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerRow(tag: TagEntity, isSelected: Boolean, isOnCard: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val content = if (isSelected) colors.onPrimaryContainer else colors.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RowShape)
            .background(if (isSelected) colors.primaryContainer else Color.Transparent)
            .toggleable(value = isSelected, role = Role.Checkbox, onValueChange = { onClick() })
            .padding(horizontal = WishAppSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3),
    ) {
        TagDot(tagDotColor(tag))
        Text(
            text = tag.title,
            style = MaterialTheme.typography.body,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (isOnCard) {
            Text(
                text = stringResource(R.string.tag_on_card),
                style = MaterialTheme.typography.labelS,
                color = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                maxLines = 1,
            )
        }
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_ms_check),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun CreateRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RowShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = WishAppSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_ms_add),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(R.string.tag_create, title),
            style = MaterialTheme.typography.body,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

private val RowShape = RoundedCornerShape(16.dp)
