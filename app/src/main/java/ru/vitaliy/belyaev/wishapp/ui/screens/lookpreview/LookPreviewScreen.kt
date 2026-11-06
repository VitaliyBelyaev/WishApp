package ru.vitaliy.belyaev.wishapp.ui.screens.lookpreview

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.model.UiLook
import androidx.activity.ComponentActivity
import ru.vitaliy.belyaev.wishapp.ui.AppActivityViewModel
import ru.vitaliy.belyaev.wishapp.ui.components.wish.card.WishCard
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishCardModel
import ru.vitaliy.belyaev.wishapp.ui.components.wish.row.WishRow
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishSelection
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.AppSnackMessage
import ru.vitaliy.belyaev.wishapp.ui.theme.PaperTone
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.bodyS
import ru.vitaliy.belyaev.wishapp.ui.theme.displayL
import ru.vitaliy.belyaev.wishapp.ui.theme.displayM
import ru.vitaliy.belyaev.wishapp.ui.theme.isWallpaperColorsAvailable
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS
import ru.vitaliy.belyaev.wishapp.ui.theme.titleL
import ru.vitaliy.belyaev.wishapp.ui.theme.titleM

/**
 * Витрина нового вида: тема, шрифты, карточки, строки и снэкбар этапа 1. Открывается только в отладочной
 * сборке, пока флаг `ui_look_mode` равен `old_only`. Тексты здесь не переводятся: экран временный,
 * его заменит каркас списка в части 2.2.
 */
@Composable
fun LookPreviewScreen(
    appViewModel: AppActivityViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
    viewModel: LookPreviewViewModel = hiltViewModel(),
) {
    val selectedTheme by appViewModel.selectedTheme.collectAsStateWithLifecycle()
    val useWallpaperColors by appViewModel.useWallpaperColors.collectAsStateWithLifecycle()
    val realCards by viewModel.cards.collectAsStateWithLifecycle()
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    var lastUndoResult by remember { mutableStateOf("—") }

    val deletedText = stringResource(R.string.snack_deleted, "Плед")
    val undoText = stringResource(R.string.snack_undo)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = WishAppSpacing.s4)
            .padding(bottom = 96.dp)
    ) {
        Spacer(modifier = Modifier.height(WishAppSpacing.s6))
        Text(text = "Хотелки", style = MaterialTheme.typography.displayL)
        Text(
            text = "Витрина нового вида, только в отладочной сборке",
            style = MaterialTheme.typography.bodyS,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SectionTitle("Тема")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val themes = listOf(
                Theme.SYSTEM to R.string.theme_system_title,
                Theme.LIGHT to R.string.theme_light,
                Theme.DARK to R.string.theme_dark,
            )
            themes.forEachIndexed { index, (theme, title) ->
                SegmentedButton(
                    selected = selectedTheme == theme,
                    onClick = { appViewModel.onThemeChosen(theme) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = themes.size),
                ) {
                    Text(stringResource(title), maxLines = 1)
                }
            }
        }
        // На Android ниже 12 плитку «Как обои» скрываем, а не показываем недоступной
        if (isWallpaperColorsAvailable()) {
            ListItem(
                headlineContent = { Text("Как обои") },
                supportingContent = { Text("Цвета интерфейса из обоев, карточки подстраиваются") },
                trailingContent = {
                    Switch(checked = useWallpaperColors == true, onCheckedChange = appViewModel::onUseWallpaperColorsChanged)
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            )
        }
        OutlinedButton(
            onClick = { appViewModel.onUiLookChosen(UiLook.OLD) },
            modifier = Modifier.padding(top = WishAppSpacing.s2),
        ) {
            Text("Вернуть прежний вид")
        }

        SectionTitle("Снэкбар")
        Row(horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2)) {
            Button(onClick = {
                appViewModel.showSnack(
                    AppSnackMessage(
                        text = deletedText,
                        actionLabel = undoText,
                        onAction = { lastUndoResult = "отменено" },
                        onDismiss = { lastUndoResult = "подтверждено" },
                    )
                )
            }) {
                Text("С отменой")
            }
            FilledTonalButton(onClick = { appViewModel.showSnackMessageOnMain("Копия сохранена") }) {
                Text("Простой")
            }
        }
        Text(
            text = "Последнее удаление: $lastUndoResult",
            style = MaterialTheme.typography.bodyS,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = WishAppSpacing.s2),
        )

        SectionTitle("Карточки")
        Text(
            text = "Нажатие выбирает карточку, дальше видны кружки выбора",
            style = MaterialTheme.typography.bodyS,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = WishAppSpacing.s2),
        )
        CardsGrid(
            cards = realCards + SampleCards,
            selectedIds = selectedIds,
            onToggle = { id -> selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id },
        )

        SectionTitle("Строки")
        Column(verticalArrangement = Arrangement.spacedBy(WishAppSpacing.s2)) {
            (realCards + SampleCards).forEach { card ->
                WishRow(
                    model = card,
                    selection = selectionOf(card.id, selectedIds),
                    onClick = { selectedIds = if (card.id in selectedIds) selectedIds - card.id else selectedIds + card.id },
                    onLongClick = { selectedIds = selectedIds + card.id },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        SectionTitle("Ярлыки")
        Row(horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2)) {
            var selected by remember { mutableStateOf(true) }
            FilterChip(selected = selected, onClick = { selected = !selected }, label = { Text("Дом") })
            FilterChip(selected = !selected, onClick = { selected = !selected }, label = { Text("Книги") })
        }

        SectionTitle("Значки")
        Row(horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3)) {
            listOf(
                R.drawable.ic_ms_home, R.drawable.ic_ms_home_filled,
                R.drawable.ic_ms_check_circle, R.drawable.ic_ms_check_circle_filled,
                R.drawable.ic_ms_label, R.drawable.ic_ms_label_filled,
                R.drawable.ic_ms_star, R.drawable.ic_ms_star_filled,
            ).forEach { Icon(painterResource(it), contentDescription = null) }
        }

        SectionTitle("Шрифты")
        TypeSample("display-l", "Хотелки", MaterialTheme.typography.displayL)
        TypeSample("display-m", "Сбылось!", MaterialTheme.typography.displayM)
        TypeSample("title-l", "Беспроводные наушники", MaterialTheme.typography.titleL)
        TypeSample("title-m", "Кофемашина", MaterialTheme.typography.titleM)
        TypeSample("body", "С шумоподавлением, лучше чёрные", MaterialTheme.typography.body)
        TypeSample("body-s", "Рожковая, с капучинатором", MaterialTheme.typography.bodyS)
        TypeSample("label", "Добавить", MaterialTheme.typography.label)
        TypeSample("label-s", "example.com · Техника", MaterialTheme.typography.labelS)
    }
}

