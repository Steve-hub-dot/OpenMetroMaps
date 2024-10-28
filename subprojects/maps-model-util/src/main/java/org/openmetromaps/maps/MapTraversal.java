package org.openmetromaps.maps;

import org.openmetromaps.maps.model.*;

import java.util.*;
import java.util.Map;

public class MapTraversal {
    public enum MapTraversalLimitType {
        TRANSFER_LIMIT, STOP_LIMIT, TIME_LIMIT
    }

    public static List<Station> traverseMap(ModelData model, Station src, MapTraversalLimitType limitType, int limit) {

        Set<Station> result = new HashSet<>();
        ArrayList<Line> validLines;
        result.add(src);

        

        switch (limitType) {
            case TRANSFER_LIMIT:
                
                break;
            
            case STOP_LIMIT:
                int srcStopId;
                Map<Station, Integer> possibleTransfers = new HashMap<>();

                validLines = findValidLines(model, src);

                for (Line line : validLines) {
                    srcStopId = getSrcId(line, src);
                    if(srcStopId != -1) addNearbyStops(model, srcStopId, limit, line, result, possibleTransfers);
                }
                break;

            case TIME_LIMIT:
                break;

            default:
                break;
        }
        return new ArrayList<>(result);
    }
    
    static ArrayList<Line> findValidLines(ModelData model, Station src) {
        ArrayList<Line> validLines = new ArrayList<>();

        for (Line line : model.lines) {
            if(line.getStops().stream().anyMatch(stop -> stop.getStation().equals(src))) {
                validLines.add(line);
            }
        }
        return validLines;
    }

    static void addNearbyStops(ModelData model, int srcStopId, int limit, Line line, Set<Station> result, Map<Station, Integer> possibleTransfers) {
        if(limit == 0) return;

        addNearbyStopsToTheRight(srcStopId, limit, line, result, possibleTransfers);
        addNearbyStopsToTheLeft(srcStopId, limit, line, result, possibleTransfers);

        //checking for transfers
        for(Line otherLine : model.lines) {
            if(otherLine != line) {
                for( Stop stop : otherLine.getStops()) {
                    if(possibleTransfers.containsKey(stop.getStation())) {
                        addNearbyStops(model, srcStopId, limit-1, otherLine, result, possibleTransfers);
                    }
                }
            }
        }
    }

    static void addNearbyStopsToTheLeft(int srcStopId, int limit, Line line, Set<Station> result, Map<Station, Integer> possibleTransfers) {
        for(int i = srcStopId -1; i >= Math.max(srcStopId - limit, 0); i--) {
            Stop current = line.getStops().get(i);
            if(!result.contains(current.getStation())) {
                result.add(current.getStation());
                if(!possibleTransfers.containsKey(current.getStation())) {
                    possibleTransfers.put(current.getStation(), limit - (srcStopId -i));
                }
            }
        }
    }
    static void addNearbyStopsToTheRight(int srcStopId, int limit, Line line, Set<Station> result, Map<Station, Integer> possibleTransfers) {
        for(int i = srcStopId + 1; i < Math.min(srcStopId + limit, line.getStops().size()); i++) {
            Stop current = line.getStops().get(i);
            if(!result.contains(current.getStation())) {
                result.add(current.getStation());
                if(!possibleTransfers.containsKey(current.getStation())) {
                    possibleTransfers.put(current.getStation(), limit - (i - srcStopId));
                }
            }
        }
    }

    static int getSrcId(Line line, Station src) {
        int i = -1;
        for(Stop stop : line.getStops()) {
            if(stop.getStation().equals(src)) {
                i = line.getStops().indexOf(stop);
                break;
            }
        }
        return i;
    }
}
