FROM eclipse-temurin:24-jdk

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

COPY src ./src
COPY scripts ./scripts  # nếu bạn cần

RUN ./mvnw package -DskipTests

# In ra file JAR để chắc chắn tồn tại
RUN ls -lah target

# Dùng tên JAR chính xác thay vì wildcard
CMD ["java", "-jar", "target/BE_Final_V2-0.0.1-SNAPSHOT.jar"]
