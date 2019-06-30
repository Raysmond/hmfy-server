mv shield.jar shield.bak.jar
mv shield-0.0.1-SNAPSHOT.jar shield.jar

sudo docker-compose -f app.yml stop shield-app

sudo docker-compose -f app.yml rm -f shield-app

sudo docker rmi shield:latest
sudo docker build -t shield .

sudo docker-compose -f app.yml up -d shield-app
