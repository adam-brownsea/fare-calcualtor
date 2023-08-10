FROM amazoncorretto:17.0.7-alpine
WORKDIR /app
ARG JAR_FILE
ENV CLASSPATH=app.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]