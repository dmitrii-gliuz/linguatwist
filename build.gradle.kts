version = "0.1.0"

plugins {
    kotlin("jvm") version "2.3.0"
    application
    id("de.comahe.i18n4k") version "0.11.1"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    constraints {
        implementation("org.apache.commons:commons-lang3:3.20.0")
    }
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.dhatim:fastexcel:0.19.0")
    implementation("org.dhatim:fastexcel-reader:0.19.0")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.14")
    implementation("ch.qos.logback:logback-classic:1.5.29")
    implementation("com.github.ajalt.clikt:clikt:5.1.0")
    implementation("de.comahe.i18n4k:i18n4k-core-jvm:0.11.1")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.1")
}

application {
    mainClass.set("org.dmitriigliuz.linguatwist.MainKt")
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks.named<JavaExec>("run") {
    jvmArgs("-Xmx512m", "--enable-native-access=ALL-UNNAMED")
    standardInput = System.`in`
}

tasks.test {
    useJUnitPlatform()
}

tasks.wrapper {
    gradleVersion = "9.4.1"
    distributionType = Wrapper.DistributionType.ALL
}