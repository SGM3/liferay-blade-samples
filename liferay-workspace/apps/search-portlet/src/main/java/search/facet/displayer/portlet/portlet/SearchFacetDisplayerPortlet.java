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
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.*;

import javax.portlet.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.header-portlet-javascript=/js/main.js",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=search-facet-displayer-portlet Portlet",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.security-role-ref=power-user,user",
		Constants.SERVICE_RANKING + "=100"

	},
	service = {Portlet.class, PortletSharedSearchContributor.class}
)
public class SearchFacetDisplayerPortlet extends MVCPortlet implements PortletSharedSearchContributor {

	@Override
	public void contribute(PortletSharedSearchSettings portletSharedSearchSettings) {
		portletSharedSearchSettings.setKeywords(queryString);
		portletSharedSearchSettings.setPaginationDelta(Integer.MAX_VALUE);
		SearchContext sc = portletSharedSearchSettings.getSearchContext();

		Facet f = buildUserFacet(sc);
		portletSharedSearchSettings.addFacet(f);
	}

	private String getUserFacetKey(){
		UserFacetBuilder userFacetBuilder = new UserFacetBuilder(
				userFacetFactory);
		return userFacetBuilder.build().getFieldName();
	}

	private Facet buildUserFacet(SearchContext sc) {
		UserFacetBuilder userFacetBuilder = new UserFacetBuilder(
				userFacetFactory);

		userFacetBuilder.setFrequencyThreshold(FREQ_THRESHOLD);
		userFacetBuilder.setMaxTerms(MAX_TERMS);
		userFacetBuilder.setSearchContext(sc);

		return userFacetBuilder.build();
	}

	private Facet buildUserFacet(SearchContext sc, String[] userNameValues) {
		UserFacetBuilder userFacetBuilder = new UserFacetBuilder(
				userFacetFactory);

		userFacetBuilder.setFrequencyThreshold(FREQ_THRESHOLD);
		userFacetBuilder.setMaxTerms(MAX_TERMS);
		userFacetBuilder.setSearchContext(sc);
		userFacetBuilder.setSelectedUsers(userNameValues);

		return userFacetBuilder.build();
	}

	private List<String> getAllAvailableSearchFacets() {
		return SearchFacetTracker.getSearchFacets().stream()
			.map(SearchFacet::getFieldName)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
		PortletSharedSearchResponse portletSharedSearchResponse =
			portletSharedSearchRequest.search(renderRequest);

		List<String> allFacetNames = getAllAvailableSearchFacets();

		List<Facet> allFacets = allFacetNames.stream()
				.map(portletSharedSearchResponse::getFacet)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());


		Map<String,List<TermCollector>> facetMapToTermsCollector =
				getFacetWithTerms(allFacets);
		renderRequest.setAttribute("facetsWithAvailableTerms",
				facetMapToTermsCollector);

		String userFacetKey = getUserFacetKey();
		String[] selectValues = renderRequest.getParameterValues(userFacetKey);
		List<String> userSelectedTermValues =
			selectValues==null?Arrays.asList():Arrays.asList(selectValues);

		renderRequest.setAttribute("userSelectedTermValues", userSelectedTermValues);

		List<Document> docFromSearchResults =
			portletSharedSearchResponse.getDocuments();

		String jsonStr = getJsonifiedDocuments(docFromSearchResults);

		//facetMapToTermsCollector.get("").get(0).
		renderRequest.setAttribute("docFromSearchResults",
			docFromSearchResults);
		renderRequest.setAttribute("jsonStringSearchResults", jsonStr);
		renderRequest.setAttribute("queryString", queryString);
		super.doView(renderRequest, renderResponse);
	}

	private String getJsonifiedDocuments(List<Document> docFromSearchResults) throws JsonProcessingException {
		return StringPool.APOSTROPHE
            + DocumentJsonifier.listToJson(docFromSearchResults)
            + StringPool.APOSTROPHE;
	}

	private Map<String,List<TermCollector>> getFacetWithTerms(List<Facet> allFacets) {
		Map<String,List<TermCollector>> fwt = new HashMap<>();

		allFacets.forEach(facet -> {
			List<TermCollector> tcs =
				facet.getFacetCollector().getTermCollectors().stream()
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			if (!tcs.isEmpty())
				fwt.put(facet.getFieldName(), tcs);
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
	public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
		String userFacetKey = getUserFacetKey();
		String [] requestUserFacetValues = actionRequest.getParameterValues(userFacetKey);
		if (requestUserFacetValues != null){
			userFacetValues = requestUserFacetValues;
		}
		queryString = actionRequest.getParameter("queryString");
		actionResponse.setRenderParameter("queryString", queryString);
		actionResponse.setRenderParameter(userFacetKey, userFacetValues);
	}

	private static final int FREQ_THRESHOLD = 1;

	private static final int MAX_TERMS = 10;


	@Reference
	protected PortletSharedSearchRequest portletSharedSearchRequest;

	protected List<FacetFactory> availableFacetFactories = new ArrayList<>();

	protected UserFacetFactory userFacetFactory = new UserFacetFactory();

	private String queryString = StringPool.BLANK;

	private String[] userFacetValues = new String[0];

	private static final Log _log = LogFactoryUtil.getLog(SearchFacetDisplayerPortlet.class);
}