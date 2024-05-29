plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("maven-publish")
}

buildscript {
    dependencies {
        classpath("io.realm:realm-gradle-plugin:10.16.0")
    }
}

apply(plugin = "realm-android")

android {
    namespace = "org.matrix.android.sdk"

    compileSdk = 34

    defaultConfig {
        minSdk = 24

        // Multidex is useful for tests
        multiDexEnabled = true

        defaultConfig {
            consumerProguardFiles("proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            // Set to true to log privacy or sensible data, such as token
            buildConfigField("boolean", "LOG_PRIVATE_DATA", "true")
            // Set to BODY instead of NONE to enable logging
            buildConfigField("okhttp3.logging.HttpLoggingInterceptor.Level", "OKHTTP_LOGGING_LEVEL", "okhttp3.logging.HttpLoggingInterceptor.Level." + project.property("vector.httpLogLevel"))
        }

        release {
            buildConfigField("boolean", "LOG_PRIVATE_DATA", "false")
            buildConfigField("okhttp3.logging.HttpLoggingInterceptor.Level", "OKHTTP_LOGGING_LEVEL", "okhttp3.logging.HttpLoggingInterceptor.Level.BASIC")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs += listOf(
                // Disabled for now, there are too many errors. Could be handled in another dedicated PR
                // '-Xexplicit-api=strict', // or warning
                "-opt-in=kotlin.RequiresOptIn",
                // Opt in for kotlinx.coroutines.FlowPreview
                "-opt-in=kotlinx.coroutines.FlowPreview",
        )
    }

    sourceSets {
        named("androidTest") {
            java.srcDir("src/sharedTest/java")
        }
        named("test") {
            java.srcDir("src/sharedTest/java")
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    packaging {
        resources.excludes.addAll(
                listOf(
                        "META-INF/LICENSE.md",
                        "META-INF/LICENSE-notice.md"
                )
        )
    }
}

dependencies {
    val coroutines = "1.8.0"
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines")

    //   implementation libs.androidx.appCompat
    api("androidx.core:core-ktx:1.13.1")

    // Lifecycle
    val lifecycle = "2.8.0"
    api("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle")
    api("androidx.lifecycle:lifecycle-common:$lifecycle")
    api("androidx.lifecycle:lifecycle-process:$lifecycle")

    // Network
    val retrofit = "2.11.0"
    api("com.squareup.retrofit2:retrofit:$retrofit")
    api("com.squareup.retrofit2:converter-moshi:$retrofit")

    // When version of okhttp is updated (current is 4.9.3), consider removing the workaround
    // to force usage of Protocol.HTTP_1_1. Check the status of:
    // - https://github.com/square/okhttp/issues/3278
    // - https://github.com/square/okhttp/issues/4455
    // - https://github.com/square/okhttp/issues/3146
    //Do not use bom for publishing
    val okhttp_version = "4.12.0"
    api("com.squareup.okhttp3:okhttp:$okhttp_version")
    api("com.squareup.okhttp3:logging-interceptor:$okhttp_version")

    val moshi = "1.15.1"
    api("com.squareup.moshi:moshi:$moshi")
    api("com.squareup.moshi:moshi-adapters:$moshi")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshi")

    //noinspection GradleDependency
    api("com.atlassian.commonmark:commonmark:0.13.0")

    // Image
    api("androidx.exifinterface:exifinterface:1.3.7")

    // Database
    api("com.github.Zhuinden:realm-monarchy:0.7.1")

    kapt("dk.ilios:realmfieldnameshelper:2.0.0")

    // Shared Preferences
    api("androidx.preference:preference-ktx:1.2.1")

    // Work
    api("androidx.work:work-runtime-ktx:2.9.0")

    // olm lib is now hosted in MavenCentral
    api("org.matrix.android:olm-sdk:3.2.12")

    // DI
    val dagger = "2.51.1"
    api("com.google.dagger:dagger:$dagger")
    kapt("com.google.dagger:dagger-compiler:$dagger")

    // Logging
    api("com.jakewharton.timber:timber:5.0.1")

    // Video compression
    api("com.otaliastudios:transcoder:0.10.5")

    // Exif data handling
    api("org.apache.commons:commons-imaging:1.0-alpha3")

    api("com.googlecode.libphonenumber:libphonenumber:8.13.36")

    // ----- Jitpack release
    api("org.futo.gitlab.circles:circles-rust-components-kotlin:v0.3.15.10@aar") {
        isTransitive = true
    }
    // ----- Maven local testing
    //api "org.futo.gitlab.circles:crypto-android:0.1"
    // ------

    testImplementation("junit:junit:4.13.2")
    // Note: version sticks to 1.9.2 due to https://github.com/mockk/mockk/issues/281
    val mockk = "1.13.10"
    testImplementation("io.mockk:mockk:$mockk")
    testImplementation("org.amshove.kluent:kluent-android:1.73")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    // Plant Timber tree for test
    testImplementation("net.lachlanmckee:timber-junit-rule:1.0.1")
    // Transitively required for mocking realm as monarchy doesn"t expose Rx
    testImplementation("io.reactivex.rxjava2:rxkotlin:2.4.0")

    kaptAndroidTest("com.google.dagger:dagger-compiler:$dagger")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("org.amshove.kluent:kluent-android:1.73")
    androidTestImplementation("io.mockk:mockk-android:$mockk")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines")

    // Plant Timber tree for test
    androidTestImplementation("net.lachlanmckee:timber-junit-rule:1.0.1")

    androidTestUtil("androidx.test:orchestrator:1.4.2")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "org.futo.gitlab.circles"
            artifactId = "matrix-android-sdk"
            version = "0.1.97"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
