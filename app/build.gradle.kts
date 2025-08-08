plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.madtitan.estimator"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.madtitan.estimator"
        minSdk = 26
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    /*composeOptions {
       // kotlinCompilerExtensionVersion = "1.5.1"
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }*/
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    configurations.all {
        resolutionStrategy.force("com.squareup:javapoet:1.13.0")
        resolutionStrategy.eachDependency {
            if (requested.group == "com.squareup" && requested.name == "javapoet") {
                useVersion("1.13.0")
            }
        }
    }
}

dependencies {
    //implementation(libs.androidx.material3.jvmstubs)
    val composeBom = platform("androidx.compose:compose-bom:2025.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    //implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    //debugImplementation(libs.androidx.ui.tooling)
   // implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(platform(libs.firebase.bom))
  //  implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
  //  implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth)
    //implementation(libs.androidx.compose.compiler)
    implementation(libs.hilt.android)
    //implementation(libs.javapoet)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.compiler.android)
    implementation(libs.hilt.navigation.compose)
    /* kapt(libs.hilt.compiler) {
        exclude(group = "com.squareup", module = "javapoet")
    }*/
    /*
    implementation(libs.firebase.crashlytics)
   */
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.firestore)
    implementation(libs.googleid)
    implementation(libs.gson)
   /* implementation(libs.androidx.compose.material3)              // material3
    implementation(libs.androidx.compose.material3.icons.extended)  // material3-icons-extended*/
    //implementation(libs.compose.material.dialogs.datetime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
   // debugImplementation(libs.androidx.ui.tooling)
   // debugImplementation(libs.androidx.ui.test.manifest)
}