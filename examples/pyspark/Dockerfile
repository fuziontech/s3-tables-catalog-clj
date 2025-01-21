FROM bitnami/spark:3.4.1

USER root

# Install system dependencies
RUN apt-get update && apt-get install -y wget curl && rm -rf /var/lib/apt/lists/*

# Install Python dependencies
RUN pip install --no-cache-dir \
  pyiceberg==0.5.1 \
  boto3

# Copy the example script
COPY iceberg_example.py /app/iceberg_example.py

# Set working directory
WORKDIR /app

# Download Iceberg and AWS dependencies
RUN wget https://repo1.maven.org/maven2/org/apache/iceberg/iceberg-spark-runtime-3.4_2.12/1.4.3/iceberg-spark-runtime-3.4_2.12-1.4.3.jar -P /opt/bitnami/spark/jars/ && \
  wget https://repo1.maven.org/maven2/software/amazon/awssdk/bundle/2.21.42/bundle-2.21.42.jar -P /opt/bitnami/spark/jars/ && \
  wget https://repo1.maven.org/maven2/software/amazon/awssdk/url-connection-client/2.21.42/url-connection-client-2.21.42.jar -P /opt/bitnami/spark/jars/

# Set the entrypoint
ENTRYPOINT ["spark-submit", "--master", "local[*]", "iceberg_example.py"]