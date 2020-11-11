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
            Episode(serieName.formatName(), season, episode, extension, file)
        }
    }

    return episodes
}

val episodes = findEpisodes(folder)


// SUBTITLES

data class Subtitle(val serieName: String,
                    val season: String,
                    val episode: String,
                    val files: List<File>)

fun findSubtitles(folder: String): List<Subtitle> {
    val subtitlesMap = hashMapOf<String, MutableList<File>>()
    val subFolder = folder + "/Subs"
    val files = File(subFolder).listFiles { file -> file.extension == "idx" || file.extension == "sub" }

    val fileRegex = """^([\w\.]+)[Ss](\d\d)[Ee](\d\d).*\.(\w\w\w)$""".toRegex()

    files.forEach { file -> 
        val fileName = file.name
        val matchResult = fileRegex.find(fileName)
        matchResult?.let {
            val (_, season, episode, _) = it.destructured
            val episodeKey = season + episode
            if (subtitlesMap[episodeKey] == null) {
                subtitlesMap[episodeKey] = mutableListOf()
            }
            subtitlesMap[episodeKey]!!.add(file)
        }
    }

    val subtitles = mutableListOf<Subtitle>()

    subtitlesMap.values.forEach { 
        val matchResult = fileRegex.find(files[0].name)
        if (matchResult != null) {
            val (serieName, season, episode, _) = matchResult.destructured
            val subtitle = Subtitle(serieName, season, episode, it)
            subtitles.add(subtitle)
        }
    }

    return subtitles
}

val subtitles = findSubtitles(folder)
subtitles.forEach { println(it) }


// HELPERS

fun String.formatName(): String = replace(".", " ").trim().capitalizeWords()
fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")
