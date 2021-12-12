package ru.vitaliy.belyaev.wishapp.ui.screens.edittags.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.vitaliy.belyaev.model.database.Tag
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItem
import ru.vitaliy.belyaev.wishapp.ui.screens.edittags.entity.EditTagItemMode

@Composable
fun EditTagItemBlock(
    editTagItem: EditTagItem,
    onClick: (EditTagItem) -> Unit,
    onAddClick: (String) -> Unit,
    onRemoveClick: (Tag) -> Unit,
    onEditDoneClick: (Tag) -> Unit,
) {

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(editTagItem) }
    ) {
        val (startIconRef, titleRef) = createRefs()
        val verticalPadding = 8.dp

        IconButton(
            onClick = {
                if (editTagItem.tag != null && editTagItem.mode == EditTagItemMode.EDIT) {
                    onRemoveClick(editTagItem.tag)
                }
            },
            modifier = Modifier
                .constrainAs(startIconRef) {
                    top.linkTo(parent.top, margin = verticalPadding)
                    bottom.linkTo(parent.bottom, margin = verticalPadding)
                    start.linkTo(parent.start, margin = 16.dp)
                }
        ) {

            val icon: Painter = when (editTagItem.tag) {
                null -> {
                    when (editTagItem.mode) {
                        EditTagItemMode.DEFAULT -> {
                            painterResource(R.drawable.ic_add)
                        }
                        EditTagItemMode.EDIT -> {
                            painterResource(R.drawable.ic_close)
                        }
                    }
                }
                else -> {
                    when (editTagItem.mode) {
                        EditTagItemMode.DEFAULT -> {
                            painterResource(R.drawable.ic_label)
                        }
                        EditTagItemMode.EDIT -> {
                            painterResource(R.drawable.ic_delete)
                        }
                    }
                }
            }
            Icon(
                icon,
                tint = Color.Gray,
                contentDescription = "Tag",
            )
        }

        var text: String = remember { editTagItem.tag?.title ?: "" }

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(titleRef) {
                    width = Dimension.fillToConstraints
                    start.linkTo(startIconRef.end, margin = 16.dp)
                    top.linkTo(parent.top, margin = verticalPadding)
                    bottom.linkTo(parent.bottom, verticalPadding)
                    end.linkTo(parent.end, margin = 16.dp)
                },
            value = text,
            textStyle = MaterialTheme.typography.body1,
            onValueChange = { newValue -> text = newValue },
            placeholder = {
                Text(
                    text = stringResource(R.string.create_tag),
                    style = MaterialTheme.typography.body1,
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = colorResource(R.color.inputCursorColor)
            ),
            trailingIcon = {
                val shape = RoundedCornerShape(50.dp)
                when (editTagItem.tag) {
                    null -> {
                        when (editTagItem.mode) {
                            EditTagItemMode.DEFAULT -> {
                            }
                            EditTagItemMode.EDIT -> {
                                Icon(
                                    painterResource(R.drawable.ic_check),
                                    tint = Color.Gray,
                                    contentDescription = "Check",
                                    modifier = Modifier
                                        .clip(shape)
                                        .clickable { onAddClick(text) }
                                )
                            }
                        }
                    }
                    else -> {
                        when (editTagItem.mode) {
                            EditTagItemMode.DEFAULT -> {
                                Icon(
                                    painterResource(R.drawable.ic_edit),
                                    tint = Color.Gray,
                                    contentDescription = "Edit",
                                    modifier = Modifier
                                        .clip(shape)
                                        .clickable { onClick(editTagItem) }
                                )
                            }
                            EditTagItemMode.EDIT -> {
                                Icon(
                                    painterResource(R.drawable.ic_check),
                                    tint = Color.Gray,
                                    contentDescription = "Check",
                                    modifier = Modifier
                                        .clip(shape)
                                        .clickable { onEditDoneClick(editTagItem.tag.copy(title = text)) }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}
