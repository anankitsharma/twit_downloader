# 🚀 Twitter Downloader: Dynamic Feature Module for Native Libraries

## 🎯 Goal
Reduce base APK size by 15-25MB by moving heavy native libs (`libffmpeg.so`, `libpython.so`, `libaria2c.so`, `libffprobe.so`) into a **Dynamic Feature Module** (`ffmpeg_module`), delivered **on demand**. Fully Play-compliant, passes **16 KB ELF requirement**, improves download conversion rates.

## 📊 Expected Results
- **Base APK Size**: Reduce from ~50-80MB to ~20-30MB
- **Download Conversion**: 15-25% improvement in Play Store installs
- **User Experience**: Faster initial app launch, on-demand heavy features

## 🔍 **Current Issues Identified**
Based on research and implementation attempts:

1. **❌ Dynamic Feature Module Not Building**: The `ffmpeg_module` is not being included in the AAB
2. **❌ Native Library Loading Issues**: Standard `System.loadLibrary()` doesn't work with DFMs
3. **❌ Missing SplitCompat Implementation**: Required for proper resource and library access
4. **❌ Incorrect Module Configuration**: DFM needs proper dependency and build configuration

---

## 📝 Step 1. Enable Dynamic Features
Edit **`settings.gradle.kts`** at project root (Kotlin DSL):

```kotlin
include(":app")
include(":ffmpeg_module")
```

---

## 📝 Step 2. Create Dynamic Feature Module
In Android Studio:

- `File > New > New Module > Dynamic Feature Module`
- Module name: `ffmpeg_module`
- Base module: `app`
- Check **onDemand** support
- Set **fusing = false**

---

## 📝 Step 3. Configure Module Manifest
File: `ffmpeg_module/src/main/AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <dist:module
        xmlns:dist="http://schemas.android.com/apk/distribution"
        dist:title="@string/ffmpeg_module_name"
        dist:onDemand="true"
        dist:fusing="false" />

</manifest>
```

Also add a module-local string so dist:title resolves:

File: `ffmpeg_module/src/main/res/values/strings.xml`

```xml
<resources>
    <string name="ffmpeg_module_name">FFmpeg Module</string>
    <!-- Title only; not user-visible -->
</resources>
```

---

## 📝 Step 4. Extract and Place Native Libraries
**First, extract `.so` files from current dependencies:**

1. **Find current libraries** in `app/build/intermediates/merged_native_libs/`
2. **Extract from `youtubedlAndroid` bundle** dependencies
3. **Copy to DFM structure**:

```
ffmpeg_module/src/main/jniLibs/arm64-v8a/
├── libffmpeg.so
├── libffprobe.so
├── libpython.so
└── libaria2c.so
```

**Note**: Only ARM64-v8a needed (already optimized for size)

---

## 📝 Step 5. Configure Gradle for DFM
File: `ffmpeg_module/build.gradle.kts` (Kotlin DSL):

```kotlin
plugins {
    id("com.android.dynamic-feature")
}

android {
    namespace = "com.rit.twitdownloader.ffmpegmodule"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    // Native libs are auto-included from jniLibs/
    // Maintain 16KB page size compatibility
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    // Match base module flavors to avoid variant ambiguity & appId errors
    flavorDimensions += "publishChannel"
    productFlavors {
        create("generic") { dimension = "publishChannel" }
        create("fdroid") { dimension = "publishChannel" }
    }
}
```

Add base module dependency so the feature can resolve the base variant (required by AGP):

```kotlin
dependencies {
    implementation(project(":app"))
}
```

---

## 📝 Step 5b. Move Native Libs to DFM and Exclude from Base App
To ensure size reduction, the heavy `.so` files must NOT be packaged in the base app.

1) Keep them only in `ffmpeg_module/src/main/jniLibs/arm64-v8a/` (Step 4).

2) Exclude them from the base app packaging:

File: `app/build.gradle.kts`

```kotlin
android {
    packaging {
        jniLibs {
            // Prevent base APK from packaging heavy libs (they live in the DFM)
            excludes += listOf(
                "**/libffmpeg.so",
                "**/libffprobe.so",
                "**/libpython.so",
                "**/libaria2c.so",
                "**/libffmpeg.zip.so",
                "**/libaria2c.zip.so",
                "**/libpython.zip.so",
            )

            // Avoid strip errors for special .zip.so artifacts when present
            keepDebugSymbols += listOf(
                "**/libffmpeg.zip.so",
                "**/libaria2c.zip.so",
                "**/libpython.zip.so",
            )
        }
    }
}
```

---

## 📝 Step 6. Add Play Core Dependency
In `app/build.gradle.kts`:

