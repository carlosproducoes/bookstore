FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

# Mantém a porta do Spring Boot aberta
EXPOSE 8080

# Usamos o maven spring-boot:run para rodar a aplicação em modo de desenvolvimento.
# O Spring Boot DevTools vai cuidar de reiniciar o servidor.
ENTRYPOINT ["mvn", "spring-boot:run"]