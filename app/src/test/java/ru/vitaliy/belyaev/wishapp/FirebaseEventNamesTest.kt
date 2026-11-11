package ru.vitaliy.belyaev.wishapp

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.FirebaseEventNames

class FirebaseEventNamesTest {

    private val firebaseNameRule = Regex("^[a-zA-Z][a-zA-Z0-9_]{0,39}$")

    @Test
    fun convertsScreenEventNames() {
        assertEquals("settings_show", FirebaseEventNames.toFirebaseName("Settings Screen - Show"))
        assertEquals("list_wish_moved", FirebaseEventNames.toFirebaseName("Wish List Screen - Wish Moved"))
        assertEquals(
            "wish_change_wish_completeness_clicked",
            FirebaseEventNames.toFirebaseName("Wish Detailed Screen - Change Wish Completeness Clicked"),
        )
        assertEquals("inapp_review_show", FirebaseEventNames.toFirebaseName("InApp Review Show"))
    }

    @Test
    fun truncatesTooLongNames() {
        val result = FirebaseEventNames.toFirebaseName("Some Very Long Event Name That Does Not Fit Into Forty Chars")
        assertTrue(result, firebaseNameRule.matches(result))
    }

    /** Все имена событий из кода: валидны для Firebase и не склеиваются после сокращения. */
    @Test
    fun allAppEventNamesAreValidAndUnique() {
        val eventsDir = File("src/main/java/ru/vitaliy/belyaev/wishapp/domain/model/analytics")
        assertTrue("Не найдена папка событий: ${eventsDir.absolutePath}", eventsDir.isDirectory)

        val nameDeclaration = Regex("""override val name: String = "([^"]+)"""")
        val names = eventsDir.walk()
            .filter { it.extension == "kt" }
            .flatMap { file -> nameDeclaration.findAll(file.readText()).map { it.groupValues[1] } }
            .toList()
        assertTrue("Событий не нашлось", names.size > 40)

        val converted = names.associateWith(FirebaseEventNames::toFirebaseName)
        converted.forEach { (original, firebaseName) ->
            assertTrue("$original -> $firebaseName", firebaseNameRule.matches(firebaseName))
        }
        val duplicates = converted.values.groupingBy { it }.eachCount().filterValues { it > 1 }
        assertTrue("Совпали имена: $duplicates", duplicates.isEmpty())
    }
}
