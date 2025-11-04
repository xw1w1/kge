plugins {
    kotlin("jvm") version "2.2.20" apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

ext.set("lwjglVersion", "3.3.2")
ext.set("jomlVersion", "1.10.5")
ext.set("imguiVersion", "1.90.0")
ext.set("kgeEditor", "0.0.2")
