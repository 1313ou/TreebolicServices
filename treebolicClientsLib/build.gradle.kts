/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk
        by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk
        by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "org.treebolic.clients"

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

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.treebolic.model)

    implementation(project(":treebolicAidl")) // needed
    implementation(project(":treebolicServicesIface"))
    implementation(project(":treebolicClientsIface"))
    implementation(project(":treebolicIface"))
    implementation(project(":treebolicParcel"))
    implementation(project(":commonLib"))

    implementation(libs.appcompat)
    implementation(libs.annotation)

    implementation(libs.core.ktx)
    coreLibraryDesugaring(libs.desugar)
}
