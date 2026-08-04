# 🚀 Quick Reference Guide

Швидкий довідник для роботи з framework-ом

## Структура папок

```
src/main/java/com/example/
├── base/          → Базові класи (BaseTest, BaseWebTest)
├── pages/         → Page Objects для UI тестів
├── api/           → API clients, endpoints, models
├── config/        → Конфігураційні класи
├── listeners/     → TestNG listeners
├── utils/         → Утиліти
└── retry/         → Retry механізм
```

## Найпопулярніші команди

| Команда | Опис |
|---------|------|
| `./gradlew test` | Запустити всі тести |
| `./gradlew test -Dbrowser=chrome` | Запустити на Chrome |
| `./gradlew allureReport` | Генерувати звіт |
| `./gradlew allureServe` | Відкрити звіт у браузері |
| `./gradlew build` | Збудувати проект |
| `./gradlew clean` | Очистити проект |

## Шаблон UI тесту

```java
@Test(retryAnalyzer = RetryAnalyzer.class)
@Description("Опис того, що робить тест")
public void testSomething() {
    LoginPage loginPage = new LoginPage();
    DashboardPage dashboardPage = loginPage
        .openPage(Config.getBaseUrl())
        .enterUsername("user")
        .enterPassword("pass")
        .clickLogin();
    
    Assert.assertTrue(dashboardPage.isUserLoggedIn());
}
```

## Шаблон API тесту

```java
@Test(retryAnalyzer = RetryAnalyzer.class)
@Description("Опис тесту")
public void testGetUser() {
    UserApiClient client = new UserApiClient(Config.getApiBaseUrl());
    Response response = client.getUserById(1);
    
    Assert.assertEquals(response.getStatusCode(), 200);
}
```

## Шаблон Page Object

```java
public class NewPage extends BasePage {
    
    private static final By ELEMENT = By.id("element-id");
    
    public NewPage doSomething() {
        click(ELEMENT);
        return this;
    }
    
    public String getElementText() {
        return getText(ELEMENT);
    }
}
```

## Логування

```java
logger.info("Інформаційне повідомлення");
logger.warn("Попередження");
logger.error("Помилка", exception);
logger.debug("Debug інформація");
```

## Очікування

```java
// Очікувати видимість елемента
$(locator).shouldBe(Condition.visible);

// Очікувати текст
$(locator).shouldHave(Condition.text("expected text"));

// Очікувати активність
$(locator).shouldBe(Condition.enabled);

// Очікувати наявність
$(locator).shouldBe(Condition.exist);
```

## Конфіг браузерів

В `testng.xml` змінюй параметр браузера:
- `chrome` - Google Chrome
- `firefox` - Mozilla Firefox
- `edge` - Microsoft Edge
- `safari` - Safari

## Прочитати та вписати текст

```java
// Прочитати текст
String text = $(locator).getText();

// Вписати текст
$(locator).setValue("text");

// Очистити поле
$(locator).clear();
```

## Натиснення клавіші

```java
// Натиснути Enter
$(locator).pressEnter();

// Натиснути Tab
$(locator).pressTab();

// Натиснути Delete
$(locator).press(Keys.DELETE);
```

## Прокрутка

```java
// Прокрутити вниз до елемента
$(locator).scrollTo();

// Прокрутити на конкретну позицію
Selenide.executeJavaScript("window.scrollBy(0, 500)");
```

## Скриншоти

```java
// Взяти скриншот всієї сторінки
Screenshots.takeScreenShot();

// Скриншот елемента
$(locator).screenshot();
```

## Хранилище дані у testng.xml

```xml
<parameter name="browser" value="chrome"/>
<parameter name="environment" value="dev"/>
```

Доступ у тесті:
```java
@Parameters({"browser", "environment"})
@Test
public void test(String browser, String environment) {
    // використовуй browser та environment
}
```

## Allure annotations

```java
@Story("Feature Name")          // Сторія
@Description("Test description") // Опис
@Severity(SeverityLevel.BLOCKER) // Критичність
@Link("https://jira.com/...")   // Посилання
@Owner("username")              // Власник тесту
```

## Properties файли

Вибір довкілля:
```bash
./gradlew test -Denvironment=staging
```

Читання в коді:
```java
String baseUrl = Config.getProperty("base.url");
```

## Коли тест буде повторений?

Якщо тест падає, RetryAnalyzer автоматично повторить його:
- Перша спроба - падіння
- Друга спроба - повтор
- Третя спроба - повтор
- Якщо все одно падає - тест маркується як FAILED

## Allure звіти

```bash
# Генерувати звіт
./gradlew allureReport

# Результати лежать у
build/reports/allure-report/

# Відкрити звіт
./gradlew allureServe
```

## Частіші помилки

### Елемент не знайдено
- Перевір локатор
- Перевір, чи елемент видимий на сторінці
- Збільш timeout

### Timeout
- Збільш `Configuration.timeout`
- Перевір швидкість інтернету
- Перевір стан сервера

### Тест запускається, але не видит браузера
- Перевір, чи браузер встановлено
- WebDriverManager має завантажити driver автоматично

---

Успіхів у тестуванні! 🎯
