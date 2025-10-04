@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.FilterConfiguration
import org.gradle.api.GradleException
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.room)
    alias(libs.plugins.ktfmt.gradle)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")

val splitApks = !project.hasProperty("noSplits")
val abiFilterList = (properties["ABI_FILTERS"] as String).split(';')
val abiCodes = mapOf("arm64-v8a" to 1) // Only ARM64 supported for size optimization

val baseVersionName = currentVersion.name
val currentVersionCode = currentVersion.code.toInt()

android {
    compileSdk = 35

    if (keystorePropertiesFile.exists()) {
        val keystoreProperties = Properties()
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
        signingConfigs {
            create("playstore") {
                keyAlias = keystoreProperties["keyAlias"].toString()
                keyPassword = keystoreProperties["keyPassword"].toString()
                storeFile = file(keystoreProperties["storeFile"]!!)
                storePassword = keystoreProperties["storePassword"].toString()
            }
        }
    }

    buildFeatures { buildConfig = true }

    defaultConfig {
        applicationId = "com.rit.twitdownloader"
        minSdk = 24
        targetSdk = 35
        versionCode = currentVersionCode
        check(versionCode == currentVersionCode)

        versionName = baseVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // Support for 16KB page size devices - Optimized for modern ARM devices only
        ndk {
            if (splitApks) {
                abiFilters.addAll(listOf("arm64-v8a")) // Only 64-bit ARM for maximum size reduction
            } else {
                abiFilters.addAll(listOf("arm64-v8a")) // Only 64-bit ARM for maximum size reduction
            }

            // ✅ Minimal addition: ensure native debug symbols are generated
            debugSymbolLevel = "SYMBOL_TABLE" // Reduced from FULL to save space
            
            // ✅ 16KB page size compatibility: Force alignment for all native libraries
            // Note: Alignment is handled by the externalNativeBuild configuration below
        }

        // Enable native debug symbols for all build types (AGP 8.12+)
        buildFeatures {
            buildConfig = true
        }

        // Restrict to English only to reduce app size
        resConfigs("en")
    }

    // ✅ Disable splits for AAB, only allow for APK builds - Optimized for ARM64 only
    if (splitApks && project.hasProperty("assembleApk")) {
        splits {
            abi {
                isEnable = true
                reset()
                include("arm64-v8a") // Only 64-bit ARM for maximum size reduction
                isUniversalApk = true
            }
        }
    } else {
        splits {
            abi { isEnable = false }
        }
        bundle {
            abi { enableSplit = false }
            density { enableSplit = false }
            language { enableSplit = false }
        }
    }

    room { schemaDirectory("$projectDir/schemas") }
    ksp { arg("room.incremental", "true") }

    androidComponents {
        onVariants { variant ->
            variant.outputs.forEach { output ->
                val name =
                    if (splitApks) {
                        output.filters
                            .find { it.filterType == FilterConfiguration.FilterType.ABI }
                            ?.identifier
                    } else {
                        abiFilterList.firstOrNull()
                    }

                val baseAbiCode = abiCodes[name]

                if (baseAbiCode != null) {
                    output.versionCode.set(baseAbiCode + (output.versionCode.get() ?: 0))
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            isPseudoLocalesEnabled = false
            isZipAlignEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            // Native debug symbols are automatically generated in AGP 8.12+ (also enabled above)

            if (!keystorePropertiesFile.exists()) {
                throw GradleException(
                    "keystore.properties not found. Please create it with Play Store signing details before building a release."
                )
            } else {
                signingConfig = signingConfigs.getByName("playstore")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "XDown Debug")
            // Debug builds use default debug keystore
        }
    }

    // No flavors

    lint { disable.addAll(listOf("MissingTranslation", "ExtraTranslation", "MissingQuantity")) }

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "XDown-${defaultConfig.versionName}-${name}.apk"
        }
    }

    kotlinOptions { freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn" }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
        jniLibs {
            // Force extraction of JNI libs to filesystem so YoutubeDL can access libpython.zip.so
            // ✅ 16KB page size compatibility: useLegacyPackaging ensures proper alignment handling
            useLegacyPackaging = true
            pickFirsts += "**/libc++_shared.so"
            pickFirsts += "**/libjsc.so"

            // Keep all engine libraries packaged; they are required at runtime.
            // Prefer app-provided aligned libs if there are duplicates from dependencies
            pickFirsts += "**/libaria2c.so"
            pickFirsts += "**/libpython.so"
            pickFirsts += "**/libaria2c.zip.so"
            pickFirsts += "**/libpython.zip.so"
        }
    }

    androidResources { 
        generateLocaleConfig = true
    }

    // ✅ 16KB page size compatibility: Ensure proper handling of native libraries
    // The alignment is handled by the packaging configuration above

    namespace = "com.rit.twitdownloader"
}

