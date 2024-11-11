package org.openmetromaps.maps;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openmetromaps.maps.model.ModelData;
import org.openmetromaps.maps.model.Station;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.openmetromaps.maps.MapTraversalTestUtils.*;

@RunWith(Parameterized.class)
public class MapTraversalTest {
    @Parameterized.Parameter(0)
    public String path;

    private static Collection<String> userTests() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("./map-traversal");
        if(url == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(Objects.requireNonNull(new File(url.getPath()).listFiles()))
            .filter(
                file -> file.exists() && file.isDirectory()
            )
            .map(File::getName)
            .toList();
    }

    @Parameterized.Parameters(name = "ID: {0}")
    public static Collection<Object[]> data() {
        Collection<String> userTests = userTests();

        return userTests.stream().flatMap(test -> Stream.of(Collections.singletonList(test))).map(List::toArray).toList();
    }

    @Test
    public void testStationsEqual() throws IOException {
        Path basePath = Path.of("src/test/resources/map-traversal/" + path);

        Path inputPath = basePath.resolve("arrange.zip");
        if(!Files.exists(inputPath)) {
            throw new IllegalArgumentException(path + " - arrange.zip not found in folder: " + inputPath);
        }
        Path expectedPath = basePath.resolve("assert.txt");
        if(!Files.exists(expectedPath)) {
            throw new IllegalArgumentException(path + " - assert.txt not found in folder: " + expectedPath);
        }
        Path operationPath = basePath.resolve("act.txt");
        if(!Files.exists(operationPath)) {
            throw new IllegalArgumentException(path + " - act.txt not found in folder: " + operationPath);
        }
        Path commentPath = basePath.resolve("comment.txt");
        if(!Files.exists(commentPath)) {
            throw new IllegalArgumentException(path + " - comment.txt not found in folder: " + commentPath);
        }

        if (Files.readString(commentPath).isBlank()) {
            throw new IllegalArgumentException(path + " - comment.txt is empty");
        }

        ModelData inputModel;
        try {
            inputModel = importModelFromGtfs(inputPath);
        } catch (Exception e){
            throw new IllegalArgumentException(path + " - Failed to import gtfs file.", e);
        }

        MapTraversalTestUtils.Operation operation = parseOperations(operationPath, inputModel);
        Set<String> expectedStationNames = parseExpectedStationNames(expectedPath, inputModel);

        List<Station> outputStations = MapTraversal.traverseMap(inputModel, operation.sourceStation, operation.limitType, operation.limit);
        Set<String> outputStationNames = new HashSet<>(outputStations.stream().map(Station::getName).toList());

        Assert.assertEquals(expectedStationNames, outputStationNames);
    }

}
