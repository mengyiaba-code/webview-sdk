plugins {
    id("com.android.library")
    `maven-publish`
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
            // You must register it as a MavenPublication
            register<MavenPublication>("release") {
                from(components["release"]) 
                
                groupId = "com.github.mengyiaba"
                artifactId = "webview-sdk"
                version = "1.0.3"
            }
        }
    }
}
