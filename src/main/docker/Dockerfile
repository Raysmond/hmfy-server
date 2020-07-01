FROM adoptopenjdk/openjdk8:jre
MAINTAINER i@raysmond.com
RUN mkdir /app
WORKDIR /app
RUN ulimit -c unlimited
COPY ./sources.list /etc/apt/
ENV TZ=Asia/Shanghai
RUN rm -f /etc/localtime
RUN rm -rf  /usr/share/zoneinfo/UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt-get update  && apt-get install -y tzdata
RUN dpkg-reconfigure --frontend noninteractive tzdata
RUN apt-get clean && apt-get autoclean
EXPOSE 9981
EXPOSE 8080
#COPY ./shield.jar /app
#CMD java -Xmx512m -Xms256m -jar /app/shield.jar
