FROM openjdk:11.0.12-slim

#Installing Redis-cli with tls
RUN apt update; apt install --quiet --yes libssl-dev wget build-essential;
RUN wget http://download.redis.io/redis-stable.tar.gz; tar xvzf redis-stable.tar.gz;
WORKDIR /redis-stable
RUN make distclean
RUN make redis-cli BUILD_TLS=yes
RUN install -m 755 src/redis-cli /usr/local/bin/

#Installing and starting the application
WORKDIR /application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
ENTRYPOINT ["java","-jar","application.jar"]
EXPOSE 8080