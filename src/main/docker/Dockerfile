FROM adoptopenjdk/openjdk11:jre
MAINTAINER i@raysmond.com
RUN mkdir /app
WORKDIR /app
RUN ulimit -c unlimited
#COPY ./shield.jar /app
COPY ./sources.list /etc/apt/
ENV TZ=/Asia/Shanghai
RUN apt-get update  && apt-get install -y tzdata
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN dpkg-reconfigure --frontend noninteractive tzdata
EXPOSE 8080
CMD java -Xmx512m -Xms256m -jar /app/shield.jar
