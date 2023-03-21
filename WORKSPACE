load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "4.5"
RULES_JVM_EXTERNAL_SHA = "b17d7388feb9bfa7f2fa09031b32707df529f26c91ab9e5d909eb1676badd9a6"

# Add support for external deps: https://github.com/bazelbuild/rules_jvm_external.
http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

# Add support for Kotlin: https://github.com/bazelbuild/rules_kotlin.
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

rules_kotlin_version = "1.7.1"
rules_kotlin_sha = "fd92a98bd8a8f0e1cdcb490b93f5acef1f1727ed992571232d33de42395ca9b3"
http_archive(
    name = "io_bazel_rules_kotlin",
    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/v%s/rules_kotlin_release.tgz" % rules_kotlin_version],
    sha256 = rules_kotlin_sha,
)

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories")
kotlin_repositories() # if you want the default. Otherwise see custom kotlinc distribution below

register_toolchains("//:kt_toolchain")

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
