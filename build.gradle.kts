plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-SNAPSHOT"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "diruptio"
version = "0.1.2"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
    compileOnly("net.lenni0451.classtransform:core:1.14.1")
    implementation(project(":api"))
    implementation("net.kyori:adventure-nbt:4.21.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }

    processResources {
        filesMatching("paper-plugin.yml") {
            expand(mapOf("version" to version))
        }
    }

    jar {
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        archiveFileName = "Sharp-${version}-unobf.jar"
    }

    test {
        useJUnitPlatform()
    }

    reobfJar {
        outputJar = file("build/libs/Sharp-${version}.jar")
    }

    assemble {
        dependsOn(reobfJar)
    }

    runServer {
        minecraftVersion("1.21.5")
        workingDir = file("run")
        systemProperty("sharp.debug", "true")
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}
