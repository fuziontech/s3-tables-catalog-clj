from pyspark.sql import SparkSession
import os


def main():
    # Initialize Spark with Iceberg
    spark = (SparkSession.builder
            .appName("IcebergExample")
            .config(
                "spark.sql.extensions",
                "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions"
            )
            .config(
                "spark.sql.catalog.demo",
                "org.apache.iceberg.spark.SparkCatalog"
            )
            .config(
                "spark.sql.catalog.demo.catalog-impl",
                "org.apache.iceberg.rest.RESTCatalog"
            )
            .config(
                "spark.sql.catalog.demo.uri",
                "http://s3-tables-rest-catalog:3000"
            )
            .config(
                "spark.sql.catalog.demo.warehouse",
                os.environ.get('WAREHOUSE_BUCKET')
            )
            .config(
                "spark.sql.catalog.demo.io-impl",
                "org.apache.iceberg.aws.s3.S3FileIO"
            )
            .config(
                "spark.sql.catalog.demo.s3.access-key-id",
                os.environ.get('AWS_ACCESS_KEY_ID')
            )
            .config(
                "spark.sql.catalog.demo.s3.secret-access-key",
                os.environ.get('AWS_SECRET_ACCESS_KEY')
            )
            .config(
                "spark.sql.catalog.demo.s3.region",
                os.environ.get('AWS_DEFAULT_REGION')
            )
            .getOrCreate())

    # Create example data
    data = [
        (1, "Alice", 25),
        (2, "Bob", 30),
        (3, "Charlie", 35)
    ]
    df = spark.createDataFrame(data, ["id", "name", "age"])

    # Create and write to table
    print("Creating and writing to table...")
    df.writeTo("demo.default.users").create()

    # Read from table
    print("\nReading from table:")
    result = spark.table("demo.default.users")
    result.show()

    # Add more data
    more_data = [
        (4, "David", 40),
        (5, "Eve", 45)
    ]
    more_df = spark.createDataFrame(more_data, ["id", "name", "age"])

    print("\nAppending more data...")
    more_df.writeTo("demo.default.users").append()

    # Read updated data
    print("\nReading updated table:")
    result = spark.table("demo.default.users")
    result.show()

    # Clean up
    print("\nDropping table...")
    spark.sql("DROP TABLE demo.default.users")

    spark.stop()


if __name__ == "__main__":
    main()