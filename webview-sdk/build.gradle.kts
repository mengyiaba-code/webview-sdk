plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    // This acts as your SDK's unique identifier
    namespace = "com.mengyiaba.webviewsdk"
    compileSdk = 34

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

afterEvaluate {
    publishing {
        publications {
            create("release") {
                from(components["release"])
                // Change these to your actual GitHub username
                groupId = "com.github.mengyiaba"
                artifactId = "webview-sdk"
                version = "1.0.1"
            }
        }
    }
}
