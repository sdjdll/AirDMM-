plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "sdjini.AirDMM"
    compileSdk {
        version = release(36)
    }
    ndkVersion = "26.1.10909125"

    defaultConfig {
        ndk{
            abiFilters += listOf("arm64-v8a")
        }
        applicationId = "sdjini.AirDMM"
        minSdk = 29
        targetSdk = 36
        versionCode = 4
        versionName = "4.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += "-Oz -fvisibility=hidden -ffunction-sections -fdata-sections"
            }
        }
        multiDexEnabled = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isEmbedMicroApp = false
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            multiDexEnabled = false
            ndk{
                abiFilters += listOf("arm64-v8a")
            }
        }

        debug {
            ndk {
                abiFilters += listOf("arm64-v8a", "x86_64")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    packaging{
        resources{
            excludes += setOf("META-INF/**")
            excludes += setOf("DebugProbesKt.bin")
            excludes += setOf("kotlin/**")
            excludes += setOf("res/**")
        }
    }
}

dependencies {
    implementation(libs.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}