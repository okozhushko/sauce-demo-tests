# 🧪 Test Framework Blueprint

Комплексний промпт для створення масштабованого test framework для API та UI тестів з використанням Java, Selenide, TestNG, Page Object, Allure reports та CI/CD pipeline.

## 🎯 Мета

Розробити модульний, легко масштабуваний test framework з підтримкою:
- ✅ **UI тестування** на кількох браузерах (Chrome, Firefox, Edge, Safari)
- ✅ **API тестування** з REST Assured
- ✅ **Page Object Pattern** для чистого коду
- ✅ **Паралельне виконання** тестів
- ✅ **Автоматичні повторні спроби** (retry механізм)
- ✅ **Красиві звіти** через Allure
- ✅ **CI/CD pipeline** через GitHub Actions
- ✅ **Конфігурування** через properties файли

---

## 🏗️ Структура проекту

```
java-test-framework/
│
├── app/
│   └── src/
│       ├── main/java/com/example/
│       │   ├── base/
│       │   │   ├── BaseTest.java              # Базовий клас для всіх тестів
│       │   │   └── BaseWebTest.java           # Базовий клас для UI тестів
│       │   │
│       │   ├── pages/
│       │   │   ├── BasePage.java              # Батьківський клас для Page Object
│       │   │   ├── LoginPage.java             # Приклад Page Object
│       │   │   └── DashboardPage.java         # Приклад Page Object
│       │   │
│       │   ├── api/
│       │   │   ├── clients/
│       │   │   │   └── ApiClient.java         # Base API client
│       │   │   ├── endpoints/
│       │   │   │   └── UserEndpoints.java     # Endpoints for API
│       │   │   └── models/
│       │   │       ├── UserRequest.java       # Request DTOs
│       │   │       └── UserResponse.java      # Response DTOs
│       │   │
│       │   ├── config/
│       │   │   ├── Config.java                # Конфігурація фреймворку
│       │   │   ├── BrowserConfig.java         # Конфіг браузерів
│       │   │   └── ApiConfig.java             # Конфіг API
│       │   │
│       │   ├── listeners/
│       │   │   ├── TestListener.java          # Listener для TestNG
│       │   │   └── AllureAttachments.java     # Attachments для Allure
│       │   │
│       │   ├── utils/
│       │   │   ├── PropertyReader.java        # Читання properties
│       │   │   ├── WaitUtils.java             # Утиліти для очікування
│       │   │   ├── ScreenshotUtils.java       # Скриншоти
│       │   │   └── DataProvider.java          # Дані для параметризованих тестів
│       │   │
│       │   └── retry/
│       │       └── RetryAnalyzer.java         # Retry механізм
│       │
│       └── test/java/com/example/
│           ├── ui/
│           │   ├── LoginTests.java            # UI тести
│           │   └── DashboardTests.java        # UI тести
│           │
│           └── api/
│               └── UserApiTests.java          # API тести
│
├── src/test/resources/
│   ├── properties/
│   │   ├── application.properties       # Основні налаштування
│   │   ├── dev.properties               # Розробка
│   │   ├── staging.properties           # Staging
│   │   └── production.properties        # Production
│   │
│   ├── testdata/
│   │   ├── users.json                   # Тестові дані
│   │   └── testcases.csv                # CSV для DataProvider
│   │
│   └── testng/
│       ├── testng.xml                   # TestNG конфігурація
│       └── testng-parallel.xml          # Паралельна конфігурація
│
├── .github/workflows/
│   ├── test-api.yml                     # CI/CD для API тестів
│   ├── test-ui.yml                      # CI/CD для UI тестів
│   └── parallel-tests.yml               # Паралельна CI/CD
│
├── build.gradle.kts                     # Градл конфіг
├── CLAUDE.md                            # Конвенції проекту
├── README.md                            # Документація
└── gradle.properties
```

---

## 📦 Залежності (Gradle)

