import axios from 'axios';
import * as L from 'leaflet';
import {Circle, MapContainer, Marker, TileLayer, useMap, useMapEvents} from "react-leaflet";
import {useEffect, useState} from "react";
import 'leaflet/dist/leaflet.css';
import {LatLngTuple} from "leaflet";

type Coordinate = [number, number];
type Waypoint = {lat: number, lng: number, time: number};

const API_KEY = "";

type RouteResponse = {
  coordinates: Coordinate[];
  speed: number;
  error?: Error;
}

async function getRoute(apiKey: string, start: Coordinate, end: Coordinate): Promise<RouteResponse> {
  const url = `https://api.openrouteservice.org/v2/directions/driving-car/geojson`;
  try {
    const response = await axios.post(url, {
      coordinates: [start, end],
      instructions: false,
    }, {
      headers: {
        Authorization: apiKey,
        "Content-Type": "application/json",
      }
    });
    console.log(response.data);
    return {
      coordinates: response.data.features[0].geometry.coordinates,
      speed: response.data.features[0].properties.summary.distance / response.data.features[0].properties.summary.duration,
    };
  } catch (e) {
    return {coordinates: [], speed: 0, error: e as Error};
  }
}

function computeIntermediatePoints(route: Coordinate[], speed: number, startTime: number = Date.now(), wayToGo:number = 2000 ): Waypoint[] {
  const waypoints = [];
  let currentTime = startTime;
  let residualDistance  = 0;
  for (let i = 0; i < route.length - 1; i++) {
    let [lng1, lat1] = route[i];
    if (i==0) {
      waypoints.push({lat: lat1, lng: lng1, time: currentTime});
    }
    const [lng2, lat2] = route[i + 1];
    let distance = L.latLng(lat1, lng1).distanceTo(L.latLng(lat2, lng2));
    while (distance > wayToGo-residualDistance ) {
      lat1 += (wayToGo-residualDistance) / distance * (lat2 - lat1);
      lng1 += (wayToGo-residualDistance) / distance * (lng2 - lng1);
      currentTime += wayToGo/speed;
      waypoints.push({lat: lat1, lng: lng1, time: currentTime});
      distance = L.latLng(lat1, lng1).distanceTo(L.latLng(lat2, lng2));
      residualDistance = 0;
    }
    residualDistance += distance;
  }
  waypoints.push({lat: route[route.length-1][1], lng: route[route.length-1][0], time: currentTime+residualDistance/speed});
  return waypoints;
}

type RouteProps = {
  markers: LatLngTuple[];
  waypoints: Waypoint[]
  onClick: (event: L.LeafletMouseEvent) => void;
}

function Route({markers, waypoints, onClick}: RouteProps) {
  const map = useMap();
  useMapEvents( {click: onClick} )
  if (markers.length > 1)
    map.fitBounds(markers,{padding: [30, 30]});
  return (
    <>
      {markers.map((marker, index) => (
        <Marker key={index} position={marker} />
      ))}
      {waypoints.map((waypoint, index) => (
        <Circle key={index} center={waypoint} radius={10}/>
      ))}
    </>
  );
}

type SidePanelProps = {
  markers: LatLngTuple[];
  setMarkers: (markers: LatLngTuple[]) => void;
  clearMarkers: () => void;
  waypoints: Waypoint[];
  wayToGo: number;
  setWayToGo: (wayToGo: number) => void;
  apiKey: string;
  setApiKey: (apiKey: string) => void;
  error?: Error;
  setError: (error: Error|undefined) => void;
}

