FROM sgrio/java-oracle:jdk_10
MAINTAINER Daniel Marchena (danielmapar@gmail.com)

ENV DB_HOST eeg-db
ENV DB_USERNAME adaptive_user
ENV DB_PASSWORD adaptive_pw
ENV DB_NAME adaptive_db
ENV DB_PORT 3307
ENV ENV docker
ENV MODEL_PATH /resources/model.bin
ENV DATABASE_UTILS_PATH /resources/DatabaseUtils.props
ENV DEBUG_SECRET adaptive_system

RUN apt-get update
RUN apt-get install -y maven
RUN mkdir /resources
COPY src/main/resources/DatabaseUtils.props /resources/DatabaseUtils.props
COPY src/main/resources/model.bin /resources/model.bin
COPY pom.xml /usr/local/service/pom.xml
COPY src /usr/local/service/src
COPY wait-for-it.sh /usr/local/service/wait-for-it.sh
WORKDIR /usr/local/service
RUN mvn package

ENTRYPOINT chmod 777 ./wait-for-it.sh && ./wait-for-it.sh -t 40 eeg-db:3307 && java -jar target/eeg-brainwave-api-1.0-SNAPSHOT.jar