```gradle
plugins {
    id 'java'
    id 'io.qameta.allure' version '2.11.1'
}

repositories {
    mavenCentral()
}

dependencies {
    // Testing Framework
    testImplementation 'org.testng:testng:7.8.1'
    
    // Selenide for UI testing
    testImplementation 'com.codeborne:selenide:7.2.0'
    
    // REST Assured for API testing
    testImplementation 'io.rest-assured:rest-assured:5.4.0'
    testImplementation 'io.rest-assured:json-path:5.4.0'
    testImplementation 'io.rest-assured:xml-path:5.4.0'
    
    // JSON processing
    testImplementation 'com.google.code.gson:gson:2.10.1'
    testImplementation 'com.fasterxml.jackson.core:jackson-databind:2.17.0'
    
    // Allure Reports
    testImplementation 'io.qameta.allure:allure-testng:2.25.0'
    testImplementation 'io.qameta.allure:allure-rest-assured:2.25.0'
    
    // Logging
    testImplementation 'org.slf4j:slf4j-api:2.0.11'
    testImplementation 'ch.qos.logback:logback-classic:1.5.0'
    
    // WebDriverManager for cross-browser support
    testImplementation 'io.github.bonigarcia:webdrivermanager:5.8.3'
    
    // Configuration
    testImplementation 'org.apache.commons:commons-lang3:3.14.0'
}

tasks.register('test') {
    useTestNG()
}
```

---

## 💻 Основні класи

### 1. BaseTest.java

```java
package com.example.base;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    
    @BeforeMethod
    public void setUp() {
        logger.info("Setting up test...");
    }
    
    @AfterMethod
    public void tearDown() {
        logger.info("Tearing down test...");
    }
}
```

### 2. BaseWebTest.java

```java
package com.example.base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Parameters;

public class BaseWebTest extends BaseTest {
    
    @BeforeMethod
    @Parameters("browser")
    public void setUpBrowser(String browser) {
        Configuration.browser = browser != null ? browser : "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10000;
        Configuration.pageLoadTimeout = 15000;
        
        logger.info("Opening browser: {}", Configuration.browser);
    }
    
    @AfterMethod
    public void tearDownBrowser() {
        Selenide.closeWebDriver();
        logger.info("Browser closed");
    }
}
```

### 3. BasePage.java

```java
package com.example.pages;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import static com.codeborne.selenide.Selenide.$;

public class BasePage {
    
    protected SelenideElement $(By locator) {
        return Selenide.$(locator);
    }
    
    protected void click(By locator) {
        $(locator).click();
    }
    
    protected void fillField(By locator, String text) {
        $(locator).setValue(text);
    }
    
    protected String getText(By locator) {
        return $(locator).getText();
    }
    
    protected boolean isVisible(By locator) {
        return $(locator).isDisplayed();
    }
}
```

### 4. LoginPage.java - Приклад Page Object

```java
package com.example.pages;

import com.codeborne.selenide.Selenide;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {
    
    private static final By USERNAME_FIELD = By.id("username");
    private static final By PASSWORD_FIELD = By.id("password");
    private static final By LOGIN_BUTTON = By.xpath("//button[@type='submit']");
    private static final By ERROR_MESSAGE = By.className("error-message");
    
    public LoginPage openPage(String baseUrl) {
        Selenide.open(baseUrl + "/login");
        return this;
    }
    
    public LoginPage enterUsername(String username) {
        fillField(USERNAME_FIELD, username);
        return this;
    }
    
    public LoginPage enterPassword(String password) {
        fillField(PASSWORD_FIELD, password);
        return this;
    }
    
    public DashboardPage clickLogin() {
        click(LOGIN_BUTTON);
        return new DashboardPage();
    }
    
    public String getErrorMessage() {
        return getText(ERROR_MESSAGE);
    }
    
    public boolean isErrorMessageVisible() {
        return isVisible(ERROR_MESSAGE);
    }
}
```

### 5. ApiClient.java - Base API Client

