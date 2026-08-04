plugins {
    java
    id("io.qameta.allure") version "2.11.2"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val allureVersion = "2.25.0"
val aspectjVersion = "1.9.22"

val agent by configurations.creating

dependencies {
    agent("org.aspectj:aspectjweaver:$aspectjVersion")

    // Testing Framework
    implementation("org.testng:testng:7.9.0")

    // Selenide for UI testing
    implementation("com.codeborne:selenide:7.2.0")

    // REST Assured for API testing
    implementation("io.rest-assured:rest-assured:5.4.0")
    implementation("io.rest-assured:json-path:5.4.0")
    implementation("io.rest-assured:xml-path:5.4.0")

    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")

    // Allure Reports
    implementation("io.qameta.allure:allure-testng:$allureVersion")
    implementation("io.qameta.allure:allure-rest-assured:$allureVersion")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.5.0")

    // WebDriverManager for cross-browser support
    implementation("io.github.bonigarcia:webdrivermanager:5.9.3")

    // Configuration
    implementation("org.apache.commons:commons-lang3:3.14.0")
}

tasks.test {
    useTestNG {
        val suiteFile = System.getProperty("testng.suites", "src/test/resources/testng/testng.xml")
        suites(suiteFile)
    }
    // Allure's @Attachment/@Step annotations (used by AllureAttachments, ScreenshotUtils)
    // are woven in at runtime via AspectJ load-time weaving - without this javaagent they
    // fail with NoSuchMethodError on the generated aspectOf() call the first time one of
    // those methods actually runs.
    jvmArgs("-javaagent:${agent.singleFile}")
    systemProperty("browser", System.getProperty("browser", "chrome"))
    systemProperty("environment", System.getProperty("environment", "dev"))
    // The `test` task runs in a forked JVM, which does NOT automatically inherit -D flags
    // passed on the `./gradlew` command line - only properties explicitly registered here
    // via systemProperty(...) reach test code. Forward every `-Dbrowser.*` override (e.g.
    // browser.chrome.headless=true from CI) so BaseWebTest/BrowserConfig actually see them,
    // instead of always silently falling back to the application.properties default.
    System.getProperties().stringPropertyNames()
        .filter { it.startsWith("browser.") }
        .forEach { key -> systemProperty(key, System.getProperty(key)) }
    outputs.upToDateWhen { false }
}

allure {
    version.set(allureVersion)
    adapter {
        frameworks {
            testng
        }
    }
}
