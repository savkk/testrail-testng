# Интеграция TestRail и TestNG

Для сопоставления тестов из TestRail и автотестов используется аннотация `@io.qameta.allure.TmsLink`

## Подключение к проекту ##

```xml
<dependency>
	<groupId>com.github.savkk</groupId>
	<artifactId>testrail-testng</artifactId>
	<version>1.0.0</version>
</dependency>
```

Также необходимо добавить репозиторий:

```xml
<repositories>
    <repository>
        <id>com.github.savkk</id>
        <url>https://dl.bintray.com/savkkkk/maven/</url>
    </repository>
</repositories>
```

### Пример: ###

```java
    @Test
    @TmsLink("15014467")
    public void someTest() {
        ...
    }
```

## Настройки ##

| Ключ       | Назначение     | 
| :------------- | :----------: |
| testrail.enabled | включить интеграцию |
| tests.package | пакет, в котором расположены автотесты |
| parallel.mode | режим параллельного запуска автотестов (TESTS, METHODS, CLASSES, INSTANCES, NONE). По-умолчанию NONE|
| parallel.thread.count | количество потоков при параллельном запуске |
| parallel.data_provider_thread.count | количество потоков для Data Provider при параллельном запуске |
| testrail.url |  хост TestRail |
| testrail.user | имя пользователя в TestRail |
| testrail.password | пароль в TestRail |
| testrail.assignedto_id | id пользователя на которого в TestRail будет назначен тест. Не обязательный параметр. |
| testrail.run_id | id тест рана |
| testrail.test_ids | id тестов из тест рана разделенные запятой. Если id не указаны, то будет осуществлен запуск всех тестов из тест рана |

Любую из настроек можно передать через System.properties, System.env или запись в testrail.properties, который необходимо разместить в classpath.
