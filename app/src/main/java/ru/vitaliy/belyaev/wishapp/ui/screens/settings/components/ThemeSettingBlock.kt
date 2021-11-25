package ru.vitaliy.belyaev.wishapp.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.vitaliy.belyaev.wishapp.R

@Composable
fun ThemeSettingBlock() {
    Row(
        modifier = Modifier
            .height(82.dp)
            .padding(start = 16.dp, end = 16.dp)
    ) {

        val basePadding = 12.dp
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .background(color = Color.Cyan, shape = RoundedCornerShape(12.dp))
                .fillMaxSize()
                .clickable { }
                .weight(1.5f)
        ) {
            Text(
                text = "Cистемная",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = basePadding, top = basePadding, end = basePadding, bottom = 4.dp)
            )
            Text(
                text = "Такая же как на устройстве",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = basePadding, bottom = basePadding, end = basePadding)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(color = Color.Cyan, shape = RoundedCornerShape(12.dp))
                .fillMaxSize()
                .clickable { }
                .weight(1f)
        ) {
            Text(
                text = "Темная",
                modifier = Modifier
                    .padding(start = basePadding, top = basePadding, end = basePadding, bottom = 4.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_dark_mode),
                contentDescription = "Dark mode",
                modifier = Modifier
                    .padding(start = basePadding, end = basePadding, bottom = basePadding)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(color = Color.Cyan, shape = RoundedCornerShape(12.dp))
                .fillMaxSize()
                .clickable { }
                .weight(1f)
        ) {
            Text(
                text = "Светлая",
                modifier = Modifier
                    .padding(start = basePadding, top = basePadding, end = basePadding, bottom = 4.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_light_mode),
                contentDescription = "Light mode",
                modifier = Modifier
                    .padding(start = basePadding, end = basePadding, bottom = basePadding)
            )
        }
    }
}