package org.openmetromaps.maps;

import org.openmetromaps.maps.model.*;


import java.util.*;
import java.util.stream.Collectors;

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
                result = reachableStopsTransfer(src, limit, adjacentMap);
                break;
            
            case STOP_LIMIT:
                addingStopsWithStopsLimit(src, limit, adjacentMap, visited, result);
                break;
            case TIME_LIMIT:
                addStopsTime(src, limit, adjacentMap, visited, result, 0);
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

    static void addStopsTime(Station src, int limit, Map<Station, Set<AdjacentStationInfo>> adjacencyMap, Set<Station> visited, List<Station> result, int timeSpent) {
        visited.add(src);

        if(!result.contains(src)) {
            result.add(src);
        }

        Set<AdjacentStationInfo> adjacentStations = adjacencyMap.getOrDefault(src, Collections.emptySet());

        for(AdjacentStationInfo adjInf : adjacentStations) {
            Station adjStation = adjInf.station;

            if(!visited.contains(adjStation)) {
                int traveltime = traveltime(src, adjStation);
                int totalTime = timeSpent + traveltime;

                if(totalTime <= limit) {
                    addStopsTime(adjStation, limit, adjacencyMap, visited, result, totalTime);
                }
            }

            visited.remove(src);
        }

    }

    static int traveltime(Station src, Station dest) {
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

    static void addingStopsWithStopsLimit(Station src, int limit, Map<Station, Set<AdjacentStationInfo>> adjacencyMap, Set<Station> visited, List<Station> result) {
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

            addingStopsWithStopsLimit(adjStat, limit-1, adjacencyMap, visited, result);
        }
    }

    static List<Station> reachableStopsTransfer(Station src, int limit, Map<Station, Set<AdjacentStationInfo>> adjacencyMap) {
        Set<Station> result = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        Map<Station, Integer> transfers = new HashMap<>();
        Set<Line> startingLines = src.getStops().stream().map(Stop::getLine).collect(Collectors.toSet());
        startingLines.addAll(src.getStops().stream().map(Stop::getLine).collect(Collectors.toSet()));

        queue.add(new State(src, limit, startingLines));
        transfers.put(src, limit);

        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            Station currentStation = currentState.station;
            int remainingTransfers = currentState.remainingTransfers;
            Set<Line> currentLines = currentState.currentLines;

            result.add(currentStation);
            Set<AdjacentStationInfo> adjacentStations = adjacencyMap.getOrDefault(currentStation, Collections.emptySet());

            for(AdjacentStationInfo info : adjacentStations) {
                Station adjStation = info.station;
                Line adjLine = info.line;

                boolean isTransfer = !currentLines.contains(adjLine);
                int newRemainingTransfers = isTransfer? remainingTransfers -1 : remainingTransfers;

                Integer pastTransfers = transfers.get(adjStation);

                if(newRemainingTransfers >= 0 && (pastTransfers == null || pastTransfers < newRemainingTransfers)) {
                    Set<Line> nexLines = isTransfer ? Collections.singleton(adjLine) : currentLines;

                    transfers.put(adjStation, newRemainingTransfers);
                    queue.add(new State(adjStation, newRemainingTransfers, nexLines));
                }

            }
            
            
        }
        return new ArrayList<>(result);
    }

    static class State{
        Station station;
        int remainingTransfers;
        Set<Line> currentLines;

        State(Station station, int remainingTransfers, Set<Line> currentLines) {
            this.station = station;
            this.remainingTransfers = remainingTransfers;
            this.currentLines = currentLines;
        }
    }

    static boolean shouldVisit(Station station, Set<Line> lines, int remainingTransfers, Map<Station, Map<Set<Line>, Integer>> visitedStates) {
        Map<Set<Line>, Integer> stationStates = visitedStates.computeIfAbsent(station, k-> new HashMap<>());

        for(Map.Entry<Set<Line>, Integer> entry : stationStates.entrySet()) {
            Set<Line> visitedLines = entry.getKey();
            int transfersLeft = entry.getValue();

            
        if(lines.containsAll(visitedLines) && remainingTransfers <= transfersLeft) {
            return false;
        }
        }

        stationStates.put(new HashSet<>(lines), remainingTransfers);
        return true;
    }
}