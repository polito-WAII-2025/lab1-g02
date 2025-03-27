[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/vlo9idtn)
# lab1-wa2025 (Group-02)
## Steps to run the project
To run the project with Docker execute these commands:

1) `cd RouteAnalyzer`
2) `./gradlew clean build`
3) `docker build -t route-analyzer .`
4) `docker run --rm -v ${PWD}/../evaluation:/app/evaluation route-analyzer /app/evaluation/custom-parameters.yml /app/evaluation/waypoints.csv`

To run the RouteAnalyzer app from command line in IntelliJ Idea:

` ./gradlew run --args="../evaluation/custom-parameters.yml ../evaluation/waypoints.csv"`

To run tests:

`./gradlew test`

## Requested Functions and Features

1. **Maximum Distance from Start**
```Kotlin 
fun maxDistanceFromStart(waypoints: List<WayPoint>, earthRadiusKm: Double):  Pair<Double, WayPoint>
```
Calculates the farthest distance from the starting point. The starting point is the first entry in the list of waypoints.

**Parameters:**

- `waypoints: List<WayPoint>`:  The list of waypoints.
- `earthRadiusKm: Double`: The Earth's radius in kilometers.

**Return Value:**

- `Pair<Double, WayPoint>`: A pair containing:
  - `Double`: The maximum distance computed from the starting point.
  - `WayPoint`: The waypoint corresponding to the maximum distance.


2. **Most Frequented Area**

```kotlin
fun mostFrequentedArea(list: List<WayPoint>, mostFrequentedAreaRadiusKm: Double): Pair<WayPoint, Long>?
```
Identifies the most frequented area within a given radius.

**Parameters:**
- `list: List<WayPoint>`: A list of waypoints.
- `mostFrequentedAreaRadiusKm: Double`: The radius in kilometers used to define the area of interest.

**Return Value:**

- `Pair<WayPoint, Long>?`: A pair containing:
  - `WayPoint`: The center point of the most frequented area.
  - `Long:` The number of waypoints within that area.
    `null` if the list is empty.

3. **Waypoints Outside Geofence**

```kotlin
fun waypointsOutsideGeofence(centre: WayPoint, radius: Double, listOfWayPoints: List<WayPoint>, earthRadiusKm: Double): List<WayPoint>
```
Filters the waypoints located outside a given geofence.

**Parameters:**

- `centre: WayPoint`: The center point of the geofence.
- `radius: Double`: The radius of the geofence in kilometers.
- `listOfWayPoints: List<WayPoint> `: The list of waypoints.
- `earthRadiusKm: Double`: The Earth's radius in kilometers.

**Return Value:**

- `List<WayPoint> `: A list containing all waypoints that are outside the geofence.


## Extra Feature
1. **Least Frequented Area**

```kotlin
fun leastFrequentedArea(list: List<WayPoint>, mostFrequentedAreaRadiusKm: Double): Pair<WayPoint, Long>?
```
Identifies the least frequented area within a given radius.

**Parameters:**

- `list: List<WayPoint>`: A list of waypoints.
- `mostFrequentedAreaRadiusKm: Double`: The radius in kilometers used to define the area of interest.

**Return Value:**

- `Pair<WayPoint, Long>?`: A pair containing:
  - `WayPoint`: The center point of the least frequented area.
  - `Long`: The number of waypoints within the least frequented area.
  `null` if the list is empty.
## Output

- `output.json`: Stores results for max distance, most frequented area, and waypoints outside geofence.

- `output_advanced.json`: Stores results for least frequented area.



