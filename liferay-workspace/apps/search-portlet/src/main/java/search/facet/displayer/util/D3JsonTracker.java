/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package search.facet.displayer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shanon Mathai
 */
public class D3JsonTracker {

	public D3JsonTracker(String xf, String yf, String yl, String d3f) {
		_axisProperties = new D3AxisProperties();

		_axisProperties.setAxisxfield(xf);
		_axisProperties.setAxisyfield(yf);
		_axisProperties.setAxisylabel(yl);
		_axisProperties.setAxisyd3format(d3f);

		_d3Columns = new D3Columns(_axisProperties);
	}

	public void addEntry(String x, String y) {
		_d3Columns.addEntry(x, y);
	}

	public void clearAllEntries() {
		_d3Columns = new D3Columns(_axisProperties);
	}

	public String getJsonAxisProperties() {
		if (_axisProperties.getAxisyd3format().equals("d")) {
			String yf = _axisProperties.getAxisyfield();
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;

			for (Map<String, String> entry : _d3Columns.getEntries()) {
				int val = Integer.parseInt(entry.get(yf));

				min = val < min ? val : min;
				max = val > max ? val : max;
			}

			if (max >= min) {
				int diff = max - min + 1;

				_axisProperties.setAxisyticks(
					"" + (diff > _MAX_TICKS ? _MAX_TICKS : diff));
			}
		}

		try {
			return "'" + _mapper.writeValueAsString(_axisProperties) + "'";
		}
		catch (JsonProcessingException jpe) {
			return "'{}'";
		}
	}

	public String getJsonList() {
		try {
			return "'" + _mapper.writeValueAsString(_d3Columns) + "'";
		}
		catch (JsonProcessingException jpe) {
			return "'{}'";
		}
	}

	public static class D3AxisProperties {

		public String getAxisxfield() {
			return _axisxfield;
		}

		public String getAxisyd3format() {
			return _axisyd3format;
		}

		public String getAxisyfield() {
			return _axisyfield;
		}

		public String getAxisylabel() {
			return _axisylabel;
		}

		public String getAxisyticks() {
			return _axisyticks;
		}

		public void setAxisxfield(String axisxfield) {
			_axisxfield = axisxfield;
		}

		public void setAxisyd3format(String axisyd3format) {
			_axisyd3format = axisyd3format;
		}

		public void setAxisyfield(String axisyfield) {
			_axisyfield = axisyfield;
		}

		public void setAxisylabel(String axisylabel) {
			_axisylabel = axisylabel;
		}

		public void setAxisyticks(String axisyticks) {
			_axisyticks = axisyticks;
		}

		private String _axisxfield;
		private String _axisyd3format;
		private String _axisyfield;
		private String _axisylabel;
		private String _axisyticks;

	}

	public static class D3Columns {

		public D3Columns(D3AxisProperties d3ap) {
			_xField = d3ap.getAxisxfield();
			_yField = d3ap.getAxisyfield();
			_entries = new ArrayList<>();
		}

		public void addEntry(String x, String y) {
			Map<String, String> m = new HashMap<>();

			m.put(_xField, x);
			m.put(_yField, y);

			_entries.add(m);
		}

		public List<Map<String, String>> getEntries() {
			return _entries;
		}

		private List<Map<String, String>> _entries;
		private String _xField;
		private String _yField;

	}

	private static final int _MAX_TICKS = 10;

	private static ObjectMapper _mapper = new ObjectMapper();

	private D3AxisProperties _axisProperties;
	private D3Columns _d3Columns;

}