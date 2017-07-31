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
package search.facet.displayer.portlet.portlet;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.search.facet.util.FacetFactory;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;
import com.liferay.portal.search.web.internal.user.facet.portlet.UserFacetBuilder;
import com.liferay.portal.search.web.internal.user.facet.portlet.UserFacetFactory;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchContributor;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchRequest;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchResponse;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchSettings;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Shanon Mathai
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.header-portlet-javascript=/js/main.js",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=User Faceted Search Portlet",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.security-role-ref=power-user,user",
		Constants.SERVICE_RANKING + "=100"

	},
	service = {Portlet.class, PortletSharedSearchContributor.class}
)
public class SearchFacetDisplayerPortlet
	extends MVCPortlet implements PortletSharedSearchContributor {

	@Override
	public void contribute(
		PortletSharedSearchSettings portletSharedSearchSettings) {

		portletSharedSearchSettings.setKeywords(_queryString);
		portletSharedSearchSettings.setPaginationDelta(Integer.MAX_VALUE);

		SearchContext sc = portletSharedSearchSettings.getSearchContext();

		Facet f = _buildUserFacet(sc, _userFacetValues);

		portletSharedSearchSettings.addFacet(f);
	}

	private String _getUserFacetKey() {
		UserFacetBuilder userFacetBuilder = new UserFacetBuilder(
			userFacetFactory);

		return userFacetBuilder.build().getFieldName();
	}

	private Facet _buildUserFacet(SearchContext sc, String[] userNameValues) {
		UserFacetBuilder userFacetBuilder = new UserFacetBuilder(
			userFacetFactory);

		userFacetBuilder.setFrequencyThreshold(_FREQ_THRESHOLD);
		userFacetBuilder.setMaxTerms(_MAX_TERMS);
		userFacetBuilder.setSearchContext(sc);
		userFacetBuilder.setSelectedUsers(userNameValues);

		return userFacetBuilder.build();
	}

	private List<String> _getAllAvailableSearchFacets() {
		return SearchFacetTracker.getSearchFacets().stream()
			.map(SearchFacet::getFieldName)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@Override
	public void doView(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {
		PortletSharedSearchResponse portletSharedSearchResponse =
			portletSharedSearchRequest.search(renderRequest);

		List<String> allFacetNames = _getAllAvailableSearchFacets();

		List<Facet> allFacets = allFacetNames.stream()
				.map(portletSharedSearchResponse::getFacet)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		Map<String, List<TermCollector>> facetMapToTermsCollector =
			_getFacetWithTerms(allFacets);

		renderRequest.setAttribute(
			"facetsWithAvailableTerms", facetMapToTermsCollector);

		String userFacetKey = _getUserFacetKey();

		String[] selectValues = renderRequest.getParameterValues(userFacetKey);

		List<String> userSelectedTermValues;

		if (selectValues == null) {
			userSelectedTermValues = Collections.emptyList();
		}
		else {
			userSelectedTermValues = Arrays.asList(selectValues);
		}

		_userFacetValues = selectValues == null ? new String[0] : selectValues;

		renderRequest.setAttribute(
			"userSelectedTermValues", userSelectedTermValues);

		List<Document> docFromSearchResults =
			portletSharedSearchResponse.getDocuments();

		renderRequest.setAttribute("queryString", _queryString);

		renderRequest.setAttribute(
			"docFromSearchResults", docFromSearchResults);

		D3JsonTracker jsonTracker = new D3JsonTracker(
			"portletId", "numFields", "Number Of Fields", "d");

		facetMapToTermsCollector.values().forEach(
			termCollectors -> termCollectors.forEach(
            	tc -> jsonTracker.addEntry(
            		tc.getTerm(), "" + tc.getFrequency())
        ));

		String jsonStrAggregate = jsonTracker.getJsonList();

		renderRequest.setAttribute(
			"jsonStringSearchResults", jsonStrAggregate);

		String jsonStrAxisProperties = jsonTracker.getJsonAxisProperties();

		renderRequest.setAttribute(
			"jsonStrAxisProperties", jsonStrAxisProperties);

		super.doView(renderRequest, renderResponse);
	}

	private String _getJsonifiedDocuments(List<Document> docFromSearchResults)
		throws JsonProcessingException {
		return StringPool.APOSTROPHE +
			DocumentJsonifier.listToJson(docFromSearchResults) +
				StringPool.APOSTROPHE;
	}

	private Map<String, List<TermCollector>> _getFacetWithTerms(
		List<Facet> allFacets) {

		Map<String, List<TermCollector>> fwt = new HashMap<>();

		allFacets.forEach(
			facet -> {
				List<TermCollector> tcs = facet
					.getFacetCollector()
					.getTermCollectors()
					.stream()
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

				if (!tcs.isEmpty()) {
					fwt.put(facet.getFieldName(), tcs);
				}
			});

		return fwt;
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		unbind = "deleteFacetFactory"
	)
	protected void addFacetFactory(FacetFactory facetFactory) {
		availableFacetFactories.add(facetFactory);
	}

	protected void deleteFacetFactory(FacetFactory facetFactory) {
		availableFacetFactories.remove(facetFactory);
	}

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {
		String userFacetKey = _getUserFacetKey();

		String[] requestUserFacetValues = actionRequest.getParameterValues(
			userFacetKey);

		if (requestUserFacetValues == null) {
			_userFacetValues = new String[0];
		}
		else {
			_userFacetValues = requestUserFacetValues;
		}

		_queryString = actionRequest.getParameter("queryString");

		_queryString = _queryString == null ? "" : _queryString;

		actionResponse.setRenderParameter("queryString", _queryString);

		actionResponse.setRenderParameter(userFacetKey, _userFacetValues);
	}

	private static final int _FREQ_THRESHOLD = 1;

	private static final int _MAX_TERMS = 10;

	private static final String _MM = "{" +
			"axisxfield: 'portletId'," +
			"axisylabel: 'Number Of Each Term'," +
			"axisyfield: 'numFields'," +
			"axisyd3format: 'd'" +
			"}";

	@Reference
	protected PortletSharedSearchRequest portletSharedSearchRequest;

	protected List<FacetFactory> availableFacetFactories = new ArrayList<>();

	protected UserFacetFactory userFacetFactory = new UserFacetFactory();

	private String _queryString = StringPool.BLANK;

	private String[] _userFacetValues = new String[0];

	private static final Log _log = LogFactoryUtil.getLog(
		SearchFacetDisplayerPortlet.class);
}