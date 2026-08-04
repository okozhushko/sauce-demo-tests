# CLAUDE.md

Conventions for this repository. Read this before adding tests or framework code.

## Stack

Java 17, Gradle (Kotlin DSL), TestNG, Selenide (UI), REST Assured (API), Allure reports, SLF4J + Logback.

## Layout

Standard single-module Gradle project — no `app/` subproject.

```
src/main/java/com/example/
  base/        shared test lifecycle (BaseTest, BaseWebTest)
  pages/       Page Objects (Selenide)
  api/clients/ REST Assured base client + resource clients
  api/endpoints/ endpoint path constants
  api/models/  request/response DTOs
  config/      Config, BrowserConfig, ApiConfig — read via Config, never System.getProperty() directly in test code
  listeners/   TestNG listeners, Allure attachment helpers
  utils/       PropertyReader, WaitUtils, ScreenshotUtils, DataProvider
  retry/       RetryAnalyzer

src/test/java/com/example/
  ui/   UI test classes, extend BaseWebTest
  api/  API test classes, extend BaseTest

src/test/resources/
  properties/  application.properties + one file per environment (dev/staging/production), merged by Config
  testdata/    JSON/CSV fixtures consumed by utils/DataProvider
  testng/      suite XML files (testng.xml full, testng-parallel.xml cross-browser, testng-api.xml API-only)
```

## Conventions

- **Page Objects** extend `BasePage`, keep locators `private static final By`, and return the next page object from action methods (fluent navigation), e.g. `LoginPage.clickLogin()` returns `DashboardPage`.
- **API clients** extend `ApiClient`, one client per resource (e.g. `UserApiClient`), methods return `Response` — assertions belong in the test, not the client.
- **Config access**: always through `com.example.config.Config` (or `BrowserConfig`/`ApiConfig`), never read properties files or `System.getProperty` directly elsewhere. `Config.getProperty` lets `-D` system properties override the merged properties file, so CI can override anything without editing files.
- **Retries**: attach `retryAnalyzer = RetryAnalyzer.class` to `@Test` only for tests known to be environment-flaky (network, browser timing) — not as a default for every test. Retry count is configured via `retry.count`, not hardcoded.
- **Allure**: annotate test classes with `@Story`, methods with `@Description`. Don't call Allure attachment helpers manually inside tests for failures — `TestListener` already attaches a screenshot + page source automatically on UI test failure.
- **New environment**: add `<env>.properties` under `src/test/resources/properties/` with only the keys that differ from `application.properties`; `Config` merges it in when run with `-Denvironment=<env>`.
- **New browser in CI matrix**: confirm the runner actually ships that browser (Safari does not exist on Linux runners) before adding it to a workflow matrix.

## Running tests

```bash
./gradlew test                                                            # default suite, chrome, dev
./gradlew test -Dbrowser=firefox                                          # UI tests on a specific browser
./gradlew test -Dtestng.suites=src/test/resources/testng/testng-api.xml   # API suite only
./gradlew test -Dtestng.suites=src/test/resources/testng/testng-parallel.xml  # cross-browser parallel
./gradlew test -Denvironment=staging                                      # against staging
./gradlew allureReport && ./gradlew allureServe                           # view report
```

## Adding a new UI test

1. Add/extend a Page Object under `pages/` if the flow touches a new page.
2. Add the test class under `src/test/java/com/example/ui/`, extend `BaseWebTest`.
3. Register the class in the relevant `testng/*.xml` suite `<classes>` block — tests not listed in a suite don't run in CI.

## Adding a new API test

1. Add/extend a resource client under `api/clients/` (extends `ApiClient`) and DTOs under `api/models/`.
2. Add the test class under `src/test/java/com/example/api/`, extend `BaseTest`.
3. Register it in `testng/testng.xml` and `testng/testng-api.xml`.
