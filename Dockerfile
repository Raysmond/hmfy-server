FROM adoptopenjdk/openjdk11:alpine-jre
MAINTAINER i@raysmond.com
RUN mkdir /app
WORKDIR /app
RUN ulimit -c unlimited
COPY ./shield.jar /app
RUN apk add --no-cache tzdata
ENV TZ=/Asia/Shanghai
CMD while : ; do date; sleep 5; done
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
EXPOSE 8080
EXPOSE 9981
CMD java -Xmx512m -Xms256m -jar /app/shield.jar
