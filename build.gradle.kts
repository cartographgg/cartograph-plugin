plugins {
    alias(libs.plugins.shadow) apply false
}

allprojects {
    group = property("group") as String
    version = property("version") as String
}
