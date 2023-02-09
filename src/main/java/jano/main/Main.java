package jano.main;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;

import com.google.gson.JsonObject;

import jano.builder.OntologyHandler;
import jano.builder.implementations.CBFactory;
import jano.builder.implementations.ContextBuilder;
import spark.ModelAndView;
import spark.Request;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static spark.Spark.*;

public class Main {

	private static OntologyHandler handler;

	public static void main(String[] args) {
		// https://jena.apache.org/documentation/ontology/#general-concepts

		handler = new OntologyHandler();
		//handler.loadOntology("https://saref.etsi.org/core/v3.1.1/");
		//handler.loadOntology("https://auroral.iot.linkeddata.es/def/adapters/ontology.ttl");
		//handler.loadOntology("https://auroral.iot.linkeddata.es/def/core/ontology.ttl");

		//handler.getOwlClasses().parallelStream().forEach(elem -> System.out.println(elem));
		//System.out.println(handler.getOwlClasses().size());

		OntClass device = handler.getOwlClass("https://auroral.iot.linkeddata.es/def/core#Device");

		//OntClass tmpSensor = handler.getOwlClass("https://saref.etsi.org/core/TemperatureSensor");
		//OntClass actuator = handler.getOwlClass("https://saref.etsi.org/core/Actuator");
		//OntClass property = handler.getOwlClass("https://saref.etsi.org/core/Property");

		//Model graph = ModelFactory.createDefaultModel();
		//graph.add(device, ResourceFactory.createProperty("https://saref.etsi.org/core/makesMeasurement"), ResourceFactory.createResource("https://saref.etsi.org/core/Measurement"));
		/*graph.add(tmpSensor, ResourceFactory.createProperty("https://saref.etsi.org/core/measuresProperty"), ResourceFactory.createResource("https://saref.etsi.org/core/Property"));
		graph.add(tmpSensor, ResourceFactory.createProperty("https://saref.etsi.org/core/makesMeasurement"), ResourceFactory.createResource("https://saref.etsi.org/core/Measurement"));
		graph.add(ResourceFactory.createResource("https://saref.etsi.org/core/Measurement"), ResourceFactory.createProperty("https://saref.etsi.org/core/measurementMadeBy"), tmpSensor);*/

		//graph.add(property, ResourceFactory.createProperty("https://saref.etsi.org/core/makesMeasurement"), ResourceFactory.createResource("https://saref.etsi.org/core/Measurement"));
		//graph.add(ResourceFactory.createResource("https://saref.etsi.org/core/Measurement"), ResourceFactory.createProperty("https://saref.etsi.org/core/measurementMadeBy"), tmpSensor);


		//ContextBuilder builder = CBFactory.build(null);
		//JsonObject context = builder.buildContext(handler, device.toString(), graph);
		//System.out.println(context);

		exception(Exception.class, (e, req, res) -> e.printStackTrace()); // print all exceptions
		staticFiles.location("/public");
		port(9999);
		get("/", (req, res) -> renderTodos(req));
		get("/api/ontology", (req, res) -> renderEditTodo(req));
		post("/api/ontology", (req, res) -> handleRequest(req));
		get("/api/ontology/classes", (req, res) -> handleRequest2(req));
	}

	private static String renderTodos(Request req) {
		String statusStr = req.queryParams("status");
		Map<String, Object> model = new HashMap<>();
		return renderTemplate("home.vm", model);
	}

	private static String renderTemplate(String template, Map model) {
		return new VelocityTemplateEngine().render(new ModelAndView(model, template));
	}

	private static String renderEditTodo(Request req) {
		System.out.println(req.queryParams("url"));
		return renderTemplate("ontology-menu.vm", new HashMap(){});
	}


	private static String handleRequest(Request request){
		handler.loadOntology(request.queryParams("url"));
		String base = handler.getBase();
		String ontologyName = request.queryParams("url");
		String ontologyURL = request.queryParams("url");

		for (String key : handler.getOntologyModel().getNsPrefixMap().keySet()) {
			String url = handler.getOntologyModel().getNsPrefixMap().get(key);
			if(url.equals(base) && key.length()>0)
				ontologyName = key;

		}

		Map<String, Object> model = new HashMap<>();
		model.put("ontology-url", ontologyURL);
		model.put("ontology-name", ontologyName);
		return new VelocityTemplateEngine().render(new ModelAndView(model, "ontology-source-item.vm"));
	}

	private static String handleRequest2(Request request){

		Map<String, Object> model = new HashMap<>();
		model.put("classes", handler.getOwlClasses());
		System.out.println(handler.getOntologyModel().toString());
		for(OntClass ontClass: handler.getOwlClasses()){
			System.out.println(ontClass.toString());
			System.out.println(ontClass.getURI());
			System.out.println("====--------====");
		}
		return new VelocityTemplateEngine().render(new ModelAndView(model, "ontology-class-item.vm"));
	}

}