```java
package com.example.api.clients;

import io.rest-assured.RestAssured;
import io.rest-assured.response.Response;
import io.rest-assured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    protected String baseUri;
    protected String basePath = "";
    
    public ApiClient(String baseUri) {
        this.baseUri = baseUri;
        RestAssured.baseURI = baseUri;
    }
    
    protected RequestSpecification given() {
        return RestAssured
            .given()
            .contentType("application/json")
            .baseUri(baseUri)
            .basePath(basePath)
            .log().all();
    }
    
    protected Response get(String endpoint) {
        logger.info("GET request to: {}", endpoint);
        return given()
            .when()
            .get(endpoint)
            .then()
            .log().all()
            .extract()
            .response();
    }
    
    protected Response post(String endpoint, Object body) {
        logger.info("POST request to: {} with body: {}", endpoint, body);
        return given()
            .body(body)
            .when()
            .post(endpoint)
            .then()
            .log().all()
            .extract()
            .response();
    }
    
    protected Response put(String endpoint, Object body) {
        logger.info("PUT request to: {} with body: {}", endpoint, body);
        return given()
            .body(body)
            .when()
            .put(endpoint)
            .then()
            .log().all()
            .extract()
            .response();
    }
    
    protected Response delete(String endpoint) {
        logger.info("DELETE request to: {}", endpoint);
        return given()
            .when()
            .delete(endpoint)
            .then()
            .log().all()
            .extract()
            .response();
    }
}
```

### 6. RetryAnalyzer.java - Механізм повторних спроб

```java
package com.example.retry;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryAnalyzer implements IRetryAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY_COUNT = 2;
    private int retryCount = 0;
    
    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT && result.getStatus() == ITestResult.FAILURE) {
            retryCount++;
            logger.warn("Test {} failed. Retry attempt: {}/{}", 
                result.getName(), retryCount, MAX_RETRY_COUNT);
            return true;
        }
        return false;
    }
}
```

### 7. Config.java - Конфігурація

```java
package com.example.config;

import java.io.IOException;
import java.util.Properties;

public class Config {
    
    private static Properties properties;
    private static final String PROPERTIES_FILE = "application.properties";
    
    static {
        properties = new Properties();
        try {
            properties.load(Config.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load properties file", e);
        }
    }
    
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public static String getBaseUrl() {
        return getProperty("base.url");
    }
    
    public static String getApiBaseUrl() {
        return getProperty("api.base.url");
    }
    
    public static int getTimeout() {
        return Integer.parseInt(getProperty("timeout"));
    }
}
```

---

## 🧪 Приклади тестів

### UI тест - LoginTests.java

```java
package com.example.ui;

import com.example.base.BaseWebTest;
import com.example.config.Config;
import com.example.pages.LoginPage;
import com.example.pages.DashboardPage;
import com.example.retry.RetryAnalyzer;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import org.testng.Assert;

@Story("Authentication")
public class LoginTests extends BaseWebTest {
    
    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Description("Valid user should be able to login successfully")
    public void testValidLogin() {
        LoginPage loginPage = new LoginPage();
        DashboardPage dashboardPage = loginPage
            .openPage(Config.getBaseUrl())
            .enterUsername("testuser")
            .enterPassword("password123")
            .clickLogin();
        
        Assert.assertTrue(dashboardPage.isUserLoggedIn());
    }
    
    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Description("Invalid credentials should show error message")
    public void testInvalidLogin() {
        LoginPage loginPage = new LoginPage();
        loginPage
            .openPage(Config.getBaseUrl())
            .enterUsername("testuser")
            .enterPassword("wrongpassword")
            .clickLogin();
        
        Assert.assertTrue(loginPage.isErrorMessageVisible());
        Assert.assertEquals(loginPage.getErrorMessage(), "Invalid credentials");
    }
}
```

### API тест - UserApiTests.java

```java
package com.example.api;

import com.example.base.BaseTest;
import com.example.api.clients.UserApiClient;
import com.example.api.models.UserRequest;
import com.example.config.Config;
import com.example.retry.RetryAnalyzer;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;

@Story("User API")
public class UserApiTests extends BaseTest {
    
    private UserApiClient userApiClient;
    
    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Description("Get user by ID should return correct user data")
    public void testGetUserById() {
        userApiClient = new UserApiClient(Config.getApiBaseUrl());
        
        var response = userApiClient.getUserById(1);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertNotNull(response.getBody().getId());
        Assert.assertNotNull(response.getBody().getName());
    }
    
    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Description("Create new user should return 201 status code")
    public void testCreateUser() {
        userApiClient = new UserApiClient(Config.getApiBaseUrl());
        
        UserRequest userRequest = new UserRequest()
            .setName("John Doe")
            .setEmail("john@example.com");
        
        var response = userApiClient.createUser(userRequest);
        
        Assert.assertEquals(response.getStatusCode(), 201);
        Assert.assertNotNull(response.getBody().getId());
    }
}
```

