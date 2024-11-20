# Usa uma imagem base do Gradle com JDK 17

FROM gradle:8.10.2-jdk11 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o restante do código para o container e executa o build
COPY . /app
RUN gradle shadowJar

# # Usa uma imagem base slim do OpenJDK para a execução da aplicação
FROM openjdk:11-jdk-slim

# # Define o diretório de trabalho para a aplicação
WORKDIR /app

# # Copia o jar compilado da fase anterior para a nova imagem
COPY --from=build /app/app/build/libs/*.jar /app/kafka-streams-app.jar

# # Comando para executar a aplicação Kafka Streams
CMD ["java", "-jar", "kafka-streams-app.jar"]