@Composable
private fun CardsGrid(cards: List<WishCardModel>, selectedIds: Set<String>, onToggle: (String) -> Unit) {
    // Две колонки по очереди: витрине хватает, настоящая сетка с перетаскиванием будет в 2.2–2.3
    Row(horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2)) {
        listOf(0, 1).forEach { column ->
            Column(
                verticalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
                modifier = Modifier.weight(1f),
            ) {
                cards.filterIndexed { index, _ -> index % 2 == column }.forEach { card ->
                    WishCard(
                        model = card,
                        selection = selectionOf(card.id, selectedIds),
                        onClick = { onToggle(card.id) },
                        onLongClick = { onToggle(card.id) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Режим выбора включается первой выбранной карточкой, как после долгого нажатия в списке. */
private fun selectionOf(id: String, selectedIds: Set<String>): WishSelection = when {
    selectedIds.isEmpty() -> WishSelection.NONE
    id in selectedIds -> WishSelection.SELECTED
    else -> WishSelection.UNSELECTED
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleM,
        modifier = Modifier.padding(top = WishAppSpacing.s6, bottom = WishAppSpacing.s2),
    )
}

@Composable
private fun TypeSample(token: String, sample: String, style: TextStyle) {
    Column(modifier = Modifier.padding(vertical = WishAppSpacing.s1)) {
        Text(
            text = token,
            style = MaterialTheme.typography.labelS,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = sample, style = style)
    }
}

private fun sample(
    id: String,
    title: String,
    comment: String = "",
    link: String? = null,
    linksCount: Int = if (link != null) 1 else 0,
    tag: String? = null,
    otherTags: Int = 0,
    completed: Boolean = false,
    tone: PaperTone = PaperTone.PEACH,
) = WishCardModel(
    id = "sample-$id",
    title = title,
    comment = comment,
    linkHost = link,
    linksCount = linksCount,
    shownTag = tag,
    otherTagsCount = otherTags,
    photos = emptyList(),
    isCompleted = completed,
    tone = tone,
)

private val SampleCards: List<WishCardModel> = listOf(
    sample("plaid", title = "Плед", comment = "Шерстяной"),
    sample(
        "coffee",
        title = "Набор для домашней обжарки кофе",
        comment = "Хочу попробовать обжаривать зёрна дома: небольшая ростер-машина, термометр и контейнеры " +
            "для хранения. Смотрела разные варианты, пока больше всего нравится модель из второй ссылки.",
        link = "example.com",
        linksCount = 5,
        tag = "Кухня",
        otherTags = 1,
        tone = PaperTone.BUTTER,
    ),
    sample("comment-only", title = "", comment = "Что-то тёплое на зиму, посмотреть в выходные", tag = "Дом", tone = PaperTone.MINT),
    sample("link-only", title = "", link = "shop.example.com", tone = PaperTone.SKY),
    sample("done", title = "Велосипед", comment = "Горный, 29 дюймов", tag = "Спорт", completed = true, tone = PaperTone.LIME),
    sample(
        "long-title",
        title = "Подарочное издание всех романов Булгакова с иллюстрациями",
        link = "knizhnyi-magazin-moskva.example",
        linksCount = 3,
        tag = "Книги",
        otherTags = 1,
        tone = PaperTone.LILAC,
    ),
) + PaperTone.tagTones.drop(5).mapIndexed { index, tone ->
    sample("tone-$index", title = tone.name.lowercase().replaceFirstChar { it.uppercase() }, tag = "Ярлык", tone = tone)
}

