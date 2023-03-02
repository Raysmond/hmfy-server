mv shield.jar shield.bak.jar
app=shield-$1-SNAPSHOT.jar
cp ../releases/$app .
cp $app shield.jar

echo "start to build ${app} image..."
#docker rmi shield:$1
docker build -t shield:$1 .


now="`date +%Y%m%d%H%M%S`"
docker-compose -f app.yml logs shield-app > ../logs/logs_$now
docker-compose -f app.yml stop shield-app
docker-compose -f app.yml rm -f shield-app

#docker rmi shield:old
#docker tag shield:latest shield:old
docker rmi shield:latest
docker tag shield:$1 shield:latest

docker-compose -f app.yml up -d shield-app
