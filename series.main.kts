#!/usr/bin/env kotlin

import kotlin.system.exitProcess
import java.io.File

// ARGUMENTS

if (args.size == 0) {
    println("Missing argument [folder]")
    exitProcess(1)
}

val folder = args[0]

// EPISODES

data class Episode(val serieName: String, 
                    val season: String, 
                    val episode: String, 
                    val extension: String,
                    val file: File)

fun findEpisodes(folder: String): List<Episode> {
    val files = File(folder).listFiles { file -> file.extension == "mkv" || file.extension == "mp4" }

    val fileRegex = """^([\w\.]+)[Ss](\d\d)[Ee](\d\d).*\.(\w\w\w)$""".toRegex()

    val episodes = files.mapNotNull { file -> 
        val fileName = file.name
        val matchResult = fileRegex.find(fileName)
        matchResult?.let {
            val (serieName, season, episode, extension) = it.destructured
            Episode(serieName.replace(".", " ").trim().capitalizeWords(),
                        season, episode, extension, file)
        }
    }

    return episodes
}

val episodes = findEpisodes(folder)

// SUBTITLES

data class Subtitle(val serieName: String,
                    val season: String,
                    val episode: String,
                    val format: String,
                    val files: List<File>)

fun findSubtitles(folder: String): List<Subtitle> {
    val subFolder = folder + "/Subs"
    val files = File(subFolder).listFiles { file -> file.extension == "idx" || file.extension == "sub" }

    val fileRegex = """^([\w\.]+)[Ss](\d\d)[Ee](\d\d).*\.(\w\w\w)$""".toRegex()

    val subtitles = files.mapNotNull { file -> 
        val fileName = file.name
        val matchResult = fileRegex.find(fileName)
        matchResult?.let {
            val (serieName, season, episode, _) = it.destructured
            Subtitle(serieName.replace(".", " ").trim().capitalizeWords(),
                        season, episode, "idx", listOf(file))
        }
    }
    return subtitles
}

val subtitles = findSubtitles(folder)
subtitles.forEach { println(it) }

// HELPERS

fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")
