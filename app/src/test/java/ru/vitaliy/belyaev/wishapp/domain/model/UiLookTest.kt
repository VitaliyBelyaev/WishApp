package ru.vitaliy.belyaev.wishapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UiLookTest {

    @Test
    fun remoteValueParsing() {
        assertEquals(UiLookMode.OLD_ONLY, UiLookMode.fromRemoteValue("old_only"))
        assertEquals(UiLookMode.CHOICE, UiLookMode.fromRemoteValue(" Choice "))
        assertEquals(UiLookMode.NEW_ONLY, UiLookMode.fromRemoteValue("new_only"))
        // Пустое значение до первой загрузки и опечатки не открывают новый вид
        assertEquals(UiLookMode.OLD_ONLY, UiLookMode.fromRemoteValue(""))
        assertEquals(UiLookMode.OLD_ONLY, UiLookMode.fromRemoteValue(null))
        assertEquals(UiLookMode.OLD_ONLY, UiLookMode.fromRemoteValue("new"))
    }

    @Test
    fun storedChoiceRoundTrip() {
        UiLook.entries.forEach { assertEquals(it, UiLook.fromStoredValue(it.storedValue)) }
        assertEquals(null, UiLook.fromStoredValue(null))
        assertEquals(null, UiLook.fromStoredValue("unknown"))
    }

    @Test
    fun releaseBeforeNewLookIsReadyAlwaysOld() {
        UiLookMode.entries.forEach { mode ->
            listOf(null, UiLook.OLD, UiLook.NEW).forEach { choice ->
                assertEquals(UiLook.OLD, resolve(mode, choice, debug = false, released = false))
            }
        }
    }

    @Test
    fun releasedModes() {
        assertEquals(UiLook.OLD, resolve(UiLookMode.OLD_ONLY, UiLook.NEW, debug = false, released = true))

        assertEquals(UiLook.NEW, resolve(UiLookMode.CHOICE, null, debug = false, released = true))
        assertEquals(UiLook.OLD, resolve(UiLookMode.CHOICE, UiLook.OLD, debug = false, released = true))

        // Выбор не стирается: при new_only он не действует, а после отката на choice снова работает
        assertEquals(UiLook.NEW, resolve(UiLookMode.NEW_ONLY, UiLook.OLD, debug = false, released = true))
    }

    @Test
    fun debugCanOpenNewLookDuringDevelopment() {
        assertEquals(UiLook.OLD, resolve(UiLookMode.OLD_ONLY, null, debug = true, released = false))
        assertEquals(UiLook.NEW, resolve(UiLookMode.OLD_ONLY, UiLook.NEW, debug = true, released = false))
        assertEquals(UiLook.NEW, resolve(UiLookMode.OLD_ONLY, UiLook.NEW, debug = true, released = true))
        assertEquals(UiLook.OLD, resolve(UiLookMode.NEW_ONLY, UiLook.OLD, debug = true, released = false))
    }

    private fun resolve(mode: UiLookMode, choice: UiLook?, debug: Boolean, released: Boolean): UiLook {
        return resolveUiLook(
            mode = mode,
            userChoice = choice,
            allowChoiceInOldOnly = debug,
            isNewLookReleased = released,
        )
    }
}
