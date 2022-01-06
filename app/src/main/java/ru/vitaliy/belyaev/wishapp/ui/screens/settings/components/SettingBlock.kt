package ru.vitaliy.belyaev.wishapp.ui.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.icon.ThemedIcon

@Composable
fun SettingBlock(title: String, onClick: () -> Unit) {

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        val (textRef, iconRef) = createRefs()
        Text(
            text = title,
            modifier = Modifier.constrainAs(textRef) {
                width = Dimension.fillToConstraints
                start.linkTo(parent.start, margin = 16.dp)
                top.linkTo(parent.top, margin = 16.dp)
                bottom.linkTo(parent.bottom, margin = 16.dp)
                end.linkTo(iconRef.start, margin = 16.dp)
            }
        )
        ThemedIcon(
            painterResource(R.drawable.ic_arrow_forward_ios),
            contentDescription = "Arrow",
            modifier = Modifier
                .size(16.dp)
                .constrainAs(iconRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 16.dp)
                }
        )
    }
}

@Composable
@Preview
fun SettingBlockPreview() {
    SettingBlock("Тема", {})
}