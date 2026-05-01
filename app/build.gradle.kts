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
        versionName = "4.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}