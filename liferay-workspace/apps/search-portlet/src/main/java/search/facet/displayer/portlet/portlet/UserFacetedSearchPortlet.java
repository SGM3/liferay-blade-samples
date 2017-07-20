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

import static search.facet.displayer.constants.UserFacetedSearchConstants.DOC_SEARCH_RESULTS_PNAME;
import static search.facet.displayer.constants.UserFacetedSearchConstants.FACETS_WITH_TERMS_PNAME;
import static search.facet.displayer.constants.UserFacetedSearchConstants.JSON_AXIS_PROPS_PNAME;
import static search.facet.displayer.constants.UserFacetedSearchConstants.JSON_SEARCH_RESULTS_PNAME;
import static search.facet.displayer.constants.UserFacetedSearchConstants.QUERY_STRING_PNAME;
import static search.facet.displayer.constants.UserFacetedSearchConstants.USER_SELECTED_TERM_PNAME;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
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

import java.util.*;
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

import search.facet.displayer.util.D3JsonTracker;

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
		"javax.portlet.portlet.info.keywords=User Faceted Search Portlet",
		"javax.portlet.portlet.info.short-title=User Faceted Search Portlet",
		"javax.portlet.portlet.info.title=User Faceted Search Portlet",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.security-role-ref=power-user,user",
		Constants.SERVICE_RANKING + "=100"

	},
	service = {Portlet.class, PortletSharedSearchContributor.class}
)
public class UserFacetedSearchPortlet
	extends MVCPortlet implements PortletSharedSearchContributor {

	@Override
	public void contribute(
		PortletSharedSearchSettings portletSharedSearchSettings) {

		if (_log.isDebugEnabled()) {
			_log.debug("Set shared query keyword to " + _queryString);
		}

		portletSharedSearchSettings.setKeywords(_queryString);
		portletSharedSearchSettings.setPaginationDelta(Integer.MAX_VALUE);

		SearchContext sc = portletSharedSearchSettings.getSearchContext();

		Facet f = _buildUserFacet(sc, _userFacetSelectedTerms);

		if (_log.isDebugEnabled()) {
			_log.debug(
				"User facet being added: "
				+ Arrays.toString(_userFacetSelectedTerms));
		}

		portletSharedSearchSettings.addFacet(f);
	}

	private String _getUserFacetKey() {
		UserFacetBuilder userFacetBuilder = new UserFacetBuilder(
			_userFacetFactory);

		Facet facet = userFacetBuilder.build();

		return facet.getFieldName();
	}

	private Facet _buildUserFacet(
		SearchContext sc, String[] userNameSelectedTerms) {

		UserFacetBuilder userFacetBuilder = new UserFacetBuilder(
			_userFacetFactory);

		userFacetBuilder.setFrequencyThreshold(_FREQ_THRESHOLD);
		userFacetBuilder.setMaxTerms(_MAX_TERMS);
		userFacetBuilder.setSearchContext(sc);
		userFacetBuilder.setSelectedUsers(userNameSelectedTerms);

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

		if (_log.isDebugEnabled()) {
			_log.debug("Searching being executed");
		}

		PortletSharedSearchResponse portletSharedSearchResponse =
			_portletSharedSearchRequest.search(renderRequest);

		List<String> allFacetNames = _getAllAvailableSearchFacets();

		List<Facet> allFacets = allFacetNames.stream()
				.map(portletSharedSearchResponse::getFacet)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		Map<String, List<TermCollector>> facetMapToTermsCollector =
			_getFacetWithTerms(allFacets);

		renderRequest.setAttribute(
			FACETS_WITH_TERMS_PNAME, facetMapToTermsCollector);

		String userFacetKey = _getUserFacetKey();

		String[] selectValues = renderRequest.getParameterValues(userFacetKey);

		List<String> userSelectedTermValues;

		if (selectValues == null) {
			userSelectedTermValues = Collections.emptyList();
		}
		else {
			userSelectedTermValues = Arrays.asList(selectValues);
		}

		_userFacetSelectedTerms =
			selectValues == null ? new String[0] : selectValues;

		renderRequest.setAttribute(
			USER_SELECTED_TERM_PNAME, userSelectedTermValues);

		List<Document> docFromSearchResults =
			portletSharedSearchResponse.getDocuments();

		renderRequest.setAttribute(QUERY_STRING_PNAME, _queryString);

		renderRequest.setAttribute(
			DOC_SEARCH_RESULTS_PNAME, docFromSearchResults);

		D3JsonTracker jsonTracker = new D3JsonTracker(
			"userName", "numDocs", "Number Of Documents by User", "d");

		_populateAggregateJson(
			facetMapToTermsCollector, docFromSearchResults, jsonTracker);

		String jsonStrAggregate = jsonTracker.getJsonList();

		renderRequest.setAttribute(JSON_SEARCH_RESULTS_PNAME, jsonStrAggregate);

		String jsonStrAxisProperties = jsonTracker.getJsonAxisProperties();

		renderRequest.setAttribute(
			JSON_AXIS_PROPS_PNAME, jsonStrAxisProperties);

		super.doView(renderRequest, renderResponse);
	}

	private void _populateAggregateJson(
			Map<String, List<TermCollector>> facetMapToTermsCollector,
			List<Document> docFromSearchResults, D3JsonTracker jsonTracker) {

		for (List<TermCollector> tc: facetMapToTermsCollector.values()){

			for (TermCollector t : tc) {
				String term =
					StringPool.OPEN_BRACKET + t.getTerm()
						+ StringPool.CLOSE_BRACKET;

				long c = docFromSearchResults.stream()
					.map(Object::toString)
					.filter(x -> x.contains(term))
					.count();

				jsonTracker.addEntry(term, Long.toString(c));
			}
		}
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

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {
		String userFacetKey = _getUserFacetKey();

		String[] requestUserFacetValues = actionRequest.getParameterValues(
			userFacetKey);

		if (requestUserFacetValues == null) {
			_userFacetSelectedTerms = new String[0];
		}
		else {
			_userFacetSelectedTerms = requestUserFacetValues;
		}

		_queryString = actionRequest.getParameter(QUERY_STRING_PNAME);

		_queryString = _queryString == null ? StringPool.BLANK : _queryString;

		actionResponse.setRenderParameter(QUERY_STRING_PNAME, _queryString);

		actionResponse.setRenderParameter(
			userFacetKey, _userFacetSelectedTerms);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UserFacetedSearchPortlet.class);

	private static final int _FREQ_THRESHOLD = 1;

	private static final int _MAX_TERMS = 10;

	@Reference
	private PortletSharedSearchRequest _portletSharedSearchRequest;

	private UserFacetFactory _userFacetFactory = new UserFacetFactory();

	private String _queryString = StringPool.BLANK;

	private String[] _userFacetSelectedTerms = new String[0];
}