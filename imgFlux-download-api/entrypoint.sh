#!/bin/bash

# Set default values for environment variables if not set
APP_LOG_PATH=/app/logs
export JVM_MEMORY=${JVM_MEMORY:-"-Xms512m -Xmx1024m"}
export JAVA_OPTS="$JAVA_OPTS \
        -server \
        --add-opens=java.base/java.io=ALL-UNNAMED \
        -Dspring.profiles.active=${SPRING_PROFILE} \
        ${JVM_MEMORY} \
        ${PROXY_PROPERTIES} \
        -Dcom.sun.management.jmxremote \
        -Dcom.sun.management.jmxremote.port=11619 \
        -Dcom.sun.management.jmxremote.ssl=false \
        -Dcom.sun.management.jmxremote.authenticate=false \
        -Djava.rmi.server.hostname=${__POD_IP} \
        -Dcmdb.hostname=${__POD_NAME} \
        -Dcom.sun.management.jmxremote.rmi.port=11619 \
        -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1HeapRegionSize=8m -XX:+ParallelRefProcEnabled -XX:-ResizePLAB \
        -verbose:gc -Xlog:gc:${APP_LOG_PATH}/gc/gc.`date '+%Y%m%d%H%M'`.log -Xlog:gc* \
        -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${APP_LOG_PATH}/gc/heapdump_`date '+%Y%m%d%H%M'`.hprof"
export SERVER_PORT=${SERVER_PORT:-8080}

# Print configuration
echo "Starting imgFlux Admin UI..."
echo "JAVA_OPTS: $JAVA_OPTS"
echo "SERVER_PORT: $SERVER_PORT"

# Start the application
exec java $JAVA_OPTS -jar /app/app.jar --server.port=$SERVER_PORT


