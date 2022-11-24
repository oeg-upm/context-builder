package jano.main;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;

import com.google.gson.JsonObject;

import jano.builder.OntologyHandler;
import jano.builder.implementations.CBFactory;
import jano.builder.implementations.ContextBuilder;

public class Main {

	public static void main(String[] args) {
		// https://jena.apache.org/documentation/ontology/#general-concepts

		OntologyHandler handler = new OntologyHandler();
		handler.loadOntology("https://saref.etsi.org/core/v3.1.1/");


		OntClass device = handler.getOwlClass("https://saref.etsi.org/core/Device");
		OntClass tmpSensor = handler.getOwlClass("https://saref.etsi.org/core/TemperatureSensor");
		OntClass actuator = handler.getOwlClass("https://saref.etsi.org/core/Actuator");
		OntClass property = handler.getOwlClass("https://saref.etsi.org/core/Property");

		Model graph = ModelFactory.createDefaultModel();
		graph.add(tmpSensor, ResourceFactory.createProperty("https://saref.etsi.org/core/controlsProperty"), ResourceFactory.createResource("https://saref.etsi.org/core/Property"));
		graph.add(tmpSensor, ResourceFactory.createProperty("https://saref.etsi.org/core/measuresProperty"), ResourceFactory.createResource("https://saref.etsi.org/core/Property"));
		graph.add(tmpSensor, ResourceFactory.createProperty("https://saref.etsi.org/core/makesMeasurement"), ResourceFactory.createResource("https://saref.etsi.org/core/Measurement"));
		graph.add(ResourceFactory.createResource("https://saref.etsi.org/core/Measurement"), ResourceFactory.createProperty("https://saref.etsi.org/core/measurementMadeBy"), tmpSensor);


		ContextBuilder builder = CBFactory.build(null);
		JsonObject context = builder.buildContext(handler, tmpSensor.toString(), graph);
		System.out.println(context);
	}


}
