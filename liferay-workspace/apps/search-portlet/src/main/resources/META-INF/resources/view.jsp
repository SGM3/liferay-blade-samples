<%@ include file="/init.jsp" %>

<liferay-ui:search-container delta="10">
	<liferay-ui:search-container-results results="${docFromSearchResults}"/>

	<liferay-ui:search-container-row
		className="com.liferay.portal.kernel.search.Document"
		keyProperty="UID"
		modelVar="doc"
	>
		<liferay-ui:search-container-column-text
			name="name"
			value="<%= "example" %>"
		/>

		<liferay-ui:search-container-column-text
			name="UID"
			property="UID"
		/>
	</liferay-ui:search-container-row>

	<liferay-ui:search-iterator />

</liferay-ui:search-container>

<svg id="graph-container" width="960" height="500"></svg>
<%-- <script src="https://d3js.org/d3.v4.min.js"></script> --%>

<script>
define._amd = define.amd;
define.amd = false;
</script>

<script type="text/javascript" src="https://d3js.org/d3.v4.min.js"></script>

<script>
define.amd = define._amd;
</script>

<script>
var jsonData = '{' +
               '    "values": [{' +
               '        "letter": "A",' +
               '        "frequency":"0.08167"' +
               '    },' +
               '    {' +
               '        "letter": "B",' +
               '        "frequency":"0.01492"' +
               '    },' +
               '    {' +
               '        "letter": "C",' +
               '        "frequency":"0.02782"' +
               '    },' +
               '    {' +
               '        "letter": "D",' +
               '        "frequency":"0.04253"' +
               '    },' +
               '    {' +
               '        "letter": "E",' +
               '        "frequency":"0.12702"' +
               '    },' +
               '    {' +
               '        "letter": "F",' +
               '        "frequency":"0.02288"' +
               '    },' +
               '    {' +
               '        "letter": "G",' +
               '        "frequency":"0.02015"' +
               '    },' +
               '    {' +
               '        "letter": "H",' +
               '        "frequency":"0.06094"' +
               '    },' +
               '    {' +
               '        "letter": "I",' +
               '        "frequency":"0.06966"' +
               '    },' +
               '    {' +
               '        "letter": "J",' +
               '        "frequency":"0.00153"' +
               '    },' +
               '    {' +
               '        "letter": "K",' +
               '        "frequency":"0.00772"' +
               '    },' +
               '    {' +
               '        "letter": "L",' +
               '        "frequency":"0.04025"' +
               '    },' +
               '    {' +
               '        "letter": "M",' +
               '        "frequency":"0.02406"' +
               '    },' +
               '    {' +
               '        "letter": "N",' +
               '        "frequency":"0.06749"' +
               '    },' +
               '    {' +
               '        "letter": "O",' +
               '        "frequency":"0.07507"' +
               '    },' +
               '    {' +
               '        "letter": "P",' +
               '        "frequency":"0.01929"' +
               '    },' +
               '    {' +
               '        "letter": "Q",' +
               '        "frequency":"0.00095"' +
               '    },' +
               '    {' +
               '        "letter": "R",' +
               '        "frequency":"0.05987"' +
               '    },' +
               '    {' +
               '        "letter": "S",' +
               '        "frequency":"0.06327"' +
               '    },' +
               '    {' +
               '        "letter": "T",' +
               '        "frequency":"0.09056"' +
               '    },' +
               '    {' +
               '        "letter": "U",' +
               '        "frequency":"0.02758"' +
               '    },' +
               '    {' +
               '        "letter": "V",' +
               '        "frequency":"0.00978"' +
               '    },' +
               '    {' +
               '        "letter": "W",' +
               '        "frequency":"0.02360"' +
               '    },' +
               '    {' +
               '        "letter": "X",' +
               '        "frequency":"0.00150"' +
               '    },' +
               '    {' +
               '        "letter": "Y",' +
               '        "frequency":"0.01974"' +
               '    },' +
               '    {' +
               '        "letter": "Z",' +
               '        "frequency":"0.00074"' +
               '    }]' +
               '}';
// https://github.com/d3/d3-format
/*
var axisProperties = {
    axisxfield: 'letter',
    axisylabel: 'Frequency',
    axisyfield: 'frequency',
    axisyd3format: '%'
}
*/
//d3.json(url[, callback])


jsonData = ${jsonStringSearchResults};
var axisProperties = {
    axisxfield: 'portletId',
    axisylabel: 'Number Of Fields',
    axisyfield: 'numFields',
    axisyd3format: 'd'
}
jsonData = JSON.parse(jsonData);

var margins = {top: 20, right: 20, bottom: 150, left: 50};

renderHistogram("#graph-container", jsonData, axisProperties, margins)
//renderHistogram("#graph-container", jsonData, axisProperties, margins)
</script>