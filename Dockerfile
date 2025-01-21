# Build stage
FROM clojure:temurin-17-tools-deps-jammy AS builder

WORKDIR /build

# Copy deps.edn and download dependencies
COPY deps.edn .
RUN clojure -P

# Copy source code and build uberjar
COPY . .
RUN clojure -T:build uber

# Create a minimal runtime image
FROM eclipse-temurin:17-jre-jammy AS runtime

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /build/target/s3-tables-catalog-clj-0.1.0-SNAPSHOT-standalone.jar /app/s3-tables-catalog-clj.jar

CMD ["java", "-jar", "s3-tables-catalog-clj.jar"]