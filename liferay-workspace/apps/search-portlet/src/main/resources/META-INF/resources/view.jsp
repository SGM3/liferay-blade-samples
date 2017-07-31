<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
int curIndexForDocs = 0;
List<String> userSelectedTermValues = (List<String>)renderRequest.getAttribute("userSelectedTermValues");
%>

<portlet:actionURL name="mySearchBar" var="portletURL" />

<div class="form-group">
    <form action="<%= portletURL %>" method="POST" name="example">
        <input
            name="<portlet:namespace/>queryString"
            type="text"
            value="${queryString}"
            />
        </input><br>
        <c:forEach items="${facetsWithAvailableTerms}" var="curFacet">
            <c:forEach items="${curFacet.value}" var="termEntry" varStatus="status">

            <%
            TermCollector termEntry = (TermCollector)pageContext.findAttribute("termEntry");
            %>

            <input name="<portlet:namespace/>${curFacet.key}" type="checkbox" value="${termEntry.term}" <%= userSelectedTermValues.contains(termEntry.getTerm())?"checked":"" %>> ${termEntry.term} (${termEntry.frequency})<br>
            </c:forEach>

            <br>
        </c:forEach>
        <div class="form-inline">
            <input  type="submit" class="btn btn-info" value="Submit">
            <c:if test="${not empty docFromSearchResults}" >
            <button type="button" class="btn btn-info pull-right" data-toggle="collapse" data-target="#<portlet:namespace/>scDiv">View Results Panel</button>
            </c:if>
        </div>
    </form>
</div>
<hr>
<div id="<portlet:namespace/>scDiv" class="collapse">
    <liferay-ui:search-container delta="10">
        <liferay-ui:search-container-results results="${docFromSearchResults}" />

        <liferay-ui:search-container-row
            className="com.liferay.portal.kernel.search.Document"
            keyProperty="UID"
            modelVar="doc"
        >
            <liferay-ui:search-container-column-text
                name="Entry Number"
                value='<%= ++curIndexForDocs +"" %>'
            />

            <liferay-ui:search-container-column-text
                name="Document Type"
                value="<%= doc.getFields().get(\"entryClassName\").getValue() %>"
            />

            <liferay-ui:search-container-column-text
                name="UID"
                property="UID"
            />
        </liferay-ui:search-container-row>

        <liferay-ui:search-iterator />
    </liferay-ui:search-container>
</div>
<div>
    <svg height="500" id="graph-container" width="960"></svg>
</div>

<%-- https://dev.liferay.com/develop/tutorials/-/knowledge_base/7-0/using-external-libraries#loading-libraries-as-browser-globals --%>
<script>
define._amd = define.amd;
define.amd = false;
</script>

<script src="https://d3js.org/d3.v4.min.js" type="text/javascript"></script>

<script>
define.amd = define._amd;
</script>

<script>
//d3.json(url[, callback])

var jsonData = ${jsonStringSearchResults};
jsonData = JSON.parse(jsonData);

var axisProperties = ${jsonStrAxisProperties}
axisProperties = JSON.parse(axisProperties);

var margins = {top: 20, right: 20, bottom: 150, left: 150};

renderHistogram("#graph-container", jsonData, axisProperties, margins)
//renderHistogram("#graph-container", jsonData, axisProperties, margins)
</script>