#!/usr/bin/env kotlin

import java.io.File

val folders = File("..").listFiles { file -> file.isDirectory() }
folders?.forEach { folder -> println(folder) }