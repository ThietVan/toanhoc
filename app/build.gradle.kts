plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.toanhoc"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.toanthongthai.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 12
        versionName = "1.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Giọng Piper (sherpa-onnx) có thư viện C++: chỉ đóng gói cho máy ARM và máy ảo x86_64
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64") }
    }
    androidResources { noCompress += "onnx" }

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
    buildFeatures {
        compose = true
    }
}

/* ------------------------------------------------------------------------------------------
 * GIỌNG ĐỌC PIPER vi_VN-vais1000 (chạy trên máy, không cần mạng)
 *  - Lần build đầu tiên Gradle tự tải (cần mạng, ~100 MB, chỉ một lần):
 *      · thư viện sherpa-onnx (Apache 2.0)  → app/libs/ (tải khi Sync)
 *      · gói giọng vits-piper-vi_VN-vais1000-medium → app/.piper-cache/
 *  - Giọng được giải nén vào assets "piper/" (model.onnx, tokens.txt, espeak-ng-data/ – chỉ giữ từ điển vi, en).
 *  - Nếu không tải được, app vẫn build và dùng giọng Google như cũ.
 * Ghi nguồn: Piper vi_VN-vais1000 (MIT) · dữ liệu VAIS-1000 – Truong Do (CC BY 4.0) · espeak-ng (GPL-3.0).
 * ---------------------------------------------------------------------------------------- */
val sherpaVersion = "1.13.7"

abstract class FetchFile : DefaultTask() {
    @get:Input abstract val url: Property<String>
    @get:OutputFile abstract val dest: RegularFileProperty

    @TaskAction
    fun run() {
        val out = dest.get().asFile
        if (out.isFile && out.length() > 0) return
        out.parentFile.mkdirs()
        val part = java.io.File(out.path + ".part")
        logger.lifecycle("Đang tải ${url.get()} (một lần)…")
        try {
            java.net.URI(url.get()).toURL().openStream().use { i -> part.outputStream().use { i.copyTo(it) } }
            if (!part.renameTo(out)) throw GradleException("Không lưu được $out")
        } catch (e: Exception) {
            part.delete()
            logger.warn("Không tải được ${url.get()} – app sẽ dùng giọng Google. (${e.message})")
            out.writeBytes(ByteArray(0))
        }
    }
}

abstract class UnpackPiper : DefaultTask() {
    @get:InputFile abstract val archive: RegularFileProperty
    @get:OutputDirectory abstract val outDir: DirectoryProperty
    @get:javax.inject.Inject abstract val archives: ArchiveOperations
    @get:javax.inject.Inject abstract val fs: FileSystemOperations

    @TaskAction
    fun run() {
        val out = outDir.get().asFile
        fs.delete { delete(out) }
        out.mkdirs()
        val tar = archive.get().asFile
        if (tar.length() == 0L) return
        fs.copy {
            from(archives.tarTree(archives.bzip2(tar)))
            include("*/*.onnx", "*/tokens.txt", "*/MODEL_CARD", "*/espeak-ng-data/**")
            exclude(org.gradle.api.specs.Spec<FileTreeElement> { e ->
                !e.isDirectory && e.name.endsWith("_dict") && e.name != "vi_dict" && e.name != "en_dict"
            })
            eachFile {
                val segs = relativePath.segments.drop(1).toMutableList()
                if (segs.size == 1 && segs[0].endsWith(".onnx")) segs[0] = "model.onnx"
                relativePath = RelativePath(!isDirectory, *(listOf("piper") + segs).toTypedArray())
            }
            includeEmptyDirs = false
            into(out)
        }
    }
}

// Thư viện sherpa-onnx: tải ngay khi Sync (một lần) để Android Studio thấy được tệp .aar
val sherpaAar = file("libs/sherpa-onnx-$sherpaVersion.aar")
if (!sherpaAar.isFile || sherpaAar.length() == 0L) {
    sherpaAar.parentFile.mkdirs()
    val part = File(sherpaAar.path + ".part")
    val src = "https://github.com/k2-fsa/sherpa-onnx/releases/download/v$sherpaVersion/sherpa-onnx-$sherpaVersion.aar"
    logger.lifecycle("Đang tải thư viện giọng đọc $src (một lần)…")
    runCatching {
        java.net.URI(src).toURL().openStream().use { i -> part.outputStream().use { i.copyTo(it) } }
        part.renameTo(sherpaAar)
    }.onFailure { part.delete(); logger.warn("Không tải được sherpa-onnx (${it.message}) – app sẽ dùng giọng Google.") }
}
val fetchPiperVoice = tasks.register<FetchFile>("fetchPiperVoice") {
    url.set("https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-vi_VN-vais1000-medium.tar.bz2")
    dest.set(layout.projectDirectory.file(".piper-cache/vits-piper-vi_VN-vais1000-medium.tar.bz2"))
}
androidComponents {
    onVariants { variant ->
        val unpack = tasks.register<UnpackPiper>("unpackPiper${variant.name.replaceFirstChar { it.uppercase() }}") {
            archive.set(fetchPiperVoice.flatMap { it.dest })
        }
        variant.sources.assets?.addGeneratedSourceDirectory(unpack, UnpackPiper::outDir)
    }
}

dependencies {
    // sherpa-onnx: app gọi qua phản chiếu (PiperTts.kt) nên vẫn build được khi thiếu tệp này
    if (sherpaAar.isFile && sherpaAar.length() > 0) implementation(files(sherpaAar))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
