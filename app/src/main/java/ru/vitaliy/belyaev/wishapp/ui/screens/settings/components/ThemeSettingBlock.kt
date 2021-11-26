package ru.vitaliy.belyaev.wishapp.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.Theme

@Composable
fun ThemeSettingBlock(
    selectedTheme: Theme,
    onThemeClicked: (Theme) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {

        val (systemRef, darkRef, lightRef, space1, space2) = createRefs()
        val basePadding = 12.dp
        val baseShape = RoundedCornerShape(12.dp)
        val borderWidth = 2.dp

        val systemBgColorRes = getBgColorRes(Theme.SYSTEM, selectedTheme)
        val systemBorderColorRes = getBorderColorRes(Theme.SYSTEM, selectedTheme)
        Column(
            modifier = Modifier
                .background(color = colorResource(systemBgColorRes), shape = baseShape)
                .border(borderWidth, colorResource(systemBorderColorRes), baseShape)
                .clip(baseShape)
                .clickable { }
                .constrainAs(systemRef) {
                    width = Dimension.percent(0.4f)
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            Text(
                text = stringResource(R.string.theme_system_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = basePadding, top = basePadding, end = basePadding, bottom = 4.dp)
            )
            Text(
                text = stringResource(R.string.theme_system_description),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = basePadding, bottom = basePadding, end = basePadding)
            )
        }
        Spacer(modifier = Modifier
            .constrainAs(space1) {
                width = Dimension.percent(0.025f)
                start.linkTo(systemRef.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )
        val darkBgColorRes = getBgColorRes(Theme.DARK, selectedTheme)
        val darkBorderColorRes = getBorderColorRes(Theme.DARK, selectedTheme)
        Column(
            modifier = Modifier
                .background(color = colorResource(darkBgColorRes), shape = baseShape)
                .border(borderWidth, colorResource(darkBorderColorRes), baseShape)
                .clip(baseShape)
                .clickable { }
                .constrainAs(darkRef) {
                    height = Dimension.fillToConstraints
                    width = Dimension.percent(0.275f)
                    start.linkTo(space1.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            Text(
                text = stringResource(R.string.theme_dark),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(start = basePadding, top = basePadding, end = basePadding, bottom = 8.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_dark_mode),
                contentDescription = "Dark mode",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(start = basePadding, end = basePadding, bottom = basePadding)
            )
        }
        Spacer(modifier = Modifier
            .constrainAs(space2) {
                width = Dimension.percent(0.025f)
                start.linkTo(darkRef.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )
        val lightBgColorRes = getBgColorRes(Theme.LIGHT, selectedTheme)
        val lightBorderColorRes = getBorderColorRes(Theme.LIGHT, selectedTheme)
        Column(
            modifier = Modifier
                .background(color = colorResource(lightBgColorRes), shape = baseShape)
                .border(borderWidth, colorResource(lightBorderColorRes), baseShape)
                .clip(baseShape)
                .clickable { }
                .constrainAs(lightRef) {
                    height = Dimension.fillToConstraints
                    width = Dimension.percent(0.275f)
                    start.linkTo(space2.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
        ) {
            Text(
                text = stringResource(R.string.theme_light),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(start = basePadding, top = basePadding, end = basePadding, bottom = 8.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_light_mode),
                contentDescription = "Light mode",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(start = basePadding, end = basePadding, bottom = basePadding)
            )
        }
    }
}

private fun getBgColorRes(theme: Theme, selected: Theme): Int {
    return if (theme == selected) R.color.theme_card_bg_selected else R.color.theme_card_bg_unselected
}

private fun getBorderColorRes(theme: Theme, selected: Theme): Int {
    return if (theme == selected) R.color.theme_card_border else R.color.transparent
}

@Composable
@Preview
fun ThemeSettingBlockPreview() {
    ThemeSettingBlock(Theme.DARK, {})
}