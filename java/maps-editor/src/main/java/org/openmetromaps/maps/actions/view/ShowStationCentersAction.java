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

package org.openmetromaps.maps.actions.view;

import org.openmetromaps.maps.MapEditor;
import org.openmetromaps.maps.actions.MapEditorBooleanAction;

import de.topobyte.swing.util.EmptyIcon;

public class ShowStationCentersAction extends MapEditorBooleanAction
{

	private static final long serialVersionUID = 1L;

	public ShowStationCentersAction(MapEditor mapEditor)
	{
		super(mapEditor, "Show Station Centers",
				"Toggle visibility of station centers");
		setIcon(new EmptyIcon(24));
	}

	@Override
	public boolean getState()
	{
		return mapEditor.isShowStationCenters();
	}

	@Override
	public void toggleState()
	{
		mapEditor.setShowStationCenters(!mapEditor.isShowStationCenters());
	}

}
