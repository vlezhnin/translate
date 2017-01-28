sbt package docker:publishLocal
docker tag translate:0.1-SNAPSHOT lezhnin/translate
docker push lezhnin/translate
