package org.openmetromaps.maps;

import org.openmetromaps.maps.model.*;

import de.topobyte.lightgeom.lina.Vector2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MapTraversal {
    public enum MapTraversalLimitType {
        TRANSFER_LIMIT, STOP_LIMIT, TIME_LIMIT
    }

    public static List<Station> traverseMap(ModelData model, Station src, MapTraversalLimitType limitType, int limit) {

        Set<Station> visited = new HashSet<>();
        Map<Station, Set<AdjacentStationInfo>> adjacentMap = adjacencyMap(model);
        List<Station> result = new ArrayList<>();

        switch (limitType) {
            case TRANSFER_LIMIT:
                
                break;
            
            case STOP_LIMIT:
                addNearbyStopsLimitStop(src, limit, adjacentMap, visited, result);
                break;
            case TIME_LIMIT:
                addNearbyStopsLimitTime(src, limit, adjacentMap, visited, result, 0);
                break;

            default:
                break;
        }
        return result;
    }

    static class AdjacentStationInfo {
        Station station;
        Line line;

        AdjacentStationInfo(Station station, Line line) {
            this.station = station;
            this.line = line;
        }
    }

    static Map<Station, Set<AdjacentStationInfo>> adjacencyMap(ModelData model) {
        Map<Station, Set<AdjacentStationInfo>> adjacencyMap = new HashMap<>();

        for(Line line : model.lines) {
            List<Stop> stops = line.getStops();

            for(int i = 0; i < stops.size(); i++) {
                Station curreStation = stops.get(i).getStation();

                adjacencyMap.putIfAbsent(curreStation, new HashSet<>());

                if( i > 0 ) {
                    Station prevStation = stops.get(i-1).getStation();
                    adjacencyMap.get(curreStation).add(new AdjacentStationInfo(prevStation, line));
                }

                if(i < stops.size()-1) {
                    Station nextStation = stops.get(i+1).getStation();
                    adjacencyMap.get(curreStation).add(new AdjacentStationInfo(nextStation, line));
                }
            }
        }
        return adjacencyMap;
    }

    static void addNearbyStopsLimitTime(Station src, int limit, Map<Station, Set<AdjacentStationInfo>> adjacencyMap, Set<Station> visited, List<Station> result, int timeSpent) {
        visited.add(src);

        if(!result.contains(src)) {
            result.add(src);
        }

        Set<AdjacentStationInfo> adjacentStations = adjacencyMap.getOrDefault(src, Collections.emptySet());

        for(AdjacentStationInfo adjInf : adjacentStations) {
            Station adjStation = adjInf.station;

            if(visited.contains(adjStation)) {
                continue;
            }

            int travelTime = calculateTraverTime(src, adjStation);

            int totalTime = timeSpent + travelTime;

            if(totalTime > limit) {
                continue;
            }
            
            addNearbyStopsLimitTime(adjStation, limit, adjacencyMap, visited, result, totalTime);

            visited.remove(src);
        }

    }

    static int calculateTraverTime(Station src, Station dest) {
        Coordinate coordFrom = src.getLocation();
        Coordinate coordTo = dest.getLocation();

        double srcLat = coordFrom.getLatitude();
        double srcLong = coordFrom.getLongitude();

        double destLat = coordTo.getLatitude();
        double destLong = coordTo.getLongitude();

        double deltaLat = Math.abs(srcLat - destLat) * 110.574;
        double deltaLong = Math.abs(srcLong * 111.320 * Math.cos(Math.toRadians(srcLat))) - 
        (destLong * 111.320 * Math.cos(Math.toRadians(destLat)));

        double distance = Math.sqrt(Math.pow(deltaLat, 2) + Math.pow(deltaLong, 2));

        return (int) Math.ceil((((distance / 40) * 60) + 1));
    }
    static void addNearbyStopsLimitStop(Station src, int limit, Map<Station, Set<AdjacentStationInfo>> adjacencyMap, Set<Station> visited, List<Station> result) {
        visited.add(src);

        if(!result.contains(src)) {
            result.add(src);
        }

        if(limit == 0) {
            visited.remove(src);
            return;
        }

        Set<AdjacentStationInfo> adjacentStations = adjacencyMap.getOrDefault(src, Collections.emptySet());

        for(AdjacentStationInfo adjInfo : adjacentStations) {
            Station adjStat = adjInfo.station;

            if(visited.contains(adjStat)) {
                continue;
            }

            addNearbyStopsLimitStop(adjStat, limit-1, adjacencyMap, visited, result);
        }
        return;
    }

}
