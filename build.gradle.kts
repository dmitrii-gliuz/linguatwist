version = "0.1.0"

plugins {
    kotlin("jvm") version "2.1.0"
    application
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
    implementation("com.github.ajalt.mordant:mordant:3.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.1")
}

sourceSets {
    main {
        kotlin {
            exclude("**/legacy/**")
        }
    }
}

application {
    mainClass.set("org.dmitriigliuz.linguatwist.MainKt")
    applicationDefaultJvmArgs = listOf("-Xmx2g", "-Xms512m")
}
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
tasks.named<JavaExec>("run") {
    jvmArgs("-Xmx2g", "-Xms512m")
}

tasks.test {
    useJUnitPlatform()
}

tasks.wrapper {
    gradleVersion = "8.2.1"
    distributionType = Wrapper.DistributionType.ALL
}