#!/usr/bin/env kotlin

import kotlin.system.exitProcess
import java.io.File


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

fun makeDestFolder(episodeKey: EpisodeKey, folder: File): File {
    val destFolderName = "${episodeKey.seriesName.fileFormat()}_${episodeKey.season}"
    val destFolder = File("${folder}/../${destFolderName}/")
    if (!destFolder.exists()) destFolder.mkdirs()

    return destFolder
}

fun runMkvMerge(folder: File, episodeKey: EpisodeKey, files: List<File>): Boolean {
    println()
    println("EPISODE: ${episodeKey.season}${episodeKey.episode}")

    val outputFile = "${episodeKey.seriesName.fileFormat()}_s${episodeKey.season}e${episodeKey.episode}.mkv"
    val inputFilesToPrint = files.map { it.name } .joinToString(" ")
    println("mkvmerge -o ${outputFile} ${inputFilesToPrint}")

    val inputFiles = files.joinToString(" ")
    return "mkvmerge -o ${outputFile} ${inputFiles}".runCommand(folder) == 0
}


// ARGUMENTS

if (args.size == 0) {
    println("Missing argument [folder]")
    exitProcess(1)
}
val originFolder = File(args[0])


// MAIN

val filesByEpisode = mapOfFiles(originFolder).toSortedMap()

var success = true
filesByEpisode.forEach { episodeKey, files ->
    if (success) {
        val destFolder = makeDestFolder(episodeKey, originFolder)
        success = runMkvMerge(destFolder, episodeKey, files)
    }
}

if (success) {
    originFolder.deleteRecursively()
}


// EXTENSIONS

fun String.formatName(): String = replace(".", " ").trim().capitalizeWords()
fun String.fileFormat(): String = replace(" ", "_").toLowerCase()
fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")
fun String.runCommand(directory: File): Int {
    val process = ProcessBuilder(*split(" ").toTypedArray())
        .directory(directory)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()

    return process.waitFor()
}
