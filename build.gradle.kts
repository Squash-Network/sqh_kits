plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("app.ultradev.hytalegradle") version "1.6.7"
}

group = "com.squashcompany.kits"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://www.cursemaven.com")
    }
}

dependencies {
    // HytaleServer.jar - provided at runtime by the server
    compileOnly(files("${property("hytaleInstallPath")}/Server/HytaleServer.jar"))

    // HyUI - UI Library for Hytale
    // Project ID: 1431415, File ID: 7493206 (v0.4.1 - latest)
    implementation("curse.maven:hyui-1431415:7493206")
}

hytale {
    // Enable operator privileges for testing
    allowOp.set(true)

    // Use release patchline (options: "release", "pre-release")
    patchline.set("release")
}

tasks.shadowJar {
    archiveClassifier.set("")
}
