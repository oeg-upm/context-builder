package jano.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;

import com.google.gson.JsonObject;

import jano.builder.OntologyHandler;
import jano.builder.implementations.CBFactory;
import jano.builder.implementations.ContextBuilder;

public class Main2 {

	public static void main(String[] args) {
		// https://jena.apache.org/documentation/ontology/#general-concepts

		OntologyHandler handler = new OntologyHandler();
		handler.loadOntology("https://auroral.iot.linkeddata.es/def/tour/ontology.ttl");


		Model graph = ModelFactory.createDefaultModel();
		//graph.add( ResourceFactory.createResource("https://auroral.iot.linkeddata.es/def/tourism#Activity"), ResourceFactory.createProperty("https://auroral.iot.linkeddata.es/def/tourism#hasDifficulty"), ResourceFactory.createResource("https://auroral.iot.linkeddata.es/def/tourism#ActivityDifficulty"));
		//graph.add( ResourceFactory.createResource("https://auroral.iot.linkeddata.es/def/tourism#Activity"), ResourceFactory.createProperty("https://auroral.iot.linkeddata.es/def/tourism#hasStartingPrice"), ResourceFactory.createResource("https://auroral.iot.linkeddata.es/def/tourism#Price"));
		graph.add( ResourceFactory.createResource("https://auroral.iot.linkeddata.es/def/tourism#Activity"), ResourceFactory.createProperty("https://auroral.iot.linkeddata.es/def/tourism#hasStartingPrice"), ResourceFactory.createResource("https://auroral.iot.linkeddata.es/def/tourism#Price"));


		ContextBuilder builder = CBFactory.build(null);
		JsonObject context = builder.buildContext(handler, "https://auroral.iot.linkeddata.es/def/tourism#Activity", graph);
		System.out.println(context);
	}


}
