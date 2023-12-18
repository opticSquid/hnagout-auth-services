# Use an appropriate base image with Java and Maven pre-installed
FROM maven:3-amazoncorretto-17
# Set the working directory inside the container
WORKDIR /usr/src/app

# Copy the project's pom.xml file to the container
COPY pom.xml .

# Download the project dependencies
RUN mvn dependency:go-offline -B

# Copy the project source code to the container
COPY src ./src

# Build the project
RUN mvn package -DskipTests 

# Set the command to run the Spring Boot application
CMD ["java", "-jar", "target/Users-0.0.1.jar"]
