FROM ubuntu:26.04

# Avoid prompts from apt
ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies
RUN apt-get update && apt-get install -y \
    graphicsmagick \
    graphicsmagick-libmagick-dev-compat \
    openjdk-17-jdk \
    wget \
    curl \
    && rm -rf /var/lib/apt/lists/*

RUN apt-get install -y tesseract-ocr tesseract-ocr-kor tesseract-ocr-chi-sim \
    tesseract-ocr-chi-tra tesseract-ocr-jpn fonts-dejavu-core

# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$PATH:$JAVA_HOME/bin

# Create app directory
WORKDIR /app

# Copy jar file
COPY target/imgFlux-download-api-*.jar app.jar

# Copy entrypoint script
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Expose port
EXPOSE 8082

# Run entrypoint
ENTRYPOINT ["/app/entrypoint.sh"]
