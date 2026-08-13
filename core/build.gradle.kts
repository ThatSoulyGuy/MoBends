plugins {
    `java-library`
}

base {
    archivesName.set("${mod.id}-core")
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.code.gson:gson:2.10.1")
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("org.slf4j:slf4j-api:2.0.9")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}
