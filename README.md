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
```bash
docker-compose up
```

2) Start Redis server locally:
Install Redis locally: https://redis.io/topics/quickstart
Terminal -> go to path of downloaded Redis folder (e.g.)
```bash
redis-server
```
TODO: This needs to be improved by making the entire app run inside docker. Redis should be installed and ran
inside the docker container so that by running one command we start the app and its dependencies.

Using SBT terminal using:
 
```sbtshell
sbt clean 
sbt compile
sbt test
sbt run
```

Run in Chrome: http://localhost:9000/ratingInfo/364689 (or another movieId) and you should get something like this: 
```{
   "ratingAverage": {
   "id": 364689,
   "ratingAverage": 9.049766
   },
   "isBellowMedianAverage": false
   }```

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
TODO
