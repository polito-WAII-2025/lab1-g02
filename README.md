[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/vlo9idtn)
# lab1-wa2025


To run the project execute the following commands in order:

1) ./gradlew clean build
2) docker build -t route-analyzer .
3) docker run --rm -v ${PWD}/evaluation:/app/resources route-analyzer /app/resources/custom-parameters.yml /app/resources/waypoints_v2.csv
