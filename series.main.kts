#!/usr/bin/env kotlin

import kotlin.system.exitProcess
import java.io.File

// Check arguments
if (args.size == 0) {
    println("Missing argument [folder]")
    exitProcess(1)
}
val dir = args[0]

val fileRegex = """^([\w\.]+)[Ss](\d\d)[Ee](\d\d).*\.(\w\w\w)$""".toRegex()
val files = File(dir).listFiles { file -> file.extension == "mkv" || file.extension == "mp4" }
val episodes = files.mapNotNull { file -> 
    val fileName = file.name
    println(fileName) 
    val matchResult = fileRegex.find(fileName)
    matchResult?.let {
        val (serieName, season, episode, extension) = it.destructured
        Episode(serieName.replace(".", " ").trim().capitalizeWords(),
                    season, episode, extension)
    }
}

episodes.forEach {
    println(it)
}

data class Episode(val serieName: String, 
                    val season: String, 
                    val episode: String, 
                    val extension: String)

fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")
