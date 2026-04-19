
# BASE IMAGE
# Eclipse Temurin JDK 17 on Ubuntu Jammy as the base image for our application.
FROM eclipse-temurin:17-jdk-jammy

# 2 set up the environment
# Set the working directory inside the container to /app, where the application code will reside.
WORKDIR /app

# 3 copy the necessary files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY api/build.gradle.kts api/
COPY converter/build.gradle.kts converter/


#permissions
RUN chmod +x gradlew

# 4 verify the Gradle setup
RUN ./gradlew --version

#copy the rest of the application code into the container.
COPY . .

# 5 build the application
RUN ./gradlew classes -x test --no-daemon

# 6 set the entry point
ENTRYPOINT ["sh", "-c", "./gradlew :converter:run --quiet --no-daemon --args=\"$*\"", "--"]