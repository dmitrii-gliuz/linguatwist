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
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.dhatim:fastexcel:0.19.0")
    implementation("org.dhatim:fastexcel-reader:0.19.0")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.5.13")
//    implementation("org.apache.poi:poi:5.2.5")
//    implementation("org.apache.poi:poi-ooxml:5.4.0")
//    implementation(platform("org.apache.logging.log4j:log4j-bom:2.24.3"))
//    implementation("org.apache.logging.log4j:log4j-api")
//    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.1")
}

application {
    mainClass.set("org.dmitriigliuz.linguatwist.MainKt")
    applicationDefaultJvmArgs = listOf("-Xmx2g", "-Xms512m")
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