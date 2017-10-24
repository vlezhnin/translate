npm --prefix=./webapp run build
echo lol
cp -rf ./webapp/build/* ./src/main/resources/
sbt package docker:publishLocal
docker tag translate:0.1-SNAPSHOT lezhnin/translate
docker push lezhnin/translate
