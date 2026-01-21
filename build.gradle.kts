plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("app.ultradev.hytalegradle") version "1.6.7"
}

group = "SquashCompany"
version = "1.0.2"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // HytaleServer.jar - provided at runtime by the server
    compileOnly(files("${property("hytaleInstallPath")}/Server/HytaleServer.jar"))
}

hytale {
    // Enable operator privileges for testing
    allowOp.set(true)

    // Use release patchline (options: "release", "pre-release")
    patchline.set("release")
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set("sqh_kits")
}

tasks.jar {
    archiveBaseName.set("sqh_kits")
}
