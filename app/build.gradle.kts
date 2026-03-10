plugins {
    alias(libs.plugins.android.application)
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("D:\\Users\\sdjini\\Desktop\\杂项\\sdjini.jks")
            storePassword = "Rrhar\'il"
            keyAlias = "sdjini"
            keyPassword = "Rrhar\'il"
        }
    }
    namespace = "sdjini.AirDMM"
    compileSdk {
        version = release(36)
    }
    ndkVersion = "26.1.10909125"

    defaultConfig {
        applicationId = "sdjini.AirDMM"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "3.1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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