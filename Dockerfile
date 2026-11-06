# image: eclipse-temurin:25-jre
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY build/libs/*.jar app.jar

# When it is run, the container will execute the command below. The command will start the Java application with any additional options specified in the JAVA_OPTS environment variable.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]