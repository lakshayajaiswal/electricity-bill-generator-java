# Use official Eclipse Temurin Java 17 JDK
FROM eclipse-temurin:17-jdk

# Set application working directory inside container
WORKDIR /app

# Copy backend source code and frontend static assets
COPY backend/ ./backend/
COPY frontend/ ./frontend/

# Compile Java backend
RUN javac backend/Main.java

# Render dynamically sets the PORT environment variable
ENV PORT=8080
EXPOSE 8080

# Launch the Java server
CMD ["java", "-cp", "backend", "Main"]