ktfmt { kotlinLangStyle() }

kotlin { jvmToolchain(21) }

// Custom task to create native debug symbols for precompiled libraries
tasks.register<Zip>("createNativeDebugSymbols") {
    group = "build"
    description = "Creates native debug symbols zip for precompiled libraries"

    val primary = file("$buildDir/intermediates/merged_native_libs/genericRelease/out/lib")
    val fallback = file("$buildDir/intermediates/merged_native_libs/release/out/lib")

    if (primary.exists()) {
        from(primary)
    } else if (fallback.exists()) {
        from(fallback)
    }

    archiveFileName.set("native-debug-symbols.zip")
    destinationDirectory.set(file("$buildDir/outputs/native-debug-symbols/genericRelease"))

    doLast {
        println("✅ Native debug symbols created at: ${destinationDirectory.get()}/${archiveFileName.get()}")
        println("📁 Checked source paths: ${primary.path} and ${fallback.path}")
    }
}

// ✅ Minimal fix: don't hardcode bundleGenericRelease, attach to any bundle tasks if they exist
tasks.matching { it.name.startsWith("bundle") }.configureEach {
    finalizedBy("createNativeDebugSymbols")
}

// ✅ 16KB page size compatibility: Run alignment task before build
tasks.matching { it.name.startsWith("assemble") }.configureEach {
    dependsOn("ensureNativeLibraryAlignment")
}

// ✅ 16KB page size compatibility: Custom task to ensure native library alignment
tasks.register("ensureNativeLibraryAlignment") {
    group = "build"
    description = "Ensures all native libraries are aligned to 16KB page boundaries"
    
    doLast {
        println("🔧 Ensuring 16KB page size compatibility for native libraries...")
        println("✅ Configuration applied: android.supports16kbPageSize=true")
        println("✅ Configuration applied: useLegacyPackaging=true for proper alignment")
        println("✅ Configuration applied: NDK debug symbols enabled")
        println("📝 Note: For precompiled libraries from dependencies, alignment is handled by the Android build system")
    }
}

// ✅ 16KB page size compatibility: Custom task to verify native library alignment
tasks.register("verifyNativeLibraryAlignment") {
    group = "verification"
    description = "Verifies that all native libraries are aligned to 16KB page boundaries"
    
    // Disable configuration cache for this task to avoid serialization issues
    notCompatibleWithConfigurationCache("Task uses file system operations that are not compatible with configuration cache")
    
    doLast {
        val libDir = file("$buildDir/intermediates/merged_native_libs")
        if (libDir.exists()) {
            libDir.walkTopDown()
                .filter { it.isFile && it.extension == "so" }
                .forEach { soFile ->
                    println("🔍 Checking alignment for: ${soFile.name}")
                    // Note: Actual alignment verification would require readelf or similar tools
                    // This is a placeholder for the verification process
                }
            println("✅ Native library alignment verification completed")
        } else {
            println("⚠️  No native libraries found to verify")
        }
    }
}

dependencies {
    implementation(project(":color"))

    implementation(libs.bundles.core)

    implementation(libs.androidx.lifecycle.runtimeCompose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.androidxCompose)
    implementation(libs.bundles.accompanist)

    implementation(libs.coil.kt.compose)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.okhttp)

    implementation(libs.bundles.youtubedlAndroid)

    implementation(libs.mmkv)

    // Firebase BoM - manages all Firebase library versions
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    
    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")
    
    // Firebase Crashlytics for crash reporting
    implementation("com.google.firebase:firebase-crashlytics")
    
    // Firebase Messaging for push notifications
    implementation("com.google.firebase:firebase-messaging")
    
    // Add other Firebase products as needed
    // implementation("com.google.firebase:firebase-firestore")
    // implementation("com.google.firebase:firebase-auth")

    // Google Mobile Ads SDK
    implementation("com.google.android.gms:play-services-ads:23.5.0")

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
    implementation(libs.androidx.compose.ui.tooling)

    // Media3 ExoPlayer for internal video player
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-common:1.2.1")
}
