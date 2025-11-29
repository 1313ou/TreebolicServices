/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "org.treebolic.parcel"

    compileSdk = vCompileSdk

    defaultConfig {
        minSdk = vMinSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.treebolic.model)
    implementation(libs.treebolic.mutable)

    implementation(project(":treebolicGlue"))
    implementation(libs.annotation)

    implementation(libs.core.ktx)
    coreLibraryDesugaring(libs.desugar)

    androidTestImplementation(libs.test.junit)
    androidTestImplementation(libs.espresso.core)
}