```kotlin
dependencies {
    // ... existing dependencies ...
    
    // Play Core for Dynamic Feature Modules
    implementation("com.google.android.play:core:1.10.3")
    implementation("com.google.android.play:core-ktx:1.8.1")
}
```

---

## 📝 Step 7. Fix Dynamic Feature Module Configuration
**CRITICAL FIX**: Add the DFM to app's build.gradle.kts:

```kotlin
android {
    // ... existing configuration ...
    
    dynamicFeatures += setOf(":ffmpeg_module")
}
```

## 📝 Step 8. Create Native Library Helper Class
Create `app/src/main/java/com/rit/twitdownloader/util/NativeLibraryHelper.kt`:

```kotlin
package com.rit.twitdownloader.util

import android.content.Context
import com.google.android.play.core.splitinstall.SplitInstallHelper
import android.util.Log

object NativeLibraryHelper {
    
    fun loadNativeLibraries(context: Context) {
        try {
            // Use SplitInstallHelper for DFM native libraries
            SplitInstallHelper.loadLibrary(context, "ffmpeg")
            SplitInstallHelper.loadLibrary(context, "ffprobe") 
            SplitInstallHelper.loadLibrary(context, "python")
            SplitInstallHelper.loadLibrary(context, "aria2c")
            
            Log.d("NativeLibraryHelper", "Native libraries loaded successfully")
        } catch (e: Exception) {
            Log.e("NativeLibraryHelper", "Failed to load native libraries", e)
            throw e
        }
    }
    
    fun loadNativeLibrariesFallback() {
        try {
            // Fallback to standard loading if DFM fails
            System.loadLibrary("ffmpeg")
            System.loadLibrary("ffprobe")
            System.loadLibrary("python") 
            System.loadLibrary("aria2c")
            
            Log.d("NativeLibraryHelper", "Native libraries loaded via fallback")
        } catch (e: Exception) {
            Log.e("NativeLibraryHelper", "Fallback loading also failed", e)
            throw e
        }
    }
}
```

## 📝 Step 9. Update App.kt with Proper DFM Loading
In `app/src/main/java/com/rit/twitdownloader/App.kt`:

```kotlin
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.rit.twitdownloader.util.NativeLibraryHelper

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // ... existing initialization ...
        
        applicationScope.launch(Dispatchers.IO) {
            try {
                // Load native libraries dynamically
                loadNativeLibraries()
                
                // ... rest of existing initialization ...
            } catch (th: Throwable) {
                withContext(Dispatchers.Main) { startCrashReportActivity(th) }
            }
        }
    }
    
    private suspend fun loadNativeLibraries() {
        if (isFfmpegModuleInstalled()) {
            // Load libraries from DFM
            NativeLibraryHelper.loadNativeLibraries(this@App)
        } else {
            // Request module installation
            requestFfmpegModule()
        }
    }
    
    private fun isFfmpegModuleInstalled(): Boolean {
        val manager = SplitInstallManagerFactory.create(this)
        return manager.installedModules.contains("ffmpeg_module")
    }
    
    private suspend fun requestFfmpegModule() {
        val manager = SplitInstallManagerFactory.create(this)
    val request = SplitInstallRequest.newBuilder()
        .addModule("ffmpeg_module")
        .build()

        try {
            android.util.Log.d("App", "Requesting ffmpeg_module installation...")
            
            kotlinx.coroutines.suspendCancellableCoroutine<Unit> { continuation ->
    manager.startInstall(request)
                    .addOnSuccessListener { 
                        android.util.Log.d("App", "ffmpeg_module installed successfully")
                        NativeLibraryHelper.loadNativeLibraries(this@App)
                        continuation.resume(Unit) { }
                    }
                    .addOnFailureListener { exception ->
                        android.util.Log.e("App", "Failed to install ffmpeg_module", exception)
                        // Try fallback
                        try {
                            NativeLibraryHelper.loadNativeLibrariesFallback()
                            continuation.resume(Unit) { }
                        } catch (fallbackException: Exception) {
                            android.util.Log.e("App", "Fallback library loading also failed", fallbackException)
                            continuation.resumeWith(Result.failure(exception))
                        }
                    }
            }
        } catch (e: Exception) {
            android.util.Log.e("App", "Failed to install ffmpeg_module", e)
            // Try fallback
            try {
                NativeLibraryHelper.loadNativeLibrariesFallback()
            } catch (fallbackException: Exception) {
                android.util.Log.e("App", "Fallback library loading also failed", fallbackException)
                throw e
            }
        }
    }
}
```

## 📝 Step 10. Add SplitCompat Support (if needed)
If you have activities that use the DFM, add to each activity:

