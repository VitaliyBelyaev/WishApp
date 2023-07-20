package ru.vitaliy.belyaev.wishapp.shared.utils

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

object SampleDataGenerator {

    fun createRuWishes(): List<WishEntity> {
        val currentMillis = Clock.System.nowEpochMillis()
        return listOf(
            WishEntity(
                id = "1",
                title = "Шуруповерт",
                link = "https://www.citilink.ru/product/drel-shurupovert-bosch-universaldrill-18v-akkum-patron-bystrozazhimnoi-1492081/?region_id=123062&gclid=CjwKCAiAm7OMBhAQEiwArvGi3Aom3wUbhHlBUu-9OPINzsyF9rM0Q2rBUgp1jFV68iT7IUaAoTA-1xoCzPcQAvD_BwE",
                links = listOf("https://www.citilink.ru/product/drel-shurupovert-bosch-universaldrill-18v-akkum-patron-bystrozazhimnoi-1492081/?region_id=123062&gclid=CjwKCAiAm7OMBhAQEiwArvGi3Aom3wUbhHlBUu-9OPINzsyF9rM0Q2rBUgp1jFV68iT7IUaAoTA-1xoCzPcQAvD_BwE"),
                comment = "С кейсом, чтобы были головки разные и запасной аккумулятор",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            WishEntity(
                id = "2",
                title = "Поход в SPA",
                link = "",
                links = listOf(),
                comment = "Побольше массажа",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            WishEntity(
                id = "3",
                title = "Робот пылесос",
                link = "https://www.citilink.ru/product/pylesos-robot-xiaomi-mi-mop-p-chernyi-1393766/?region_id=123062&gclid=CjwKCAiAm7OMBhAQEiwArvGi3DS-H1fiWV65XGxNEcrSzE1PpsULu34hK2eZ1C235ZV3OHton6qXMBoCzrQQAvD_BwE",
                links = listOf("https://www.citilink.ru/product/pylesos-robot-xiaomi-mi-mop-p-chernyi-1393766/?region_id=123062&gclid=CjwKCAiAm7OMBhAQEiwArvGi3DS-H1fiWV65XGxNEcrSzE1PpsULu34hK2eZ1C235ZV3OHton6qXMBoCzrQQAvD_BwE"),
                comment = "Чтобы с шерстью справлялся и умел по неровнастям ездить",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            WishEntity(
                id = "4",
                title = "Халат",
                link = "",
                links = listOf(),
                comment = "Цвет не яркий",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            WishEntity(
                id = "5",
                title = "Мультитул LEATHERMAN",
                link = "https://ileatherman.ru/multitul-leatherman-wave-plus-832524-s-nejlonovym-chexlom",
                links = listOf("https://ileatherman.ru/multitul-leatherman-wave-plus-832524-s-nejlonovym-chexlom"),
                comment = "",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            )
        )
    }

    fun createEnWishes(): List<WishEntity> {
        val currentMillis = Clock.System.nowEpochMillis()
        return listOf(
            WishEntity(
                id = "1",
                title = "Macbook Pro 14″",
                link = "https://www.apple.com/shop/buy-mac/macbook-pro/14-inch",
                links = listOf("https://www.apple.com/shop/buy-mac/macbook-pro/14-inch"),
                comment = "Space Gray with 32 Gb of RAM",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            WishEntity(
                id = "2",
                title = "Garmin Forerunner 955",
                link = "https://www.garmin.com/en-US/p/777655/pn/010-02638-11",
                links = listOf("https://www.garmin.com/en-US/p/777655/pn/010-02638-11"),
                comment = "White",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            WishEntity(
                id = "3",
                title = "Robotic Vacuum",
                link = "",
                links = listOf(),
                comment = "iRobot or Shark",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            WishEntity(
                id = "4",
                title = "Scarf",
                link = "",
                links = listOf(),
                comment = "Warm, neutral shades",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            ),
            WishEntity(
                id = "5",
                title = "LEATHERMAN Multitool",
                link = "https://www.amazon.com/LEATHERMAN-Multitool-Replaceable-Spring-Action-Stainless/dp/B0B2V4N34X/ref=sr_1_2?keywords=LEATHERMAN&qid=1673963133&sr=8-2",
                links = listOf("https://www.amazon.com/LEATHERMAN-Multitool-Replaceable-Spring-Action-Stainless/dp/B0B2V4N34X/ref=sr_1_2?keywords=LEATHERMAN&qid=1673963133&sr=8-2"),
                comment = "",
                isCompleted = false,
                createdTimestamp = currentMillis,
                updatedTimestamp = currentMillis,
                position = 0
            )
        )
    }

    fun createTags(): List<TagEntity> {
        val tagNames = listOf(
            "Power",
            "Internet",
            "Direction",
            "Series",
            "Manufacturer",
            "Drama",
            "Efficiency",
            "Maintenance",
            "Football",
            "Moment",
            "Memory",
            "Birthday",
            "Insurance"
        )
        val tags = mutableListOf<TagEntity>()

        tagNames.forEach {
            val tag = TagEntity(uuid4().toString(), it)
            tags.add(tag)
        }
        return tags
    }
}