# Build stage
FROM clojure:temurin-17-tools-deps-jammy AS builder

WORKDIR /build

# Copy dependencies first to leverage Docker caching
COPY deps.edn .
RUN clojure -P

# Copy source code
COPY . .

# Build uberjar
RUN clojure -T:build uber

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy uberjar from builder stage
COPY --from=builder /build/target/s3-tables-catalog-clj-0.1.0-SNAPSHOT-standalone.jar ./app.jar

# Set environment variables
ENV AWS_REGION=us-west-2

# Expose the service port
EXPOSE 3000

# Run the service
CMD ["java", "-jar", "app.jar"]