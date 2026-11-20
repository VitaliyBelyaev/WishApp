package ru.vitaliy.belyaev.wishapp.ui.screens.about

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.utils.coroutines.DispatcherProvider

/** Библиотека и текст её лицензии. Текст часто просто ссылка на лицензию. */
data class OpenSourceLicense(val name: String, val text: String)

/**
 * Лицензии из файлов, которые собирает `oss-licenses-plugin`: в `third_party_license_metadata` строки
 * `смещение:длина название`, смещение и длина в байтах `third_party_licenses`. Туда же сборка дописывает
 * лицензии шрифтов (`app/build.gradle.kts`). В debug плагин кладёт заглушку `Debug License Info`.
 *
 * Свой экран вместо `OssLicensesMenuActivity`: тот в старой теме и падает, когда Android пересоздаёт его
 * из сохранённого Intent после обновления (`docs/BACKLOG.md`).
 */
@Singleton
class OpenSourceLicenses @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
) {

    private val mutex = Mutex()
    private var cached: List<OpenSourceLicense>? = null

    suspend fun all(): List<OpenSourceLicense> = mutex.withLock {
        cached ?: withContext(dispatchers.io()) { read() }.also { cached = it }
    }

    private fun read(): List<OpenSourceLicense> {
        val resources = context.resources
        val texts = resources.openRawResource(R.raw.third_party_licenses).use { it.readBytes() }
        val metadata = resources.openRawResource(R.raw.third_party_license_metadata).use { it.readBytes() }
        return parseLicenses(metadata.decodeToString(), texts)
    }
}

/** Разбор файлов плагина. Повторы по названию убираем, список по алфавиту без учёта регистра. */
internal fun parseLicenses(metadata: String, texts: ByteArray): List<OpenSourceLicense> = metadata.lineSequence()
    .mapNotNull { line -> parseLine(line, texts) }
    .distinctBy { it.name.lowercase() }
    .sortedBy { it.name.lowercase() }
    .toList()

private fun parseLine(line: String, texts: ByteArray): OpenSourceLicense? {
    val match = MetadataLine.matchEntire(line.trim()) ?: return null
    val (offsetText, lengthText, name) = match.destructured
    val offset = offsetText.toInt()
    val end = offset + lengthText.toInt()
    return if (end <=
        texts.size
    ) {
        OpenSourceLicense(name = name.trim(), text = texts.decodeToString(offset, end).trim())
    } else {
        null
    }
}

/** `смещение:длина название`, смещение и длина в байтах. */
private val MetadataLine = Regex("""(\d{1,9}):(\d{1,9}) (.+)""")
