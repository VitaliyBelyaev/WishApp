package ru.vitaliy.belyaev.wishapp.ui.core.icon

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import ru.vitaliy.belyaev.wishapp.R

@Composable
fun ThemedIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = colorResource(R.color.iconPrimaryColor)
) {
    Icon(imageVector, contentDescription, modifier, tint)
}

@Composable
fun ThemedIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = colorResource(R.color.iconPrimaryColor)
) {
    Icon(painter, contentDescription, modifier, tint)
}