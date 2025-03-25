[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/vlo9idtn)
# lab1-wa2025

To run the project with Docker execute these commands:

1) `cd RouteAnalyzer`
2) `./gradlew clean build`
3) `docker build -t route-analyzer .`
4) `docker run --rm -v ${PWD}/../evaluation:/app/evaluation route-analyzer /app/evaluation/custom-parameters.yml /app/evaluation/waypoints.csv`

To run the RouteAnalyzer app from command line in IntelliJ Idea:

` ./gradlew run --args="../evaluation/custom-parameters.yml ../evaluation/waypoints.csv"`

To run tests:

`./gradlew test`


