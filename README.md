# S3 Tables Catalog Clojure Service

[![CI](https://github.com/james/s3-tables-catalog-clj/actions/workflows/ci.yml/badge.svg)](https://github.com/james/s3-tables-catalog-clj/actions/workflows/ci.yml)

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

## Running the Service

To start the service:
```bash
AWS_REGION=us-west-2 clj -M:run
```

The service will start on port 3000.

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
