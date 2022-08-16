package net.kilogram.messenger.utils

import kotlinx.coroutines.runBlocking
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import net.kilogram.messenger.utils.FileUtil.delete
import net.kilogram.messenger.utils.FileUtil.initDir
import java.io.File

object LocaleUtil {

    @JvmField
    val cacheDir = File(ApplicationLoader.applicationContext.cacheDir, "builtIn_lang_export")

    @JvmStatic
    fun fetchAndExportLang() = runBlocking {

        delete(cacheDir)
        initDir(cacheDir)

        for (localeInfo in LocaleController.getInstance().languages) {

            if (!localeInfo.builtIn || localeInfo.pathToFile != "unofficial") continue

            if (localeInfo.hasBaseLang()) {

                localeInfo.pathToBaseFile.takeIf { it.isFile }?.copyTo(File(cacheDir, localeInfo.pathToBaseFile.name))

            }

            localeInfo.getPathToFile()?.takeIf { it.isFile }?.copyTo(File(cacheDir, localeInfo.getPathToFile().name))

        }


    }

}