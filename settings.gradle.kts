import java.util.Properties
import java.io.FileInputStream

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

fun secret(name: String): String? {
    val fileProperties = File(rootProject.projectDir.absolutePath, "my.properties")
    val pr = runCatching { FileInputStream(fileProperties) }.getOrNull()?.let { file ->
        Properties().apply {
            load(file)
        }
    }
    return pr?.getProperty(name) ?: System.getenv(name)
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            setUrl("https://jitpack.io")
        }
        maven {
            secret("ARVIFOX_REPOSITORY_URL")?.let { setUrl(it) }
//            credentials {
//                username = readSecret("ARVIFOX_USERNAME")
//                password = readSecret("ARVIFOX_PASSWORD")
//            }
        }
    }
}
rootProject.name = "Remysaw"
include(":app")
