#!/usr/bin/env kotlin

import kotlin.system.exitProcess
import java.io.File

// ARGUMENTS

if (args.size == 0) {
    println("Missing argument [folder]")
    exitProcess(1)
}
val serieFolder = File(args[0])


// DATA

data class EpisodeKey(val season: String, val episode: String, val seriesName: String): Comparable<EpisodeKey> {

    override fun compareTo(other: EpisodeKey): Int {
        return (season + episode).compareTo(other.season + other.episode)
    }
}

// FUNCTIONS

fun mapOfFiles(folder: File): Map<EpisodeKey, List<File>> {
    val fileRegex = """^([\w\.]+)[Ss](\d\d)[Ee](\d\d).*\.(\w\w\w)$""".toRegex()
    val filesByEpisode = hashMapOf<EpisodeKey, MutableList<File>>()

    folder.walk()
        .filter { it.isFile }
        .forEach { file ->
            val matchResult = fileRegex.find(file.name)
            matchResult?.let {
                val (seriesName, season, episode, _) = it.destructured
                 val episodeKey = EpisodeKey(season, episode, seriesName.formatName())
                 if (filesByEpisode[episodeKey] == null) {
                     filesByEpisode[episodeKey] = mutableListOf()
                 }
                filesByEpisode[episodeKey]!!.add(file)
            }
        }

    return filesByEpisode
}

fun runMkvMerge(episodeKey: EpisodeKey, files: List<File>) {
    println()
    println("EPISODE: ${episodeKey.season}${episodeKey.episode}")

    val outputFile = "${episodeKey.seriesName.fileFormat()}_s${episodeKey.season}e${episodeKey.episode}.mkv"
    val inputFilesToPrint = files.map { it.name } .joinToString(" ")
    println("mkvmerge -o ${outputFile} ${inputFilesToPrint}")

    val inputFiles = files.joinToString(" ")
    "mkvmerge -o ${outputFile} ${inputFiles}".runCommand(serieFolder)
}


// MAIN

val filesByEpisode = mapOfFiles(serieFolder).toSortedMap()
filesByEpisode.forEach { episodeKey, files ->
    runMkvMerge(episodeKey, files)
}


// EXTENSIONS

fun String.formatName(): String = replace(".", " ").trim().capitalizeWords()
fun String.fileFormat(): String = replace(" ", "_").toLowerCase()
fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")
fun String.runCommand(directory: File) {
    ProcessBuilder(*split(" ").toTypedArray())
            .directory(directory)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor()
}
