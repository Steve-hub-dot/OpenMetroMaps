package org.openmetromaps.maps;

import org.junit.Before;
import org.junit.Test;
import org.openmetromaps.maps.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class MapTraversalUnitTests {
    ModelData model;

    Station stationA;
    Station stationB;
    Station stationC;

    Line line1;

    /*
     * 1 X----X----X
     *   A    B    C
     */
    @Before
    public void createMap() {
        // Arrange

        List<Stop> stationAStops = new ArrayList<>();
        stationA = new Station(0, "A", new Coordinate(47.4891, 19.0614), stationAStops);

        List<Stop> stationBStops = new ArrayList<>();
        stationB = new Station(1, "B", new Coordinate(47.4891, 19.0714), stationBStops);

        List<Stop> stationCStops = new ArrayList<>();
        stationC = new Station(2, "C", new Coordinate(47.4891, 19.0814), stationCStops);

        List<Stop> line1Stops = new ArrayList<>();
        line1 = new Line(3, "1", "#009EE3", false, line1Stops);

        Stop line1AStop = new Stop(stationA, line1);
        stationAStops.add(line1AStop);
        line1Stops.add(line1AStop);

        Stop line1BStop = new Stop(stationB, line1);
        stationBStops.add(line1BStop);
        line1Stops.add(line1BStop);

        Stop line1CStop = new Stop(stationC, line1);
        stationCStops.add(line1CStop);
        line1Stops.add(line1CStop);

        model = new ModelData(new ArrayList<>(List.of(line1)), new ArrayList<>(List.of(stationA, stationB, stationC)));
    }

    /*
     * Starting from:
     *
     * 1 X----X----X
     *   A    B    C
     *
     * STOP_LIMIT 1: A, B
     */
    @Test
    public void testStopLimit() {
        // Act
        List<Station> result = MapTraversal.traverseMap(model, stationA, MapTraversal.MapTraversalLimitType.STOP_LIMIT, 1);

        // Assert
        Set<Station> expected = Set.of(stationA, stationB);
        assertEquals(expected, new HashSet<>(result));
    }

    /*
     * Starting from:
     *
     * 1 X----X----X
     *   A    B    C
     *
     * TRANSFER_LIMIT 0: A, B, C
     */
    @Test
    public void testTransferLimit() {
        // Act
        List<Station> result = MapTraversal.traverseMap(model, stationA, MapTraversal.MapTraversalLimitType.TRANSFER_LIMIT, 0);

        // Assert
        Set<Station> expected = Set.of(stationA, stationB, stationC);
        assertEquals(expected, new HashSet<>(result));
    }

    /*
     * Starting from:
     *
     * 1 X----X----X
     *   A    B    C
     *
     * TIME_LIMIT 5: A, B
     */
    @Test
    public void testTimeLimit() {
        // Act
        List<Station> result = MapTraversal.traverseMap(model, stationA, MapTraversal.MapTraversalLimitType.TIME_LIMIT, 5);

        // Assert
        Set<Station> expected = Set.of(stationA, stationB);
        assertEquals(expected, new HashSet<>(result));
    }
}