function SidePanel(
  {
    markers, setMarkers, clearMarkers,
    waypoints, wayToGo, setWayToGo,
    apiKey, setApiKey,
    error, setError
  }: SidePanelProps) {
  return (
    <div style={{flex: 1, display: "flex", flexDirection: "column", padding: "0.5em"}}>
      <div style={{flex: 0, display: "flex", flexDirection: "column", padding: "0.5em"}}>
        <h4 style={{marginBottom: 0}}>API Key</h4>
        <a href="https://api.openrouteservice.org" target="_blank">Documentation</a>
        <input type="text" value={apiKey} onChange={(e) => {
          setError(undefined);
          setApiKey(e.target.value)}}/>
      </div>
      {error ? <div style={{color: "red", padding:"1em"}}>{error.message}</div>:
        <>
          <h4 style={{marginBottom: 0}}>Stops</h4>
          <ol>
            {markers.map((m, index) => (
              <li key={index}>
                ({m[0].toFixed(2)}, {m[1].toFixed(2)})
                <button style={{padding: "0 0.25em", marginLeft: "0.5em", backgroundColor: "#c0c0c0"}}
                        onClick={
                          () => setMarkers(
                            markers.filter((_, j) =>
                              index != j))
                        }>x
                </button>
              </li>
            ))}
          </ol>
          <div style={{paddingLeft: "1em"}}>
            <button onClick={clearMarkers}>Clear</button>
          </div>
          <hr style={{width: "100%"}}/>
          <div>
            {waypoints.length} waypoints
          </div>
          <div>
            <div>Waypoint distance:
              <select name="wayToGo" onChange={e => setWayToGo(+e.target.value)}>
                <option value={0.1} selected={wayToGo === 0.1}>100 m</option>
                <option value={1} selected={wayToGo === 1}>1 km</option>
                <option value={2} selected={wayToGo === 2}>2 km</option>
                <option value={5} selected={wayToGo === 5}>5 km</option>
              </select>
            </div>
          </div>
          <div>
            Total duration: {
            waypoints.length > 0 ?
              new Date((waypoints[waypoints.length - 1].time - waypoints[0].time) * 1000).toISOString().substring(11, 19)
              : 0} hours
          </div>
          <div>
            <button
              disabled={waypoints.length === 0}
              style={{margin: "0.5em"}}
              onClick={() => {
                const csv = waypoints.map(w => `${w.time};${w.lat.toFixed(5)};${w.lng.toFixed(5)}`).join('\n');
                const blob = new Blob([csv], {type: 'text/csv'});
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = 'waypoints.csv';
                a.click();
                window.URL.revokeObjectURL(url);
              }}>Download CSV
            </button>
          </div>
        </>
      }
    </div>

  )

}

export default function MyMap() {
  const [apiKey, setApiKey] = useState(API_KEY);
  const [error, setError] = useState<Error|undefined>(undefined);
  const [markers, setMarkers] = useState<LatLngTuple[]>([]);
  const [waypoints, setWaypoints] = useState<Waypoint[]>([]);
  const [wayToGo, setWayToGo] = useState(2);
  const clearMarkers = () => {
    setMarkers([]);
    setWaypoints([]);
  }
  const addMarker = (event: L.LeafletMouseEvent) => setMarkers([...markers, [event.latlng.lat, event.latlng.lng]]);
  useEffect(() => {
    const computeWaypoints = async () => {
      if (markers.length < 2) return
      const waypoints: Waypoint[] = [];
      let time = Date.now();
      for (let i= 1; i<markers.length; i++) {
        const start = [...markers[i-1]].reverse() as Coordinate;
        const end = [...markers[i]].reverse() as Coordinate;
        const {coordinates,speed, error} = await getRoute(apiKey,start, end);
        if (error) {
          setError(error);
          console.error(error)
          return
        }
        computeIntermediatePoints(coordinates, speed, time, wayToGo*1000).forEach(w => waypoints.push(w));
        time = waypoints[waypoints.length-1].time;
      }
      setWaypoints(waypoints);
    }
    computeWaypoints().then();
  }, [markers, wayToGo]);
  return (
    <div style={{width:"100vw", height:"100vh", display:"flex"}}>
      <MapContainer center={[49.505, 9.30]} zoom={5} style={{flex:4}}>
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        <Route markers={markers} waypoints={waypoints} onClick={addMarker}/>
      </MapContainer>
      <SidePanel
        markers={markers}
        setMarkers={setMarkers}
        clearMarkers={clearMarkers}
        waypoints={waypoints}
        wayToGo={wayToGo}
        setWayToGo={setWayToGo}
        apiKey={apiKey}
        setApiKey={setApiKey}
        error={error}
        setError={setError}
      />
    </div>
  )
}

