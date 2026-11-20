package ru.vitaliy.belyaev.wishapp.ui.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.components.header.PageHeader
import ru.vitaliy.belyaev.wishapp.ui.components.header.PageHeaderCollapse
import ru.vitaliy.belyaev.wishapp.ui.components.header.headerHeight
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppRadius
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.bodyS

/** Лицензии открытого ПО (экран `licenses` в прототипе): библиотеки по алфавиту, нажатие открывает текст. */
@Composable
fun LicensesScreen(onBack: () -> Unit, onOpen: (name: String) -> Unit, viewModel: LicensesViewModel = hiltViewModel()) {
    val licenses by viewModel.state.collectAsStateWithLifecycle()
    LicensesContent(names = licenses?.map { it.name }.orEmpty(), onBack = onBack, onOpen = onOpen)
}

@Composable
internal fun LicensesContent(names: List<String>, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val listState = rememberLazyListState()
    val collapsePx = with(LocalDensity.current) { PageHeaderCollapse.toPx() }
    val progress: () -> Float = {
        if (listState.firstVisibleItemIndex >
            0
        ) {
            1f
        } else {
            (listState.firstVisibleItemScrollOffset / collapsePx).coerceIn(0f, 1f)
        }
    }
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = WishAppSpacing.s4,
                end = WishAppSpacing.s4,
                top = headerHeight() + WishAppSpacing.s2,
                bottom = 40.dp + bottom,
            ),
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .testTag(LicensesTestTags.LIST),
        ) {
            itemsIndexed(names, key = { _, name -> name }) { index, name ->
                LicenseRow(name = name, shape = groupShape(index, names.size), onClick = { onOpen(name) })
            }
        }
        PageHeader(title = stringResource(R.string.licenses), progress = progress, onBack = onBack)
    }
}

/** Строки одним блоком на `surface-low`: скруглены верх первой и низ последней. */
private fun groupShape(index: Int, count: Int): Shape {
    val top = if (index == 0) WishAppRadius.lg else 0.dp
    val bottom = if (index == count - 1) WishAppRadius.lg else 0.dp
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

@Composable
private fun LicenseRow(name: String, shape: Shape, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(horizontal = WishAppSpacing.s3, vertical = WishAppSpacing.s2),
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.body,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(R.drawable.ic_ms_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** Текст одной лицензии: название в шапке, ссылки в тексте нажимаются, текст можно выделить. */
@Composable
fun LicenseScreen(name: String, onBack: () -> Unit, viewModel: LicensesViewModel = hiltViewModel()) {
    val licenses by viewModel.state.collectAsStateWithLifecycle()
    val text = licenses?.firstOrNull { it.name == name }?.text
    LicenseContent(name = name, text = text, onBack = onBack)
}

@Composable
internal fun LicenseContent(name: String, text: String?, onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val collapsePx = with(LocalDensity.current) { PageHeaderCollapse.toPx() }
    val progress: () -> Float = { (scrollState.value / collapsePx).coerceIn(0f, 1f) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(start = WishAppSpacing.s4, end = WishAppSpacing.s4, top = headerHeight() + WishAppSpacing.s2)
                .testTag(LicensesTestTags.TEXT),
        ) {
            if (text != null) {
                val linkColor = MaterialTheme.colorScheme.primary
                val context = LocalContext.current
                val annotated = remember(text, linkColor) {
                    linkify(text, linkColor) { url -> context.openInBrowser(url) }
                }
                SelectionContainer {
                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodyS,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(WishAppRadius.lg))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(WishAppSpacing.s4),
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
        PageHeader(title = name, progress = progress, onBack = onBack)
    }
}

/** Ссылки в тексте лицензии: подчёркнуты цветом `primary` и открываются в браузере. */
internal fun linkify(text: String, color: Color, onOpen: (String) -> Unit): AnnotatedString = buildAnnotatedString {
    var start = 0
    UrlRegex.findAll(text).forEach { match ->
        append(text.substring(start, match.range.first))
        val url = match.value
        val link = LinkAnnotation.Url(
            url = url,
            styles = TextLinkStyles(SpanStyle(color = color, textDecoration = TextDecoration.Underline)),
            linkInteractionListener = { onOpen(url) },
        )
        withLink(link) { append(url) }
        start = match.range.last + 1
    }
    append(text.substring(start))
}

private val UrlRegex = Regex("""https?://[^\s<>"')\]]+""")

object LicensesTestTags {
    const val LIST = "licenses_list"
    const val TEXT = "license_text"
}
