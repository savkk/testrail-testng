# Интеграция TestRail и TestNG
Один из вариантов интеграции TestRail и тестов, запускаемых при помощи TestNG.
Для сопоставления тестов из TestRail и автотестов используется аннотация `@io.qameta.allure.TmsLink`

Для запуска используется метод `main`, определенный в классе `TestNGRunner`. В pom-файле приведен пример профиля `testrail` запускающего данный метод из maven.
В качестве входного параметра должен прийти `id` Test Run'а:

`mvn clean test -Ptestrail -DrunId=1234`