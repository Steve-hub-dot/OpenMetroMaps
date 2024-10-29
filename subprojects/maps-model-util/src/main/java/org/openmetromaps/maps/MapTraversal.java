package org.openmetromaps.maps;

import org.openmetromaps.maps.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MapTraversal {
    public enum MapTraversalLimitType {
        TRANSFER_LIMIT, STOP_LIMIT, TIME_LIMIT
    }

    public static List<Station> traverseMap(ModelData model, Station src, MapTraversalLimitType limitType, int limit) {
        Set<Station> result = new HashSet<>();
        ArrayList<Line> validLines;        
        Map<Line, Integer> possibleLines = new ConcurrentHashMap<>();
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
                    if(srcStopId != -1) addNearbyStops(model, srcStopId, limit, line, result, possibleTransfers, possibleLines);
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

    static void addNearbyStops(ModelData model, int srcStopId, int limit, Line line, Set<Station> result, Map<Station, Integer> possibleTransfers, Map<Line, Integer> possibleLines) {
        if(limit == 0) return;


        addNearbyStopsToTheRight(srcStopId, limit, line, result, possibleTransfers);
        addNearbyStopsToTheLeft(srcStopId, limit, line, result, possibleTransfers);

        //checking for transfers
        for(Line otherLine : model.lines) {
            if(!otherLine.equals(line) && !possibleLines.containsKey(otherLine)) {
                for( Stop stop : otherLine.getStops()) {
                    if(possibleTransfers.containsKey(stop.getStation())) {
                        srcStopId = getSrcId(otherLine, stop.getStation());
                        if(srcStopId != -1 || limit > 1)
                            possibleLines.put(otherLine, srcStopId);
                    }
                }
            }
        }

        for( Map.Entry<Line, Integer> a : possibleLines.entrySet()) {
            addNearbyStops(model, a.getValue().intValue(), limit-1, a.getKey(), result, possibleTransfers, possibleLines);
        }
    }

    static void addNearbyStopsToTheLeft(int srcStopId, int limit, Line line, Set<Station> result, Map<Station, Integer> possibleTransfers) {
        for(int i = srcStopId; i >= Math.max(srcStopId - limit +1, 0); i--) {
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
        for(int i = srcStopId; i <= Math.min(srcStopId + limit, line.getStops().size()-1); i++) {
            Stop current = line.getStops().get(i);
            if(!result.contains(current.getStation())) {
                result.add(current.getStation());
                if(!possibleTransfers.containsKey(current.getStation())) {
                    possibleTransfers.put(current.getStation(), limit - Math.abs((i - srcStopId)));
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
