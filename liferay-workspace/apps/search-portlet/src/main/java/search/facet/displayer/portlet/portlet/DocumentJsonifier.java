package search.facet.displayer.portlet.portlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;

import java.util.List;
import java.util.Map;

public abstract class DocumentJsonifier {

	public static String listToJson(List<Document> docs)
		throws JsonProcessingException {

		return listToJson(docs, false);
	}

	public static String listToJson(List<Document> docs, boolean pretty)
		throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();

		ArrayNode topLevelArray = mapper.createArrayNode();

		docs.stream().forEach(d -> {
			topLevelArray.add(toJsonNode(mapper, d));
		});
		ObjectNode topLevelElemnt = mapper.createObjectNode();

		topLevelElemnt.set("values", topLevelArray);

		if (pretty) {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(topLevelElemnt);
		}
		else {
			return mapper.writer().writeValueAsString(topLevelElemnt);
		}
	}

	private static ObjectNode toJsonNode(ObjectMapper mapper, Document doc) {
		Map<String, Field> fields = doc.getFields();
		ObjectNode docNode = mapper.createObjectNode();

		docNode.put("UID", doc.getUID());
		docNode.put("portletId", doc.getPortletId());
		docNode.put("numFields", fields == null?0:fields.size());

		return docNode;
	}

}