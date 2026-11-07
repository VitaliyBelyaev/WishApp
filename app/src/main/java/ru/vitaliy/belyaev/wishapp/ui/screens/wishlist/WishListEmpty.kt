package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.displayM
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS

/** Пустой раздел: у «Хотелок» кнопки первой хотелки и восстановления из копии, у остальных только текст. */
@Composable
internal fun EmptySection(
    section: ListSection,
    contentPadding: PaddingValues,
    onAddClick: () -> Unit,
    onRestoreClick: () -> Unit,
) {
    val (title, text) = when (section) {
        ListSection.Wishes -> R.string.empty_main_title to R.string.empty_main_text
        ListSection.Done -> R.string.empty_done_title to R.string.empty_done_text
        is ListSection.Tag -> R.string.empty_tag_title to R.string.empty_tag_text
    }
    CenteredColumn(contentPadding) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.displayM,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(WishAppSpacing.s2))
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (section == ListSection.Wishes) {
            // Кнопки по макету `hk-btn`: основная 52 dp, текстовая 44 dp на 6 dp ниже
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAddClick,
                contentPadding = PaddingValues(horizontal = 20.dp),
                modifier = Modifier.height(52.dp),
            ) {
                Icon(painterResource(R.drawable.ic_ms_add), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(WishAppSpacing.s2))
                Text(stringResource(R.string.empty_main_action), style = MaterialTheme.typography.label)
            }
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(
                onClick = onRestoreClick,
                contentPadding = PaddingValues(horizontal = WishAppSpacing.s3),
                modifier = Modifier.height(44.dp),
            ) {
                Text(stringResource(R.string.empty_restore), style = MaterialTheme.typography.label)
            }
        }
    }
}

/**
 * Поиск ничего не нашёл. Если в соседнем разделе есть совпадения, предлагаем перейти туда,
 * а везде, кроме «Сбылось», можно сразу создать хотелку с этим названием.
 */
@Composable
internal fun EmptySearchResult(
    query: String,
    section: ListSection,
    sectionTitle: String,
    otherSectionMatches: Int,
    contentPadding: PaddingValues,
    onGoToOtherSection: () -> Unit,
    onCreateClick: () -> Unit,
) {
    val otherName = stringResource(if (section == ListSection.Done) R.string.nav_wishes else R.string.nav_done)
    CenteredColumn(contentPadding) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_ms_search_off),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(modifier = Modifier.height(WishAppSpacing.s4))
        Text(
            text = stringResource(R.string.search_empty_title),
            style = MaterialTheme.typography.displayM,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(WishAppSpacing.s2))
        Text(
            text = if (otherSectionMatches > 0) {
                stringResource(R.string.search_elsewhere, sectionTitle)
            } else {
                stringResource(R.string.search_empty_text)
            },
            style = MaterialTheme.typography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (otherSectionMatches > 0) {
            Spacer(modifier = Modifier.height(WishAppSpacing.s4))
            GoToOtherSectionButton(
                count = otherSectionMatches,
                sectionName = otherName,
                onClick = onGoToOtherSection,
            )
        }
        if (section != ListSection.Done && query.isNotBlank()) {
            Spacer(modifier = Modifier.height(WishAppSpacing.s3))
            OutlinedButton(
                onClick = onCreateClick,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Icon(painterResource(R.drawable.ic_ms_add), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(WishAppSpacing.s2))
                Text(
                    text = stringResource(R.string.search_create, query.trim()),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun GoToOtherSectionButton(count: Int, sectionName: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.defaultMinSize(minHeight = 56.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = WishAppSpacing.s4, vertical = WishAppSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3),
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelS,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .defaultMinSize(minWidth = 24.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.search_go_to, sectionName),
                style = MaterialTheme.typography.label,
            )
            Icon(
                painter = painterResource(R.drawable.ic_ms_arrow_forward),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CenteredColumn(contentPadding: PaddingValues, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = WishAppSpacing.s8, vertical = WishAppSpacing.s6),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
        }
    }
}
