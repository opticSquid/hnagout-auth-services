# Use an appropriate base image with Java and Maven pre-installed
FROM maven:3-amazoncorretto-21
# Set the working directory inside the container
WORKDIR /usr/src/app

# Copy the project's pom.xml file to the container
COPY pom.xml .

# Download the project dependencies
# RUN mvn dependency:go-offline -B

# Copy the project source code to the container
COPY src ./src

# Build the project
# skipping tests because tests depend on test container and it is not possible to install docker in a docker image
# other than that nin github test is done is another workflow file which will tell is wheather can we merge the pr or not
RUN mvn clean package -DskipTests=true

# Set the command to run the Spring Boot application
CMD ["java", "-jar", "target/hangout-auth-api-1.2.3.jar"]
