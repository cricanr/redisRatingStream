Redis Rating Stream application to calculate stats for Movies
=====
TODO

Description and architecture used
-------
TODO

Future improvements suggestions
-------
TODO

How to run it
-------
Prerequisites

1) Start dependencies (downstream services) first:
`docker-compose up`

Install: 
Linux:
  -  java 
  -  stb
  - docker  
### Java - Run Time Environment (JRE)
```bash
  sudo apt-get update
  apt install openjdk-8-jre-headless
  java -version
```  
######Output
```bash
openjdk version "1.8.0_181"
OpenJDK Runtime Environment (build 1.8.0_181-8u181-b13-0ubuntu0.16.04.1-b13)
OpenJDK 64-Bit Server VM (build 25.181-b13, mixed mode)
```


### sbt - Define your tasks in Scala. Run them in parallel from sbt's interactive shell.
docs: [click here](https://www.scala-sbt.org/1.0/docs/Installing-sbt-on-Linux.html)
```bash
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
sudo apt-get update
sudo apt-get install sbt
```

### Docker
```bash
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get update
apt-cache policy docker-ce
```

Running the application:
Locally (without docker)
1) Using SBT terminal to prepare the `svc` dir ([click here](https://medium.com/@shatil/play-framework-https-hello-world-with-docker-62963cf26daf)) 
```sbtshell
sbt clean 
sbt compile
sbt test
sbt run
```

In docker container: 
1) Create artifact/package of app: 
```sbtshell
sbt dist
```

Move package artifact
```sbtshell
set -x && unzip -d svc target/universal/*-1.0.zip && mv svc/*/* svc/ && rm svc/bin/*.bat && mv svc/bin/* svc/bin/start
```

2) Create .env file based on example .env.dist
 do not commit in the repository the file .env ; update the .gitignore 
 
3) Create docker-compose.yml ([click here](https://docs.docker.com/compose/compose-file/compose-versioning/))
```yaml
#version: "3.3"
#services:
#  app:
#    build: ./
#    environment:
#      - REDIS_HOST=redis
#      - RHOST=redis
#    networks:
#      - redis-net
#    depends_on:
#      - redis
#    ports:
#      - 9000:9443
#    command: /svc/bin/start -Dhttps.port=9443 -Dredis.ip="$REDIS_IP" -Dplay.crypto.secret=secret

  redis:
    image: redis
    command: ["redis-server", "--appendonly", "yes"]
    hostname: redis
    networks:
      - redis-net
    volumes:
      - redis-data:/data
    ports:
      - 6379:6379

networks:
  redis-net:

volumes:
  redis-data:

```
5) Create Dockerfile
Check `Dockerfile` code

6) Start dependencies (downstream services) first:
```bash
docker-compose up --force-recreate --build
```
--build => will build first
--force-recreate => will remove cache
up => docker-compose.yml then Dockerfile

or 
```
docker-compose up
```

Start the Scala play app: (when finished docker integration, this should no longer be needed) 
```sbtshell
sbt run
```
NOTE: Because a problem with starting redisStream Akka service when Play app is running also in the `docker-compose` environment 
I have removed it for now from docker-compose and we need to run the Play app separately. To be fixed-forward.

7) Run in Chrome: http://localhost:9000/ratingInfo/364689 (or another movieId) and you should get something like this: 
```{
   "ratingAverage": {
        "id": 364689,
        "ratingAverage": 9.049766
   },
   "isBellowMedianAverage": false
   }
```

Or

http://localhost:9000/ratingInfos/movieIds=680,23&limit=300
Output: 
```[
   {
   "ratingAverage": {
   "id": 680,
   "ratingAverage": 5.129987
   },
   "isBellowMedianAverage": false
   },
   {
   "ratingAverage": {
   "id": 23,
   "ratingAverage": -1
   },
   "isBellowMedianAverage": true
   }
   ]```
   
Voila! Your app runs correctly!

You can also run and debug using IntelliJ if you want.

Useful link: 
--------------
https://github.com/Tapad/sbt-docker-compose/commit/4022a67d3a83441d71994ba14fa4a6139ccc735d#diff-3e4674dca13d884cdb52705d4b2c0f49
