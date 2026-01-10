package ru.vitaliy.belyaev.wishapp.ui.screens.wish_list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishSortMode

@ExperimentalMaterial3Api
@Composable
fun SortBottomSheetContent(
    currentSortMode: WishSortMode,
    onSortModeSelected: (WishSortMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(bottom = 8.dp)) {
        Text(
            text = stringResource(R.string.sort_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        NavMenuItemBlock(
            icon = painterResource(R.drawable.ic_format_list_numbered_24),
            title = stringResource(R.string.sort_default),
            isSelected = currentSortMode is WishSortMode.Default,
            onClick = { onSortModeSelected(WishSortMode.Default) }
        )

        NavMenuItemBlock(
            icon = painterResource(R.drawable.ic_clock_arrow_down_24),
            title = stringResource(R.string.sort_created_newest),
            isSelected = currentSortMode is WishSortMode.CreatedDateNewest,
            onClick = { onSortModeSelected(WishSortMode.CreatedDateNewest) }
        )

        NavMenuItemBlock(
            icon = painterResource(R.drawable.ic_clock_arrow_up_24),
            title = stringResource(R.string.sort_created_oldest),
            isSelected = currentSortMode is WishSortMode.CreatedDateOldest,
            onClick = { onSortModeSelected(WishSortMode.CreatedDateOldest) }
        )

        NavMenuItemBlock(
            icon = painterResource(R.drawable.ic_sort_by_alpha_24),
            title = stringResource(R.string.sort_title_az),
            isSelected = currentSortMode is WishSortMode.TitleAZ,
            onClick = { onSortModeSelected(WishSortMode.TitleAZ) }
        )

        NavMenuItemBlock(
            icon = painterResource(R.drawable.ic_sort_by_alpha_24),
            title = stringResource(R.string.sort_title_za),
            isSelected = currentSortMode is WishSortMode.TitleZA,
            onClick = { onSortModeSelected(WishSortMode.TitleZA) }
        )
    }
}
