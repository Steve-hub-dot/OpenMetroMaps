package org.openmetromaps.maps;

import org.openmetromaps.maps.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class MapTraversal {
    private MapTraversal() {}

    public static int calculateExpectedRevenue(ModelData data) {
        throw new RuntimeException("Not implemented");
    }
    public enum MapTraversalLimitType {
        TRANSFER_LIMIT, STOP_LIMIT, TIME_LIMIT
    }

    public static List<Station> traverseMap(ModelData model, Station src, MapTraversalLimitType limitType, int limit) {

        Map<Station, Set<AdjacentStationInfo>> adjacentMap = adjacencyMap(model);
        List<Station> result = new ArrayList<>();
    
        switch (limitType) {
            case TRANSFER_LIMIT:
                result = reachableStopsTransfer(src, limit, adjacentMap);
                break;
    
            case STOP_LIMIT:
                Set<Station> visitedStops = new HashSet<>();
                addingStopsWithStopsLimit(src, limit, adjacentMap, visitedStops, result);
                break;
    
            case TIME_LIMIT:
                Set<Station> visitedTime = new HashSet<>();
                //get lines the selected station is on
                Set<Line> startingLines = src.getStops().stream().map(Stop::getLine).collect(Collectors.toSet());
                addStopsTime(src, limit, adjacentMap, visitedTime, result, 0, startingLines);
                break;
    
            default:
                break;
        }
        return result;
    }

    static class AdjacentStationInfo {
        //the adjacent station
        Station station;
        //the line the adjacent station is a part of
        Line line;

        AdjacentStationInfo(Station station, Line line) {
            this.station = station;
            this.line = line;
        }
    }

    //maps stations to their adjacent stations and lines
    static Map<Station, Set<AdjacentStationInfo>> adjacencyMap(ModelData model) {
        Map<Station, Set<AdjacentStationInfo>> adjacencyMap = new HashMap<>();

        //iterating over all lines in the model
        for(Line line : model.lines) {
            List<Stop> stops = line.getStops();

            //iterate over all stops of the line
            for(int i = 0; i < stops.size(); i++) {
                Station curreStation = stops.get(i).getStation();

                //creating adjacency map
                adjacencyMap.putIfAbsent(curreStation, new HashSet<>());

                //adding previous station
                if( i > 0 ) {
                    Station prevStation = stops.get(i-1).getStation();
                    adjacencyMap.get(curreStation).add(new AdjacentStationInfo(prevStation, line));
                }

                //adding next station
                if(i < stops.size()-1) {
                    Station nextStation = stops.get(i+1).getStation();
                    adjacencyMap.get(curreStation).add(new AdjacentStationInfo(nextStation, line));
                }
            }
        }
        return adjacencyMap;
    }

    static void addStopsTime(Station src, int limit, Map<Station, Set<AdjacentStationInfo>> adjacencyMap, Set<Station> visited, List<Station> result, int timeSpent, Set<Line> currentLines) {
    
        visited.add(src);

        //adding current station if not already added
        if (!result.contains(src)) {
            result.add(src);
        }


        Set<AdjacentStationInfo> adjacentStations = adjacencyMap.getOrDefault(src, Collections.emptySet());

        //iterating over all adjacentStations
        for (AdjacentStationInfo adjInf : adjacentStations) {
            Station adjStation = adjInf.station;
            Line adjLine = adjInf.line;

            if (!visited.contains(adjStation)) {
                int traveltime = traveltime(src, adjStation);

                //checking if its a transfer
                boolean isTransfer = !currentLines.contains(adjLine);
                if (isTransfer) {
                    //adding transfer time
                    traveltime += 5;
                }

                int totalTime = timeSpent + traveltime;

                //check if within time limit
                if (totalTime <= limit) {
                    //update currentLines for the next recursion
                    Set<Line> newCurrentLines = isTransfer ? new HashSet<>(Collections.singleton(adjLine)) : currentLines;

                    //adding adjacent stations recursively
                    addStopsTime(adjStation, limit, adjacencyMap, visited, result, totalTime, newCurrentLines);
                }
            }
        }
    }

    //calculates the travel time between src and dest stations
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

    //recursive method to add stations reachable within a stop limit
    static void addingStopsWithStopsLimit(Station src, int limit, Map<Station, Set<AdjacentStationInfo>> adjacencyMap, Set<Station> visited, List<Station> result) {
        visited.add(src);

        if(!result.contains(src)) {
            result.add(src);
        }

        //check if stop limits are reached
        if(limit == 0) {
            visited.remove(src);
            return;
        }

        //get adjacent stations
        Set<AdjacentStationInfo> adjacentStations = adjacencyMap.getOrDefault(src, Collections.emptySet());

        //iterating over all adjacent stations
        for(AdjacentStationInfo adjInfo : adjacentStations) {
            Station adjStat = adjInfo.station;

            //skipping station if already visited
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

        //get the lines the selected station is on
        Set<Line> startingLines = src.getStops().stream().map(Stop::getLine).collect(Collectors.toSet());

        //initialize queue with the selected station
        queue.add(new State(src, limit, startingLines));
        transfers.put(src, limit);

        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            Station currentStation = currentState.station;
            int remainingTransfers = currentState.remainingTransfers;
            Set<Line> currentLines = currentState.currentLines;

            //add current station to result
            result.add(currentStation);
            //get its adjacent stations
            Set<AdjacentStationInfo> adjacentStations = adjacencyMap.getOrDefault(currentStation, Collections.emptySet());

            goingThroughAdjacentStations(adjacentStations, currentLines, remainingTransfers, transfers, queue);
        }
        return new ArrayList<>(result);
    }

    static void goingThroughAdjacentStations(Set<AdjacentStationInfo> adjacentStations, Set<Line> currentLines, int remainingTransfers, Map<Station, Integer> transfers, Queue<State> queue) {
        for (AdjacentStationInfo adjacentStationInfo : adjacentStations) {
            Station adjStation = adjacentStationInfo.station;
            Line adjLine = adjacentStationInfo.line;

            //check if transferring to a new line
            boolean isTransfer = !currentLines.contains(adjLine);
            //updating remaining transfers
            int newRemainingTransfers = isTransfer ? remainingTransfers -1 : remainingTransfers;
            handleTransferAndQueue(adjStation, adjLine, isTransfer, newRemainingTransfers, transfers, queue, currentLines);
        }
    }
    
    static void handleTransferAndQueue(Station adjStation, Line adjLine, boolean isTransfer, int newRemainingTransfers, Map<Station, Integer> transfers, Queue<State> queue, Set<Line> currentLines) {
        Integer pastTransfers = transfers.get(adjStation);
        if(newRemainingTransfers >= 0 && (pastTransfers == null || pastTransfers < newRemainingTransfers)) {
            //update lines depending on whether a transfer has occured
            Set<Line> nexLines = isTransfer ? Collections.singleton(adjLine) : currentLines;

            //update transfers map with the remaining transfers
            transfers.put(adjStation, newRemainingTransfers);
            queue.add(new State(adjStation, newRemainingTransfers, nexLines));
        }
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

}