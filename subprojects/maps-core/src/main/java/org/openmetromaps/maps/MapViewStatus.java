// Copyright 2017 Sebastian Kuerten
//
// This file is part of OpenMetroMaps.
//
// OpenMetroMaps is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// OpenMetroMaps is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with OpenMetroMaps. If not, see <http://www.gnu.org/licenses/>.

package org.openmetromaps.maps;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;

import org.openmetromaps.maps.graph.Edge;
import org.openmetromaps.maps.graph.NetworkLine;
import org.openmetromaps.maps.graph.Node;
import org.openmetromaps.maps.model.Station;
import org.openmetromaps.maps.model.Stop;

public class MapViewStatus
{

    private List<Node> selectedNodes = new ArrayList<>();
	private Set<NetworkLine> hiddenLines = new HashSet<>();
	private Set<NetworkLine> selectedLines = new LinkedHashSet<>();
	private List<Stop> highlightedPath = new ArrayList<>();
	private List<Map.Entry<Edge, NetworkLine>> highlightedEdgeLines = new ArrayList<>();
	private Set<Station> highlightedStations = new HashSet<>();


	public boolean isNodeSelected(Node node)
	{
		return selectedNodes.contains(node);
	}

	public void selectNode(Node node)
	{
		selectedNodes.add(node);
	}

	public void unselectNode(Node node)
	{
		selectedNodes.remove(node);
	}

	public void selectNoNodes()
	{
		selectedNodes.clear();
	}

	public int getNumSelectedNodes()
	{
		return selectedNodes.size();
	}

	public List<Node> getSelectedNodes()
	{
		return Collections.unmodifiableList(selectedNodes);
	}

	public boolean isLineHidden(NetworkLine line) {
		return hiddenLines.contains(line);
	}

	public void hideLine(NetworkLine line) {
		hiddenLines.add(line);
	}

	public void unhideLine(NetworkLine line) {
		hiddenLines.remove(line);
	}

	public void hideNoLines() {
		hiddenLines.clear();
	}

	public int getNumHiddenLines() {
		return hiddenLines.size();
	}

	public Set<NetworkLine> getHiddenLines() {
		return Collections.unmodifiableSet(hiddenLines);
	}

	public boolean isLineSelected(NetworkLine line) {
		return selectedLines.contains(line);
	}

	public boolean isEdgeLineHighlighted(Edge edge, NetworkLine line) {
		return highlightedEdgeLines.contains(Map.entry(edge, line));
	}

	public boolean isStationHighlighted(Station station) {
		return highlightedStations.contains(station);
	}

	public void selectLine(NetworkLine line) {
		selectedLines.add(line);
	}

	public void unselectLine(NetworkLine line) {
		selectedLines.remove(line);
	}

	public void selectNoLines() {
		selectedLines.clear();
	}

	public int getNumSelectedLines() {
		return selectedLines.size();
	}

	public Set<NetworkLine> getSelectedLines() {
		return Collections.unmodifiableSet(selectedLines);
	}

	public void highlightPath(List<Stop> path, List<? extends Map.Entry<Edge, NetworkLine>> edgeLines) {
		highlightedPath.addAll(path);
		highlightedEdgeLines.addAll(edgeLines);
		highlightedStations.addAll(path.stream().map(Stop::getStation).toList());
	}

	public void highlightStations(List<Station> stations) {
		highlightedStations.addAll(stations);
	}

	public void removeHighlight() {
		highlightedPath.clear();
		highlightedEdgeLines.clear();
		highlightedStations.clear();
	}

	public List<Map.Entry<Edge, NetworkLine>> getHighlightedEdgeLines() {
		return Collections.unmodifiableList(highlightedEdgeLines);
	}

	public List<Stop> getHighlightedPath() {
		return Collections.unmodifiableList(highlightedPath);
	}

	public Set<Station> getHighlightedStations() {
		return Collections.unmodifiableSet(highlightedStations);
	}
}
