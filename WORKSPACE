load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@rules_jvm_external//:defs.bzl", "artifact")

RULES_JVM_EXTERNAL_TAG = "4.0"
RULES_JVM_EXTERNAL_SHA = "31701ad93dbfe544d597dbe62c9a1fdd76d81d8a9150c2bf1ecf928ecdf97169"
http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

# Add support for Kotlin: https://github.com/bazelbuild/rules_kotlin.
rules_kotlin_version = "v1.5.0-alpha-2"
rules_kotlin_sha = "6194a864280e1989b6d8118a4aee03bb50edeeae4076e5bc30eef8a98dcd4f07"
http_archive(
    name = "io_bazel_rules_kotlin",
    sha256 = rules_kotlin_sha,
    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/%s/rules_kotlin_release.tgz" % rules_kotlin_version],
)

android_sdk_repository(
  name = "androidsdk",
  api_level = 28,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
     artifacts = [
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-RC",
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.0-RC",
        "androidx.appcompat:appcompat:1.0.2",
        "androidx.test:core:1.0.0",
        "androidx.test.espresso:espresso-core:3.2.0",
        "androidx.test.ext:junit:1.0.0",
        "junit:junit:4.12",
        "androidx.test:rules:1.1.0",
        "androidx.test:runner:1.1.1"
     ],
     fetch_sources = True,
     repositories =  [
        "https://maven.fabric.io/public",
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
)
