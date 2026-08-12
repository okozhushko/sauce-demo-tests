# CLAUDE.md

Conventions for this repository. Read this before adding tests or framework code.

## Stack

Java 21, Gradle (Kotlin DSL), TestNG, Selenide (UI), REST Assured (API), Allure reports, SLF4J + Logback.

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
  testng/      suite XML files:
    testng.xml           full local suite (UI + API), no hard-coded browser
    testng-ui.xml         UI only, no hard-coded browser — this is what the CI browser
                           matrix (test-ui.yml) targets, so -Dbrowser=<name> takes effect
    testng-api.xml        API only
    testng-parallel.xml   nightly cross-browser suite; each <test> block hard-codes its
                           own <parameter name="browser">, so -Dbrowser has no effect here
                           by design — it runs chrome+firefox+edge in one invocation
```

## Conventions

- **Page Objects** extend `BasePage`, keep locators `private static final By`, and return the next page object from action methods (fluent navigation), e.g. `InventoryPage.openProduct()` returns `ProductPage`. Shared regions used by multiple pages (e.g. the header/cart nav) live in their own `<Name>Component` class and are composed into page objects rather than duplicated - see `HeaderComponent`.
- **API clients** extend `ApiClient`, one client per resource (e.g. `UserApiClient`), methods return `Response` — assertions belong in the test, not the client.
- **Config access**: always through `com.example.config.Config` (or `BrowserConfig`/`ApiConfig`), never read properties files or `System.getProperty` directly elsewhere. `Config.getProperty` lets `-D` system properties override the merged properties file, so CI can override anything without editing files.
- **Retries**: attach `retryAnalyzer = RetryAnalyzer.class` to `@Test` only for tests known to be environment-flaky — not as a blanket default. In this repo every UI/API test class currently targets a real public third-party demo endpoint (no local app/backend exists yet), so blanket retry is the accepted exception here — each class that does it says so in a class-level Javadoc comment. If/when this framework points at a real, reliably-available backend, drop back to attaching retries selectively rather than by default. Retry count is configured via `retry.count`, not hardcoded.
- **Allure**: annotate test classes with `@Story`, methods with `@Description`. Don't call Allure attachment helpers manually inside tests for failures — `TestListener` already attaches a screenshot + page source automatically on UI test failure.
- **New environment**: add `<env>.properties` under `src/test/resources/properties/` with only the keys that differ from `application.properties`; `Config` merges it in when run with `-Denvironment=<env>`.
- **New browser in CI matrix**: confirm the runner actually ships that browser (Safari does not exist on Linux runners; Safari is supported on macOS runners only) before adding it to a workflow matrix.
- **Safari testing**: Safari WebDriver is less stable than Chrome/Firefox/Edge. Tests running on Safari use `SafariRetryAnalyzer` (3 retries instead of 2) and run with reduced parallelism (thread-count=1 in parallel suites). Safari tests run locally on macOS with `-Dbrowser=safari` and in nightly cross-browser CI via `testng-parallel.xml`.

## Running tests

```bash
./gradlew test                                                            # default suite, chrome, dev
./gradlew test -Dbrowser=firefox                                          # UI tests on a specific browser
./gradlew test -Dbrowser=safari                                           # UI tests on Safari (macOS only)
./gradlew test -Dtestng.suites=src/test/resources/testng/testng-api.xml   # API suite only
./gradlew test -Dtestng.suites=src/test/resources/testng/testng-parallel.xml  # cross-browser parallel (chrome, firefox, edge, safari)
./gradlew test -Denvironment=staging                                      # against staging
./gradlew allureReport && ./gradlew allureServe                           # view report
```

## Adding a new UI test

1. Add/extend a Page Object under `pages/` if the flow touches a new page.
2. Add the test class under `src/test/java/com/example/ui/`, extend `BaseWebTest`.
3. Register the class in `testng/testng-ui.xml` (the per-browser CI matrix runs this one) *and* `testng/testng.xml` (full local suite) — a class only listed in one is only covered by that suite. Add it to `testng/testng-parallel.xml` too if it needs nightly cross-browser coverage, not just a smoke check.

## Adding a new API test

1. Add/extend a resource client under `api/clients/` (extends `ApiClient`) and DTOs under `api/models/`.
2. Add the test class under `src/test/java/com/example/api/`, extend `BaseTest`.
3. Register it in `testng/testng.xml` and `testng/testng-api.xml`.