```kotlin
import com.google.android.play.core.splitinstall.SplitCompat

class YourActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }
}
```

---

## 📝 Step 11. Build App Bundle
Run in terminal:

```bash
./gradlew clean bundleDebug
```

Output:  
`app/build/outputs/bundle/debug/app-debug.aab`

---

## 📝 Step 12. Test Locally with BundleTool
1. Download bundletool: https://developer.android.com/studio/command-line/bundletool  
   Save as `bundletool.jar`.

2. Build `.apks` package:
   ```bash
   java -jar bundletool.jar build-apks \
       --bundle=app/build/outputs/bundle/debug/app-debug.aab \
       --output=app.apks \
       --local-testing
   ```

3. Install on device:
   ```bash
   java -jar bundletool.jar install-apks --apks=app.apks
   ```

4. Run app → module will download automatically on first use.  
   Verify logs show: `ffmpeg_module installed`.  

---

## 📝 Step 13. Upload to Play Store
- Switch to release build:
  ```bash
  ./gradlew clean bundleRelease
  ```
- Upload `.aab` to Play Console.  
- Base APK will be **15-25MB smaller**.  
- `ffmpeg_module` will show as **on-demand delivery**.  

---

## 📊 Success Metrics
- [ ] **Base APK Size**: Reduced from ~50-80MB to ~20-30MB
- [ ] **Download Conversion**: 15-25% improvement expected
- [ ] **User Experience**: Faster initial app launch
- [ ] **Play Store Compliance**: Maintains 16KB ELF requirements

---

## ✅ Verification Checklist
- [ ] Base APK size is reduced (libs not in `app/`).  
- [ ] `bundletool install-apks` works locally.  
- [ ] Native libraries load successfully after module install.  
- [ ] Play Console shows small base app size + separate module.  
- [ ] App works without network (graceful degradation).

---

## 🚀 Implementation Notes
- **Kotlin DSL**: All Gradle files use Kotlin DSL syntax
- **ARM64 Only**: Optimized for size (no ARM32 support)
- **16KB Compatible**: Maintains page size alignment
- **Error Handling**: Graceful fallback if module fails to load
- **User Experience**: Seamless on-demand loading

## 🔧 **Critical Troubleshooting**

### **Issue 1: DFM Not Building / Not Included in AAB**
**Problem**: `ffmpeg_module` not included in App Bundle
**Causes**: Missing `dynamicFeatures` or flavor mismatch between base and feature
**Solutions**:
```kotlin
// In app/build.gradle.kts
android {
    dynamicFeatures += setOf(":ffmpeg_module")
}
```
Also add matching flavors in the feature (Step 5) and depend on the base:

```kotlin
// In ffmpeg_module/build.gradle.kts
android {
    flavorDimensions += "publishChannel"
    productFlavors {
        create("generic") { dimension = "publishChannel" }
        create("fdroid") { dimension = "publishChannel" }
    }
}

dependencies {
    implementation(project(":app"))
}
```

### **Issue 2: UnsatisfiedLinkError**
**Problem**: Native libraries not loading from DFM
**Solution**: Use `SplitInstallHelper.loadLibrary()` instead of `System.loadLibrary()`

### **Issue 3: Resource Access Errors**
**Problem**: `NoSuchFieldError` when accessing DFM resources
**Solution**: Add `SplitCompat.installActivity(this)` in activities

### **Issue 4: Module Installation Fails**
**Problem**: DFM installation fails silently
**Solution**: Check network connectivity and Play Store availability

### **Issue 5: Build Configuration Errors (e.g., `__applicationId__` Collection is empty)**
**Problem**: `:ffmpeg_module:processDebugMainManifest` fails to resolve `applicationId`
**Cause**: Feature variant cannot match a base variant due to missing flavors
**Fix**: Match the base module flavors/dimensions in the feature (Step 5) and add `implementation(project(":app"))`.

### **Issue 6: Variant Ambiguity for `project(:app)`**
**Problem**: "Unable to resolve dependency for ':ffmpeg_module@debug/compileClasspath'... ambiguity between `generic`/`fdroid`"
**Fix**: Same as above — declare the same `flavorDimensions` and `productFlavors` in the feature so Gradle can select the correct variant.

### **Issue 7: Strip errors for `.zip.so` files**
**Problem**: `llvm-strip` reports "not recognized as a valid object file" for `lib*.zip.so`
**Status**: Harmless if packaged as-is, but can fail strict pipelines
**Fix**: Add `keepDebugSymbols` for those files (Step 5b) or exclude them from the base and keep them only in the DFM.

---

⚡ This playbook is specifically tailored for the Twitter Downloader app architecture and requirements.

