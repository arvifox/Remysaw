plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.android")
    //id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.arvifox.remysaw"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.arvifox.remysaw"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    configurations {
        all {
            exclude(module = "bcprov-jdk15on")
        }
    }
}

dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.fragment:fragment-ktx:1.6.0")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.ui:ui-graphics:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    implementation("androidx.compose.material3:material3:1.1.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.4.3")
    implementation("androidx.compose.foundation:foundation:1.4.3")
    implementation("androidx.compose.runtime:runtime:1.4.3")
    implementation("androidx.compose.animation:animation:1.4.3")
    implementation("androidx.compose.animation:animation-graphics:1.4.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    debugImplementation("androidx.compose.ui:ui-tooling:1.4.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.3")

    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation("androidx.lifecycle:lifecycle-process:2.6.1")
    implementation("androidx.lifecycle:lifecycle-common:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    implementation("jp.co.soramitsu.shared_features:core:1.1.1.6-SNAPSHOT")
    implementation("com.neovisionaries:nv-websocket-client:2.14")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.warchant:ed25519-sha3-java:2.0.1")
    implementation("com.madgag.spongycastle:core:1.58.0.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")
    implementation("androidx.navigation:navigation-compose:2.6.0")

    implementation("com.google.accompanist:accompanist-permissions:0.30.1")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.4.3")
}