---

## ⚙️ Конфігурація

### application.properties

```properties
# Environment configuration
environment=dev
base.url=https://www.example.com
api.base.url=https://api.example.com

# Timeouts (in milliseconds)
timeout=10000
page.load.timeout=15000

# Browser configuration
browser.chrome.headless=false
browser.firefox.headless=false
browser.edge.headless=false
browser.safari.headless=false

# Logging
logging.level=INFO

# Retry configuration
retry.count=2

# Allure configuration
allure.results.directory=build/allure-results
```

### dev.properties

```properties
environment=dev
base.url=http://localhost:3000
api.base.url=http://localhost:8080
logging.level=DEBUG
```

### staging.properties

```properties
environment=staging
base.url=https://staging.example.com
api.base.url=https://api-staging.example.com
logging.level=INFO
```

### production.properties

```properties
environment=production
base.url=https://www.example.com
api.base.url=https://api.example.com
logging.level=WARN
```

### testng.xml - TestNG конфігурація

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="Test Suite" thread-count="4" parallel="methods">
    
    <listeners>
        <listener class-name="com.example.listeners.TestListener"/>
        <listener class-name="io.qameta.allure.testng.AllureTestNg"/>
    </listeners>
    
    <test name="UI Tests - Chrome" parallel="methods" thread-count="2">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="com.example.ui.LoginTests"/>
            <class name="com.example.ui.DashboardTests"/>
        </classes>
    </test>
    
    <test name="UI Tests - Firefox" parallel="methods" thread-count="2">
        <parameter name="browser" value="firefox"/>
        <classes>
            <class name="com.example.ui.LoginTests"/>
        </classes>
    </test>
    
    <test name="UI Tests - Edge" parallel="methods" thread-count="2">
        <parameter name="browser" value="edge"/>
        <classes>
            <class name="com.example.ui.LoginTests"/>
        </classes>
    </test>
    
    <test name="UI Tests - Safari" parallel="methods" thread-count="2">
        <parameter name="browser" value="safari"/>
        <classes>
            <class name="com.example.ui.LoginTests"/>
        </classes>
    </test>
    
    <test name="API Tests" parallel="methods" thread-count="4">
        <classes>
            <class name="com.example.api.UserApiTests"/>
        </classes>
    </test>
    
</suite>
```

---

## 🚀 CI/CD Pipeline (GitHub Actions)

### .github/workflows/test-ui.yml

```yaml
name: UI Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  ui-tests:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        browser: [chrome, firefox, edge]
      fail-fast: false
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build with Gradle
        run: ./gradlew build
      
      - name: Run UI Tests - ${{ matrix.browser }}
        run: |
          ./gradlew test \
            -Dbrowser=${{ matrix.browser }} \
            -Dtestng.suites=src/test/resources/testng/testng.xml
      
      - name: Generate Allure Report
        if: always()
        run: ./gradlew allureReport
      
      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: allure-report-${{ matrix.browser }}
          path: build/reports/allure-report/
      
      - name: Publish Allure Report
        if: always()
        uses: simple-elf/allure-report-action@master
        with:
          allure_results: build/allure-results
          gh_pages: gh-pages
```

### .github/workflows/test-api.yml

```yaml
name: API Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  api-tests:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build with Gradle
        run: ./gradlew build
      
      - name: Run API Tests
        run: ./gradlew test -Dtestng.suites=src/test/resources/testng/testng-api.xml
      
      - name: Generate Allure Report
        if: always()
        run: ./gradlew allureReport
      
      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: allure-report-api
          path: build/reports/allure-report/
