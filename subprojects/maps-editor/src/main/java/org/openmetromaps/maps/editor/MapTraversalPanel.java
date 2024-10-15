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

package org.openmetromaps.maps.editor;

import de.topobyte.awt.util.GridBagConstraintsEditor;
import org.apache.commons.lang3.tuple.Pair;
import org.openmetromaps.maps.MapTraversal;
import org.openmetromaps.maps.graph.Edge;
import org.openmetromaps.maps.graph.NetworkLine;
import org.openmetromaps.maps.graph.Node;
import org.openmetromaps.maps.model.Line;
import org.openmetromaps.maps.model.Station;
import org.openmetromaps.maps.model.Stop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class MapTraversalPanel extends JPanel {

    final static Logger logger = LoggerFactory.getLogger(MapTraversalPanel.class);

    private static final long serialVersionUID = 1L;

    private MapEditor mapEditor;

    private Set<Node> nodes;

    MapTraversal.MapTraversalLimitType limitType = MapTraversal.MapTraversalLimitType.TRANSFER_LIMIT;
    int limit = 1;

    public MapTraversalPanel(MapEditor mapEditor) {
        super(new GridLayout(0, 1));
        this.mapEditor = mapEditor;
        this.nodes = new HashSet<>();

        setupLayout();

        mapEditor.addDataChangeListener(this::refresh);
    }

    private void setupLayout() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraintsEditor ce = new GridBagConstraintsEditor();
        GridBagConstraints c = ce.getConstraints();

        ce.fill(GridBagConstraints.BOTH);
        ce.weight(0, 0);
        c.insets = new Insets(0, 2, 4, 4);

        int lineCount = 0;
        JPanel preferencePanel = new JPanel(new GridLayout(0, 1, 0, 0));

        ce.gridPos(0, lineCount++);

        ButtonGroup bg = new ButtonGroup();
        JRadioButton transferLimitButton = new JRadioButton("Limit transfers");
        JRadioButton stopLimitButton = new JRadioButton("Limit stops");
        JRadioButton timeLimitButton = new JRadioButton("Limit time");
        transferLimitButton.setSelected(limitType == MapTraversal.MapTraversalLimitType.TRANSFER_LIMIT);
        stopLimitButton.setSelected(limitType == MapTraversal.MapTraversalLimitType.STOP_LIMIT);
        timeLimitButton.setSelected(limitType == MapTraversal.MapTraversalLimitType.TIME_LIMIT);
        ActionListener preferenceListener = a -> {
            if (transferLimitButton.isSelected())
                limitType = MapTraversal.MapTraversalLimitType.TRANSFER_LIMIT;
            else if (stopLimitButton.isSelected())
                limitType = MapTraversal.MapTraversalLimitType.STOP_LIMIT;
            else if (timeLimitButton.isSelected())
                limitType = MapTraversal.MapTraversalLimitType.TIME_LIMIT;
        };
        transferLimitButton.addActionListener(preferenceListener);
        stopLimitButton.addActionListener(preferenceListener);
        timeLimitButton.addActionListener(preferenceListener);
        preferencePanel.add(transferLimitButton);
        preferencePanel.add(stopLimitButton);
        preferencePanel.add(timeLimitButton);
        bg.add(transferLimitButton);
        bg.add(stopLimitButton);
        bg.add(timeLimitButton);

        ce.gridPos(0, lineCount++);
        panel.add(preferencePanel, c);

        JPanel limitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JSpinner limitField = new JSpinner(new SpinnerNumberModel(limit,0,Integer.MAX_VALUE,1));
        limitField.addChangeListener((e) -> {
            limit = (int)limitField.getValue();
        });
        limitField.setPreferredSize(new Dimension(64, 20));
        JLabel limitFieldText = new JLabel("Limit:");
        limitPanel.add(limitFieldText);
        limitPanel.add(limitField);

        ce.gridPos(0, lineCount++);
        panel.add(limitPanel, c);

        JButton navigateButton = new JButton("Traverse");
        navigateButton.setEnabled(nodes.size() == 1);
        navigateButton.addActionListener(this::navigateHandler);

        ce.gridPos(0, lineCount++);
        panel.add(navigateButton, c);

        Set<Station> highlightedStations = mapEditor.getMapViewStatus().getHighlightedStations();

        for (Station station : highlightedStations.stream().sorted(Comparator.comparing(Station::getName)).toList()) {
            ce.gridPos(0, lineCount++);
            panel.add(new JLabel(station.getName()), c);
        }

        ce.gridPos(0, lineCount);
        ce.weight(1, 1);
        panel.add(new JPanel(), c);

        add(new JScrollPane(panel));
    }

    private void navigateHandler(ActionEvent e) {
        var from = nodes.iterator().next().station;
        List<Station> stations = MapTraversal.traverseMap(mapEditor.getModel().getData(), from, limitType, limit);

        mapEditor.removeHighlight();
        mapEditor.highlightStations(stations);
        mapEditor.triggerDataChanged();
    }

    public void setSelection(Set<Node> nodes, Set<NetworkLine> selectedLines) {
        this.nodes = nodes;
        refresh();
    }

    protected void refresh() {
        removeAll();
        setupLayout();
        revalidate();
        repaint();
    }
}
