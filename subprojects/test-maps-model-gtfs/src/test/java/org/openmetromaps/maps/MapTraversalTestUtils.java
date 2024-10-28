package org.openmetromaps.maps;

import org.openmetromaps.gtfs.DraftModel;
import org.openmetromaps.gtfs.GtfsImporter;
import org.openmetromaps.maps.model.ModelData;
import org.openmetromaps.maps.model.Station;
import org.openmetromaps.misc.NameChanger;
import org.openmetromaps.model.gtfs.DraftModelConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class MapTraversalTestUtils {
    static class Operation {
        String limitType;
        int limit;
        Station sourceStation;

        Operation(String limitType, int limit, Station sourceStation) {
            this.limitType = limitType; 
            this.limit = limit;
            this.sourceStation = sourceStation;
        }
    }
    
    static Operation parseOperations(Path operationPath, ModelData inputModel) throws IOException {
        List<String> operationLines = Files.readAllLines(operationPath);
        validateInputChars(operationLines);
        if (operationLines.isEmpty()) {
            throw new IllegalArgumentException(operationPath + " - act.txt is empty");
        }
        String[] operation = operationLines.getFirst().split(";");

        if (operation.length != 3) {
            throw new IllegalArgumentException(operationPath + " - act.txt invalid format: Invaid number of parameters. act.txt must contain 3 parameters separated by a \";\".");
        }
        String limitType = switch (operation[0].toUpperCase()) {
            case "TRANSFER_LIMIT" -> "TRANSFER_LIMIT";
            case "STOP_LIMIT" -> "STOP_LIMIT";
            case "TIME_LIMIT" -> "TIME_LIMIT";
            default -> throw new IllegalArgumentException(operationPath + " - Invalid limit type: " + operation[0]);
        };

        String limitStr = operation[1];
        int limit;
        try {
            limit = Integer.parseUnsignedInt(limitStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(operationPath + " - Invalid limit: \"" + limitStr + "\" is not a non negative integer.", e);
        }

        String stationName = operation[2];
        Station sourceStation = inputModel.stations.stream().filter(s -> s.getName().equals(stationName)).findFirst().orElse(null);

        if (sourceStation == null) {
            throw new IllegalArgumentException(operationPath + " - Starting station not found: \"" + stationName + "\"");
        }
        return new Operation(limitType, limit, sourceStation);
    }

    static Set<String> parseExpectedStationNames(Path expectedPath, ModelData inputModel) throws IOException {
        Set<String> expectedStations = new HashSet<>();

        var assertLines = Files.readAllLines(expectedPath);
        validateInputChars(assertLines);
        for(String station : assertLines) {
            if (station.isBlank()){
                continue;
            }
            if (inputModel.stations.stream().noneMatch(s -> s.getName().equals(station))) {
                throw new IllegalArgumentException(expectedPath + " - Station not found: \"" + station + "\"");
            }
            expectedStations.add(station);
        }
        if (expectedStations.isEmpty()) {
            throw new IllegalArgumentException(expectedPath + " - Expected station list from assert.txt is empty.");
        }
        return  expectedStations;
    }

    static void validateInputChars(List<String> linesInFile) {
        // Validate the lines in file, and report the row and column of the error.
        for (int row = 0; row < linesInFile.size(); row++) {
            String line = linesInFile.get(row);

            // Position of the first non-matching character (with respect to "[\p{L}\p{N};-_ \r\n]") or -1
            int column = IntStream.range(0, line.length())
                    .filter(i -> {
                        char c = line.charAt(i);
                        return !Character.isLetterOrDigit(c) && c != ';' && c != '_' && c != '-' && c != ' ' && c != '\r' && c != '\n';
                    })
                    .findFirst()
                    .orElse(-1);
            if (column != -1) {
                System.out.printf("Invalid character '%c' in line %d at position %d (only alphanumeric characters, semicolons, underscores, dashes, spaces, and newlines are allowed).%n", line.charAt(column), row + 1, column);

                String inflatedSeparator = "-".repeat(line.length());
                System.out.println(inflatedSeparator);
                System.out.printf("%s%n", line);
                System.out.printf("%s^%n", " ".repeat(column));
                System.out.println(inflatedSeparator);

                throw new IllegalArgumentException(String.format("Invalid character '%c' in line %d at position %d (only alphanumeric characters, semicolons, underscores, dashes, spaces, and newlines are allowed).%n", line.charAt(column), row + 1, column));
            }
        }
    }
    static ModelData importModelFromGtfs(Path pathInput) throws IOException {
        GtfsImporter importer = new GtfsImporter(
                pathInput,
                new NameChanger(new ArrayList<>(), new ArrayList<>()),
                false
        );
        importer.execute();

        DraftModel draft = importer.getModel();
        ModelData data = new DraftModelConverter().convert(draft);
        return data;
    }
}
