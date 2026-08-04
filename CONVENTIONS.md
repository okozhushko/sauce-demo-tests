# 📋 Project Conventions - CLAUDE.md

Документація проекту `java-test-framework` для розробників та AI помічників

## 📖 Про проект

**java-test-framework** — це скелет масштабованого test automation framework для:
- 🌐 **UI тестування** (Selenide на Chrome, Firefox, Edge, Safari)
- 🔌 **API тестування** (REST Assured)
- 📦 **Page Object паттерн** для чистого коду
- ⚡ **Паралельне виконання** тестів
- 🔄 **Retry механізм** для нестійких тестів
- 📊 **Allure Reports** для красивих звітів
- 🚀 **CI/CD** через GitHub Actions

## 🎯 Вимоги проекту

### Java версія
- **JDK 17 або новіша**
- Gradle для збудування

### Основні залежності
- **TestNG 7.8.1** — фреймворк тестування
- **Selenide 7.2.0** — UI automation
- **REST Assured 5.4.0** — API testing
- **Allure 2.25.0** — звітування
- **SLF4J + Logback** — логування

## 📁 Структура папок

```
app/src/
├── main/java/com/example/
│   ├── base/              # Базові класи для всіх тестів
│   ├── pages/             # Page Objects для UI тестів
│   ├── api/              # API clients та моделі
│   ├── config/           # Конфігурація фреймворку
│   ├── listeners/        # TestNG listeners
│   ├── utils/            # Утилітарні класи
│   └── retry/            # Retry анализатор
└── test/java/com/example/
    ├── ui/               # UI тести
    └── api/              # API тести

src/test/resources/
├── properties/           # Конфіг файли (dev, staging, prod)
├── testdata/            # Тестові дані (JSON, CSV)
└── testng/              # TestNG конфіги (testng.xml)

.github/workflows/       # GitHub Actions pipelines
```

## 🏗️ Архітектура

### Page Object Pattern
Кожна сторінка має свій клас, що успадковує `BasePage`:
- **Локатори** — приватні статичні финальные поля `By`
- **Методи** — публічні методи для взаємодії
- **Ланцюги** — методи повертають `this` або нову Page об'єкт для fluent interface

### API Client
Всі API клієнти успадковують `ApiClient`:
- **given()** — створює RequestSpecification з логуванням
- **get/post/put/delete()** — методи для запитів

## 🧪 Правила написання тестів

### Іменування тестів
- **Метод**: `test<WhatIsBeingTested><ExpectedResult>`
- Приклад: `testValidLoginSuccess()`, `testInvalidCredentialsShowsError()`

### Структура тесту (AAA Pattern)
1. **Arrange** — підготовка даних та об'єктів
2. **Act** — виконання тесту
3. **Assert** — перевірка результату

## ⚙️ Конфігурація

### Environments
- **dev** — розробка (http://localhost:3000)
- **staging** — staging сервер
- **production** — production сервер

Вибір: `./gradlew test -Denvironment=staging`

### Браузери
Визначаються в `testng.xml` параметром `browser`:
- `chrome` — Google Chrome
- `firefox` — Mozilla Firefox
- `edge` — Microsoft Edge
- `safari` — Apple Safari

## 💡 Best Practices

### Page Objects
✅ DO:
- Кожна сторінка — окремий клас
- Локатори як приватні поля
- Методи взаємодії — публічні
- Fluent interface (return this)

❌ DON'T:
- Assert у Page Object методах
- Магічні числа (використовуй Config)
- Дублювати локатори

### Тести
✅ DO:
- Один assert на тест або логічно пов'язані
- Використовувати @BeforeMethod/@AfterMethod
- Параметризувати тести де можливо
- Додавати Allure annotations

❌ DON'T:
- Залежності між тестами
- Hard-coded дані
- Дивні delay (sleep)
- Ігнорувати exception

## 📝 Commit messages

Conventional commits формат:
```
feat(pages): add new LoginPage
fix(api): fix user endpoint parsing
test(ui): add login scenario
docs: update README
refactor(base): simplify BasePage
```

## 📚 Документація

- **TEST_FRAMEWORK_BLUEPRINT.md** — основна документація
- **QUICK_REFERENCE.md** — швидкий довідник
- **CONVENTIONS.md** — цей файл

---

**Версія**: 1.0 | **Дата**: 2026-08-04
