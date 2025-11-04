plugins {
    id("java")
    kotlin("jvm")
}

group = "com.craftware"
version = "unspecified"

val lwjglVersion: String by rootProject.extra
val jomlVersion: String by rootProject.extra
val imguiVersion: String by rootProject.extra

dependencies {
    implementation(kotlin("stdlib"))

    api("org.joml:joml:$jomlVersion")

    api("org.lwjgl:lwjgl:$lwjglVersion")
    api("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    api("org.lwjgl:lwjgl-opengl:$lwjglVersion")

    api("org.lwjgl:lwjgl:${lwjglVersion}:natives-windows")
    api("org.lwjgl:lwjgl-glfw:${lwjglVersion}:natives-windows")
    api("org.lwjgl:lwjgl-opengl:${lwjglVersion}:natives-windows")

    api("io.github.spair:imgui-java-binding:$imguiVersion")
    api("io.github.spair:imgui-java-lwjgl3:$imguiVersion")
    api("io.github.spair:imgui-java-natives-windows:$imguiVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(24)
}