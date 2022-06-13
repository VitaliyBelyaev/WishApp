package ru.vitaliy.belyaev.wishapp.ui.core.bottomsheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.contentColorFor
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.ui.theme.localTheme

@ExperimentalMaterialApi
@Composable
fun WishAppBottomSheet(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    sheetShape: Shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
    sheetElevation: Dp = 0.dp,
    sheetBackgroundColor: Color = localTheme.colors.bottomSheetBackgroundColor,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    scrimColor: Color = Color.Black.copy(alpha = 0.5f),
    content: @Composable () -> Unit
) {
    ModalBottomSheetLayout(
        sheetContent = sheetContent,
        modifier = modifier,
        sheetState = sheetState,
        sheetShape = sheetShape,
        sheetElevation = sheetElevation,
        sheetBackgroundColor = sheetBackgroundColor,
        sheetContentColor = sheetContentColor,
        scrimColor = scrimColor,
        content = content
    )
}