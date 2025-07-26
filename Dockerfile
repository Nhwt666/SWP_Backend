FROM eclipse-temurin:24-jdk-alpine

# Thiết lập thư mục làm việc trong container
WORKDIR /app

# Sao chép pom.xml trước để tận dụng cache của Docker
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Cấp quyền thực thi cho file mvnw
RUN chmod +x ./mvnw

# Tải các dependencies
RUN ./mvnw dependency:go-offline -B

# Sao chép mã nguồn
COPY src ./src

# Build ứng dụng
RUN ./mvnw package -DskipTests

# Mở cổng 8080
EXPOSE 8080

# Chạy ứng dụng (sử dụng wildcard để tự động tìm file JAR)
CMD java -jar target/*.jar