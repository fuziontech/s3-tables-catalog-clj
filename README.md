# S3 Tables Catalog Clojure Service

[![CI](https://github.com/fuziontech/s3-tables-catalog-clj/actions/workflows/ci.yml/badge.svg)](https://github.com/fuziontech/s3-tables-catalog-clj/actions/workflows/ci.yml)
[![Docker](https://github.com/fuziontech/s3-tables-catalog-clj/actions/workflows/docker.yml/badge.svg)](https://github.com/fuziontech/s3-tables-catalog-clj/actions/workflows/docker.yml)

A Clojure REST service that provides an Iceberg REST Catalog interface using AWS S3 Tables Catalog.

## Prerequisites

- Clojure CLI tools
- Java 11 or later
- AWS credentials configured (either via environment variables, AWS credentials file, or IAM role)
- Access to an S3 bucket for the warehouse location

## AWS Configuration

The service requires proper AWS configuration:

1. **Required Environment Variables:**
   ```bash
   export AWS_REGION=us-west-2  # or your preferred region
   ```

2. **AWS Credentials** (one of the following):
   - Environment variables:
     ```bash
     export AWS_ACCESS_KEY_ID=your_access_key
     export AWS_SECRET_ACCESS_KEY=your_secret_key
     ```
   - AWS credentials file (`~/.aws/credentials`):
     ```ini
     [default]
     aws_access_key_id=your_access_key
     aws_secret_access_key=your_secret_key
     ```
   - IAM role (if running on AWS infrastructure)

## Setup

Configure the warehouse location:
Edit `src/s3_tables_catalog_clj/core.clj` and update the warehouse location:
```clojure
(.initialize "s3-tables-catalog"
            {"warehouse" "s3://your-bucket/warehouse"
             "region" "us-west-2"})
```

## Development

### Using Docker Compose

The easiest way to develop is using Docker Compose:

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` with your AWS configuration:
   ```bash
   WAREHOUSE_BUCKET=s3://your-bucket/warehouse
   ```

3. Start the development environment:
   ```bash
   docker compose up
   ```

This will:
- Mount your source code for live reloading
- Cache Maven dependencies
- Mount your AWS credentials (if using `~/.aws`)
- Expose ports for the API (3000) and nREPL (9001)

You can connect your IDE to the nREPL server on port 9001 for interactive development.

### Running Locally

To start the service:
```bash
AWS_REGION=us-west-2 clj -M:run
```

The service will start on port 3000.

## Running in Production

### Using Docker

You can run the service using Docker:

```bash
docker run -p 3000:3000 \
  -e AWS_REGION=us-west-2 \
  -e AWS_ACCESS_KEY_ID=your_access_key \
  -e AWS_SECRET_ACCESS_KEY=your_secret_key \
  fuziontech/s3-tables-catalog-clj:latest
```

Or using AWS credentials file:

```bash
docker run -p 3000:3000 \
  -e AWS_REGION=us-west-2 \
  -v ~/.aws:/root/.aws \
  fuziontech/s3-tables-catalog-clj:latest
```

### Building the Docker Image Locally

To build the Docker image locally:

```bash
docker build -t s3-tables-catalog-clj .
```

## API Endpoints

### Catalog Configuration
```
GET /api/v1/config
```
Returns catalog configuration information.

### Namespaces

#### List Namespaces
```
GET /api/v1/namespaces?parent=optional.parent.namespace
```
Lists all namespaces, optionally filtered by parent namespace.

#### Create Namespace
```
POST /api/v1/namespaces
```
Creates a new namespace.

Request body:
```json
{
  "namespace": "my.namespace",
  "properties": {
    "key": "value"
  }
}
```

#### Get Namespace
```
GET /api/v1/namespaces/{namespace}
```
Gets details about a specific namespace.

### Tables

#### List Tables
```
GET /api/v1/tables?namespace=optional.namespace
```
Lists all tables, optionally filtered by namespace.

#### Create Table
```
POST /api/v1/tables
```
Creates a new table.

Request body:
```json
{
  "namespace": "my.namespace",
  "name": "my_table",
  "schema": {
    "type": "struct",
    "fields": [
      {"name": "id", "type": "long", "required": true}
    ]
  },
  "spec": {
    "fields": []
  },
  "properties": {
    "key": "value"
  }
}
```

#### Get Table
```
GET /api/v1/tables/{namespace}/{table}
```
Gets details about a specific table.

#### Drop Table
```
DELETE /api/v1/tables/{namespace}/{table}?purge=false
```
Drops a table. The `purge` parameter (default: false) determines whether to delete the underlying data.

## Building an Uberjar

To build a standalone jar:
```bash
clj -T:build uber
```

The jar will be created in `target/s3-tables-catalog-clj-0.1.0-SNAPSHOT-standalone.jar`

To run the jar:
```bash
AWS_REGION=us-west-2 java -jar target/s3-tables-catalog-clj-0.1.0-SNAPSHOT-standalone.jar
```
