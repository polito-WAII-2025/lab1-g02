[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/vlo9idtn)
# lab1-wa2025


To run the project execute these commands:

1) `./gradlew clean build`
2) `docker build -t route-analyzer .`
3) `docker run --rm -v ${PWD}/src/main/resources:/app/resources route-analyzer /app/resources/yml/custom-parameters.yml /app/resources/csv/waypoints_v2.csv./gradlew run --args=".\src\main\resources\yml\custom-parameters.yml .\src\main\resources\csv\waypoints_v2.csv"`


**Note:** To run the RouteAnalyzer app from command line in IntelliJ Idea
`./gradlew run --args=".\src\main\resources\yml\custom-parameters.yml .\src\main\resources\csv\waypoints_v2.csv"`
