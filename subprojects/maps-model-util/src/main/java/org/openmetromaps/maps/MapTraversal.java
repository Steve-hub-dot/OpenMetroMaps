package org.openmetromaps.maps;

import org.openmetromaps.maps.model.*;

import java.util.*;

public class MapTraversal {
    public enum MapTraversalLimitType {
        TRANSFER_LIMIT, STOP_LIMIT, TIME_LIMIT
    }

    public static List<Station> traverseMap(ModelData model, Station src, MapTraversalLimitType limitType, int limit) {
        return new ArrayList<>();
    }
}
