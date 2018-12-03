FROM sgrio/java-oracle:jdk_10
MAINTAINER Daniel Marchena (danielmapar@gmail.com)
RUN apt-get update
RUN apt-get install -y maven
COPY pom.xml /usr/local/service/pom.xml
COPY src /usr/local/service/src
WORKDIR /usr/local/service
RUN mvn package
CMD ["java","-jar","target/eeg-brainwave-api-1.0-SNAPSHOT.jar"]
