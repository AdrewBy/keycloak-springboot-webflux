
# Указываем базовый образ с нужной версией JDK (в данном случае Java 21)
FROM bellsoft/liberica-openjdk-alpine:21.0.1

# Желательно запускать приложения не от имени суперпользователя, который
# используется по умолчанию, поэтому нужно создать пользователя и группу
# для запуска приложения.
RUN addgroup -S spring-boot-group && adduser -S -G spring-boot-group spring-boot
USER spring-boot:spring-boot-group


# Иногда требуется получить доступ к файлам, генерирующимся в процессе выполнения,
# для этого зарегистрируем том /tmp
VOLUME /tmp

# Со временем у проекта будет изменяться версия, и чтобы не изменять всякий раз
# этот Dockerfile имя jar-файла вынесем в аргумент. Альтернативно можно указать
# постоянное имя jar-файла в Maven при помощи finalName.
ARG JAR_FILE=spring-security-keycloak-api-1.0.0.jar

WORKDIR /application

# Скопируем "толстый" JAR
COPY target/${JAR_FILE} application.jar

# В конце укажем точку входа. Выбран вариант с использованием exec для того, чтобы
# можно было передать в строку запуска дополнительные параметры запуска - JAVA_OPTS, а так же
# ${0} и ${@} для передачи аргументов запуска.
ENTRYPOINT exec java ${JAVA_OPTS} -jar application.jar ${0} ${@}