```

### .github/workflows/parallel-tests.yml

```yaml
name: Parallel Tests on Multiple Browsers

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  schedule:
    - cron: '0 0 * * *'  # Щодня о полуночі

jobs:
  parallel-ui-tests:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        browser: [chrome, firefox, edge]
      fail-fast: false
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run Tests on ${{ matrix.browser }}
        run: ./gradlew test -Dbrowser=${{ matrix.browser }} -Dparallel=true
      
      - name: Generate Allure Report
        if: always()
        run: ./gradlew allureReport
      
      - name: Upload Artifact
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: allure-${{ matrix.browser }}
          path: build/allure-results/
```

---

## 🚀 Команди для запуску

```bash
# Запустити всі тести
./gradlew test

# Запустити тільки UI тести на Chrome
./gradlew test -Dbrowser=chrome

# Запустити паралельно на кількох браузерах
./gradlew test -Dbrowser=chrome,firefox,edge

# Запустити тільки API тести
./gradlew test -Dtest.api=true

# Генерувати Allure звіт
./gradlew allureReport

# Відкрити Allure звіт у браузері
./gradlew allureServe

# Запустити тести з конкретною конфіг файлом
./gradlew test -Denvironment=staging

# Запустити один клас тестів
./gradlew test --tests com.example.ui.LoginTests
```

---

## 📝 WaitUtils.java - Утиліти для очікування

```java
package com.example.utils;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import java.time.Duration;
import static com.codeborne.selenide.Selenide.$;

public class WaitUtils {
    
    public static void waitForElementVisible(By locator, int timeout) {
        $(locator).shouldBe(Condition.visible, Duration.ofMillis(timeout));
    }
    
    public static void waitForElementClickable(By locator) {
        $(locator).shouldBe(Condition.enabled);
    }
    
    public static void waitForText(By locator, String text) {
        $(locator).shouldHave(Condition.text(text));
    }
    
    public static void waitForUrl(String url) {
        Selenide.webdriver().shouldHave(Condition.url(url));
    }
    
    public static void waitSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

---

## ✅ Чек-лист для реалізації

- [ ] Налаштування Gradle з усіма залежностями
- [ ] Створення базових класів (BaseTest, BaseWebTest)
- [ ] Реалізація BasePage та LoginPage
- [ ] Налаштування ApiClient для REST Assured
- [ ] Реалізація RetryAnalyzer
- [ ] Налаштування Allure Reports
- [ ] Конфіг TestNG з паралелізацією
- [ ] Налаштування логування (SLF4J + Logback)
- [ ] Створення приклад UI тестів
- [ ] Створення приклад API тестів
- [ ] Налаштування GitHub Actions CI/CD pipeline
- [ ] Документація та README
- [ ] Налаштування конфіг файлів (dev, staging, production)
- [ ] TestListener з Allure attachments
- [ ] Утиліти для очікування та скриншотів

---

## 📚 Корисні ресурси та посилання

- **Selenide**: https://selenide.org
- **TestNG**: https://testng.org
- **REST Assured**: https://rest-assured.io
- **Allure Reports**: https://docs.qameta.io/allure
- **WebDriverManager**: https://github.com/bonigarcia/webdrivermanager
- **Gradle**: https://gradle.org
- **GitHub Actions**: https://github.com/features/actions

---

## 🎓 Коротка документація

### Page Object Pattern
Забезпечує інкапсуляцію елементів сторінки та методів взаємодії з ними. Кожна сторінка має свій клас з приватними локаторами та публічними методами.

### Retry механізм
Якщо тест падає, він автоматично повторяється до 2 разів. Це помагає впоратися з нестійкими тестами.

### Паралелізація
Тести можуть виконуватися одночасно на кількох потоках або браузерах, що значно прискорює выполнение.

### Allure Reports
Генерує красиві HTML звіти з деталями про кожен тест, включаючи скриншоти, логи та attachments.

---

**Автор**: Claude Code Assistant  
**Дата**: 2026-08-04  
**Версія**: 1.0  

Цей blueprint готовий до використання. Просто слідуй чек-листу та адаптуй під свої потреби!
