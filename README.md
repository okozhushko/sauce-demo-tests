# java-test-framework

A modular Java test automation framework for UI and API testing, built on Selenide, REST Assured, TestNG, and Allure reporting.

## Features

- UI testing across Chrome, Firefox, and Edge via [Selenide](https://selenide.org)
- API testing via [REST Assured](https://rest-assured.io)
- Page Object pattern for UI tests
- Parallel test execution (TestNG suite/class/method level)
- Automatic retry for flaky tests (`RetryAnalyzer`)
- [Allure](https://docs.qameta.io/allure) HTML reports with automatic screenshot + page-source attachment on UI failure
- Environment-aware configuration (`dev` / `staging` / `production`)
- GitHub Actions CI/CD pipelines

## Prerequisites

- JDK 21+
- Chrome/Firefox installed locally for UI test runs (driver binaries are fetched automatically by WebDriverManager)

## Getting started

```bash
./gradlew build
./gradlew test
```

## Project structure

```
src/main/java/com/example/
├── base/            # BaseTest, BaseWebTest — shared test lifecycle
├── pages/           # Page Objects
├── api/
│   ├── clients/     # REST Assured base client + resource clients
│   ├── endpoints/   # endpoint path constants
│   └── models/      # request/response DTOs
├── config/          # Config, BrowserConfig, ApiConfig
├── listeners/       # TestNG listeners, Allure attachment helpers
├── utils/           # PropertyReader, WaitUtils, ScreenshotUtils, DataProvider
└── retry/           # RetryAnalyzer

src/test/java/com/example/
├── ui/              # UI test classes
└── api/             # API test classes

src/test/resources/
├── properties/      # application.properties + per-environment overrides
├── testdata/        # JSON/CSV fixtures
└── testng/          # TestNG suite XML files

.github/workflows/   # CI/CD pipelines
```

## Running tests

```bash
# Full suite (default: chrome, dev environment)
./gradlew test

# UI tests on a specific browser
./gradlew test -Dbrowser=firefox

# API tests only
./gradlew test -Dtestng.suites=src/test/resources/testng/testng-api.xml

# Cross-browser parallel run
./gradlew test -Dtestng.suites=src/test/resources/testng/testng-parallel.xml

# Against a specific environment
./gradlew test -Denvironment=staging

# A single test class
./gradlew test --tests com.example.ui.LoginTests
```

## Reports

```bash
./gradlew allureReport   # generate static HTML report under build/reports/allure-report
./gradlew allureServe    # generate + open the report in a browser
```

## Configuration

Base settings live in `src/test/resources/properties/application.properties`. Per-environment files (`dev.properties`, `staging.properties`, `production.properties`) override only the keys that differ, and are merged in based on `-Denvironment=<name>` (default `dev`). Any property can also be overridden ad hoc via `-D<key>=<value>` on the Gradle command line.

## CI/CD

Three GitHub Actions workflows under `.github/workflows/`:

- `test-ui.yml` — UI tests on push/PR to `main`/`develop`, matrixed across browsers (chrome/firefox/edge, one gradle run per browser against `testng-ui.xml`)
- `test-api.yml` — API tests on push/PR to `main`/`develop`
- `parallel-tests.yml` — full cross-browser regression via `testng-parallel.xml` in a single job (that suite already runs chrome+firefox+edge together); nightly (02:00 UTC) + manual dispatch only, not on push/PR, to avoid duplicating `test-ui.yml`'s coverage

Each workflow uploads the generated Allure report as a build artifact.

## Conventions

See [`CLAUDE.md`](./CLAUDE.md) for naming, layout, and contribution conventions.
