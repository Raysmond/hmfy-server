
./gradlew -Pprod clean bootJar

scp -P 2233 build/libs/shield-0.0.1-SNAPSHOT.jar zou@116.247.114.12:/home/zou/shield/docker/
