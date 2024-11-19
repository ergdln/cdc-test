# # Usa uma imagem base do Maven com JDK 17 (imagem atualizada)
# FROM maven:3.9.4-eclipse-temurin-17 AS build

# # Define o diretório de trabalho dentro do container
# WORKDIR /app

# # Copia o arquivo pom.xml e baixa as dependências (cache eficiente)
# COPY pom.xml /app
# RUN mvn dependency:resolve

# # Copia o restante do código para o container e executa o build
# COPY . /app
# RUN mvn clean package -DskipTests

# # Usa uma imagem base slim do OpenJDK para a execução da aplicação
# FROM openjdk:17-jdk-slim

# # Define o diretório de trabalho para a aplicação
# WORKDIR /app

# # Copia o jar compilado da fase anterior para a nova imagem
# COPY --from=build /app/target/kafka-streams-app-1.0-SNAPSHOT-jar-with-dependencies.jar /app/kafka-streams-app.jar

# # Comando para executar a aplicação Kafka Streams
# CMD ["java", "-jar", "/app/kafka-streams-app.jar"]

# Usa uma imagem base do Gradle com JDK 17

FROM gradle:7.6.0-jdk17 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia os arquivos de configuração do Gradle (constrói o cache de dependências)
COPY build.gradle settings.gradle /app/
RUN gradle build -i --no-daemon || return 0

# Copia o restante do código para o container e executa o build
COPY . /app
RUN gradle clean build -x test --no-daemon

# Usa uma imagem base slim do OpenJDK para a execução da aplicação
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho para a aplicação
WORKDIR /app

# Copia o jar compilado da fase anterior para a nova imagem
COPY --from=build /app/build/libs/*.jar /app/kafka-streams-app.jar

# Comando para executar a aplicação Kafka Streams
CMD ["java", "-jar", "/app/kafka-streams-app.jar"]
