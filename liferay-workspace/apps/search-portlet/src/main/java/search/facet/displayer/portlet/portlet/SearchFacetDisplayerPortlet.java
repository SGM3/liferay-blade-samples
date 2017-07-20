package search.facet.displayer.portlet.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacet;
import com.liferay.portal.kernel.search.facet.ScopeFacet;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.internal.display.context.SearchDisplayContext;
import com.liferay.portal.search.web.internal.display.context.SearchDisplayContextFactory;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchContributor;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchRequest;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchResponse;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchSettings;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

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
		SearchDisplayContext sdc =
			searchDisplayContextFactory.create(
				renderRequest,renderResponse, renderRequest.getPreferences());

		List<SearchFacet> enabledSearchFacets = sdc.getEnabledSearchFacets();

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

	private List<Document> _mockResultSet(int count){
		List<Document> mockResultSet = new ArrayList<>();
		while (count-- > 0) {
			mockResultSet.add(
				new Document() {
				@Override
				public void add(Field field) {

				}

				@Override
				public void addDate(String s, Date date) {

				}

				@Override
				public void addDate(String s, Date[] dates) {

				}

				@Override
				public void addFile(String s, byte[] bytes, String s1) throws IOException {

				}

				@Override
				public void addFile(String s, File file, String s1) throws IOException {

				}

				@Override
				public void addFile(String s, InputStream inputStream, String s1) throws IOException {

				}

				@Override
				public void addFile(String s, InputStream inputStream, String s1, int i) throws IOException {

				}

				@Override
				public void addGeoLocation(double v, double v1) {

				}

				@Override
				public void addKeyword(String s, boolean b) {

				}

				@Override
				public void addKeyword(String s, Boolean aBoolean) {

				}

				@Override
				public void addKeyword(String s, boolean[] booleans) {

				}

				@Override
				public void addKeyword(String s, Boolean[] booleans) {

				}

				@Override
				public void addKeyword(String s, double v) {

				}

				@Override
				public void addKeyword(String s, Double aDouble) {

				}

				@Override
				public void addKeyword(String s, double[] doubles) {

				}

				@Override
				public void addKeyword(String s, Double[] doubles) {

				}

				@Override
				public void addKeyword(String s, float v) {

				}

				@Override
				public void addKeyword(String s, Float aFloat) {

				}

				@Override
				public void addKeyword(String s, float[] floats) {

				}

				@Override
				public void addKeyword(String s, Float[] floats) {

				}

				@Override
				public void addKeyword(String s, int i) {

				}

				@Override
				public void addKeyword(String s, int[] ints) {

				}

				@Override
				public void addKeyword(String s, Integer integer) {

				}

				@Override
				public void addKeyword(String s, Integer[] integers) {

				}

				@Override
				public void addKeyword(String s, long l) {

				}

				@Override
				public void addKeyword(String s, Long aLong) {

				}

				@Override
				public void addKeyword(String s, long[] longs) {

				}

				@Override
				public void addKeyword(String s, Long[] longs) {

				}

				@Override
				public void addKeyword(String s, short i) {

				}

				@Override
				public void addKeyword(String s, Short aShort) {

				}

				@Override
				public void addKeyword(String s, short[] shorts) {

				}

				@Override
				public void addKeyword(String s, Short[] shorts) {

				}

				@Override
				public void addKeyword(String s, String s1) {

				}

				@Override
				public void addKeyword(String s, String s1, boolean b) {

				}

				@Override
				public void addKeyword(String s, String[] strings) {

				}

				@Override
				public void addKeywordSortable(String s, Boolean aBoolean) {

				}

				@Override
				public void addKeywordSortable(String s, Boolean[] booleans) {

				}

				@Override
				public void addKeywordSortable(String s, String s1) {

				}

				@Override
				public void addKeywordSortable(String s, String[] strings) {

				}

				@Override
				public void addLocalizedKeyword(String s, Map<Locale, String> map) {

				}

				@Override
				public void addLocalizedKeyword(String s, Map<Locale, String> map, boolean b) {

				}

				@Override
				public void addLocalizedKeyword(String s, Map<Locale, String> map, boolean b, boolean b1) {

				}

				@Override
				public void addLocalizedText(String s, Map<Locale, String> map) {

				}

				@Override
				public void addNumber(String s, BigDecimal bigDecimal) {

				}

				@Override
				public void addNumber(String s, BigDecimal[] bigDecimals) {

				}

				@Override
				public void addNumber(String s, double v) {

				}

				@Override
				public void addNumber(String s, Double aDouble) {

				}

				@Override
				public void addNumber(String s, double[] doubles) {

				}

				@Override
				public void addNumber(String s, Double[] doubles) {

				}

				@Override
				public void addNumber(String s, float v) {

				}

				@Override
				public void addNumber(String s, Float aFloat) {

				}

				@Override
				public void addNumber(String s, float[] floats) {

				}

				@Override
				public void addNumber(String s, Float[] floats) {

				}

				@Override
				public void addNumber(String s, int i) {

				}

				@Override
				public void addNumber(String s, int[] ints) {

				}

				@Override
				public void addNumber(String s, Integer integer) {

				}

				@Override
				public void addNumber(String s, Integer[] integers) {

				}

				@Override
				public void addNumber(String s, long l) {

				}

				@Override
				public void addNumber(String s, Long aLong) {

				}

				@Override
				public void addNumber(String s, long[] longs) {

				}

				@Override
				public void addNumber(String s, Long[] longs) {

				}

				@Override
				public void addNumber(String s, String s1) {

				}

				@Override
				public void addNumber(String s, String[] strings) {

				}

				@Override
				public void addNumberSortable(String s, BigDecimal bigDecimal) {

				}

				@Override
				public void addNumberSortable(String s, BigDecimal[] bigDecimals) {

				}

				@Override
				public void addNumberSortable(String s, Double aDouble) {

				}

				@Override
				public void addNumberSortable(String s, Double[] doubles) {

				}

				@Override
				public void addNumberSortable(String s, Float aFloat) {

				}

				@Override
				public void addNumberSortable(String s, Float[] floats) {

				}

				@Override
				public void addNumberSortable(String s, Integer integer) {

				}

				@Override
				public void addNumberSortable(String s, Integer[] integers) {

				}

				@Override
				public void addNumberSortable(String s, Long aLong) {

				}

				@Override
				public void addNumberSortable(String s, Long[] longs) {

				}

				@Override
				public void addText(String s, String s1) {

				}

				@Override
				public void addText(String s, String[] strings) {

				}

				@Override
				public void addTextSortable(String s, String s1) {

				}

				@Override
				public void addTextSortable(String s, String[] strings) {

				}

				@Override
				public void addUID(String s, long l) {

				}

				@Override
				public void addUID(String s, long l, String s1) {

				}

				@Override
				public void addUID(String s, Long aLong) {

				}

				@Override
				public void addUID(String s, Long aLong, String s1) {

				}

				@Override
				public void addUID(String s, String s1) {

				}

				@Override
				public void addUID(String s, String s1, String s2) {

				}

				@Override
				public void addUID(String s, String s1, String s2, String s3) {

				}

				@Override
				public void addUID(String s, String s1, String s2, String s3, String s4) {

				}

				@Override
				public String get(Locale locale, String s) {
					return null;
				}

				@Override
				public String get(Locale locale, String s, String s1) {
					return null;
				}

				@Override
				public String get(String s) {
					return null;
				}

				@Override
				public String get(String s, String s1) {
					return null;
				}

				@Override
				public Date getDate(String s) throws ParseException {
					return null;
				}

				@Override
				public Field getField(String s) {
					return null;
				}

				@Override
				public Map<String, Field> getFields() {
					Map<String, Field> fds = new HashMap<>();
					int count = (int)(Math.random() * 100);
					while (count-- > 0){
						fds.put(""+ count, null);
					}
					return fds;
				}

				@Override
				public String getPortletId() {
					return "_portlet_id_"+ ((int)(10000 * Math.random())) + "_";
				}

				@Override
				public String getUID() {
					return ((int)(10000000 * Math.random())) + "";
				}

				@Override
				public String[] getValues(String s) {
					return new String[0];
				}

				@Override
				public boolean hasField(String s) {
					return false;
				}

				@Override
				public boolean isDocumentSortableTextField(String s) {
					return false;
				}

				@Override
				public void remove(String s) {

				}

				@Override
				public void setSortableTextFields(String[] strings) {

				}

				@Override
				public Object clone() {
					return null;
				}
			}
			);
		}
		return  mockResultSet;
	}

	@Reference
	protected PortletSharedSearchRequest portletSharedSearchRequest;
	@Reference
	protected SearchDisplayContextFactory searchDisplayContextFactory;

	private String queryString = StringPool.BLANK;

	private static final Log _log = LogFactoryUtil.getLog(SearchFacetDisplayerPortlet.class);
}