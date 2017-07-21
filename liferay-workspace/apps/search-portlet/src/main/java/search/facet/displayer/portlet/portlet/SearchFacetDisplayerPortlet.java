package search.facet.displayer.portlet.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacet;
import com.liferay.portal.kernel.search.facet.ScopeFacet;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchContributor;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchRequest;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchResponse;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchSettings;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.*;
import java.io.IOException;
import java.util.List;

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
		portletSharedSearchSettings.addFacet(new ScopeFacet(portletSharedSearchSettings.getSearchContext()));
		portletSharedSearchSettings.addFacet(new AssetEntriesFacet(portletSharedSearchSettings.getSearchContext()));
	}

	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
		PortletSharedSearchResponse portletSharedSearchResponse =
			portletSharedSearchRequest.search(renderRequest);

		List<Document> docFromSearchResults = portletSharedSearchResponse.getDocuments();
		String jsonStr = '\''+ DocumentJsonifier.listToJson(docFromSearchResults) + '\'';

		renderRequest.setAttribute("docFromSearchResults", docFromSearchResults);
		renderRequest.setAttribute("jsonStringSearchResults", jsonStr);

		renderRequest.setAttribute("queryString", queryString);
		super.doView(renderRequest, renderResponse);
	}

	@Override
	public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
		queryString = actionRequest.getParameter("queryString");
		actionResponse.setRenderParameter("queryString", queryString);
	}

	@Reference
	protected PortletSharedSearchRequest portletSharedSearchRequest;

	private String queryString = StringPool.BLANK;

	private static final Log _log = LogFactoryUtil.getLog(SearchFacetDisplayerPortlet.class);
}