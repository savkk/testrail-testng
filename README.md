# Интеграция TestRail и TestNG
Один из вариантов интеграции TestRail и тестов, запускаемых при помощи TestNG.
Для сопоставления тестов из TestRail и автотестов используется аннотация `@io.qameta.allure.TmsLink`

Для запуска используется метод `main`, определенный в классе `TestNGRunner`. В pom-файле приведен пример профиля `testrail` запускающего данный метод из maven.
В качестве входного параметра должен прийти `id` Test Run'а:

`mvn clean process-test-classes -Ptestrail -DrunId=1234`

Если дополнительно указать параметр `testIds`, то из указанного Test Run'a будут запущены только тесты с указанными id:

`mvn clean process-test-classes -Ptestrail -DrunId=1234 "-DtestIds=29878661,29878601"`
