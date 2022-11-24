package jano.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.http.HttpHeaders;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Resource;
import org.eclipse.jetty.http.MimeTypes;
import org.javatuples.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jano.builder.OntologyHandler;
import jano.view.VelocityRenderer;
import spark.Request;
import spark.Response;
import spark.Route;

public class ControllerOntology {

	public static OntologyHandler handler = new OntologyHandler();
	private static VelocityRenderer render = new VelocityRenderer();

	public static final Route test = (Request request, Response response) -> {
		String id = UUID.randomUUID().toString();
		Map<String, Object> map  =new HashMap<>();
		map.put("uuid", id);
		return render.render(map, "test.vm");
	};

	public static final Route loadOwl = (Request request, Response response) -> {
		String url = request.queryParams("url");
		if(url!=null && !url.isEmpty()) {
			try {
			handler.loadOntology(url);
			response.status(201);
			}catch(Exception e) {
				response.status(404);
				return "{ \"error\" : "+e.toString().substring(e.toString().indexOf(':')+1)+" }";
			}
		}else {
			response.status(404);
			return "{ \"error\" : \"Provide an non-empty ontology URL\" }";
		}
		JsonObject responseJson = new JsonObject();
		JsonArray array = new JsonArray();
		handler.getOwlClasses().parallelStream().map(elem -> elem.toString()).forEach(elem -> array.add(elem));
		responseJson.add("classes", array);
		return responseJson;
	};

	public static final Route listOwlClasses = (Request request, Response response) -> {
		response.header(HttpHeaders.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON_UTF_8.asString());

		VelocityRenderer render = new VelocityRenderer();
		Map<String, Object> map  =new HashMap<>();
		map.put("classes", handler.getOwlClasses().parallelStream().map(elem -> elem.toString()).collect(Collectors.toList()));
		return render.render(map, "ontology-menu.vm");
	};

	public static final Route topology = (Request request, Response response) -> {
		String url = request.queryParams("url");
		String explicitBase = request.queryParams("printBase");
		boolean printBase = (explicitBase ==null || explicitBase.isEmpty()) && Boolean.valueOf(explicitBase) ;

		if(url!=null && !url.isEmpty()) {
			try {
			JsonArray ontNodes = new JsonArray();
			JsonArray ontEdges = new JsonArray();
			OntologyHandler handler = new OntologyHandler();
			handler.loadOntology(url);

			List<OntClass> classes = handler.getOwlClasses();
			classes.parallelStream().map(node -> formatUri(handler, node, printBase)).map(node->nodeObject(node)).forEach(node -> ontNodes.add(node));
			//classes.parallelStream().map(node -> new Pair<>(node, handler.getNeighbours(node, true))).map(graph -> edgeObject(graph, handler,printBase)).filter(graph -> graph!=null).forEach(graph -> ontEdges.add(graph));
			JsonObject graph = new JsonObject();
			graph.add("nodes", ontNodes);
			graph.add("edges", ontEdges);
			return graph;
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	};

	private static final JsonObject nodeObject(String url) {
		JsonObject node = new JsonObject();
		JsonObject nodeObject = new JsonObject();
		nodeObject.addProperty("id", url);
		node.add("data", nodeObject);
		return node;
	}

	private static final JsonObject edgeObject(Pair<Resource, Map<Resource, Resource>> graph, OntologyHandler handler, Boolean printBase) {
		JsonObject node = new JsonObject();
		String source = formatUri(handler, graph.getValue0(), printBase);
		for(Entry<Resource,Resource> entry:graph.getValue1().entrySet()) {
			JsonObject nodeObject = new JsonObject();
			nodeObject.addProperty("source", source);
			String target = formatUri(handler, entry.getValue(), printBase);
			nodeObject.addProperty("target", target);
			String label = formatUri(handler, entry.getKey(), printBase);
			nodeObject.addProperty("label", label);
			node.add("data", nodeObject);
			return node;
		}
		System.out.println(graph.getValue0()+" does not have neighbours");
		return null;
	}

	private static final String formatUri(OntologyHandler handler, Resource resource, Boolean printBase) {
		StringBuilder builder = new StringBuilder();
		String prefix = handler.getOntologyModel().getNsURIPrefix(resource.getNameSpace());
		if(prefix==null || (prefix.isEmpty() && printBase))
			return resource.toString();
		builder.append(prefix);
		builder.append(":");
		builder.append(resource.getLocalName());
		return builder.toString();
	}
}
