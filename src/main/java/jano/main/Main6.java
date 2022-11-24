package jano.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

import com.google.gson.JsonObject;

import jano.builder.OntologyHandler;
import jano.builder.adapters.TurtleAdapter;
import jano.builder.implementations.CBFactory;
import jano.builder.implementations.ContextBuilder;

public class Main6 {

	static String model = "";
	public static void main(String[] args) throws Exception {
		// https://jena.apache.org/documentation/ontology/#general-concepts
		Model model = ModelFactory.createDefaultModel();
		model.read(new FileInputStream(new File("/Users/andreacimmino/Desktop/adapters.ttl")), null, "TURTLE");
		model.add(ResourceFactory.createResource("https://auroral.iot.linkeddata.es/def/core#Sensor"), RDF.type,  ResourceFactory.createResource("http://jano.org/def#Root"));
		Writer w = new StringWriter();
		model.write(w, "TURTLE");
		TurtleAdapter adapter = new TurtleAdapter();
		System.out.println(w.toString());
		Model graph = adapter.adapt(w.toString());
		graph.write(System.out, "Turtle");
		w.flush();

		Resource subject = model.listSubjectsWithProperty( RDF.type, ResourceFactory.createResource("http://jano.org/def#Root")).next();
		System.out.println("Root node: " + subject);



		Model ontology = graph.remove(subject, RDF.type, ResourceFactory.createResource("http://jano.org/def#Root"));
		ontology.write(w, "TURTLE");
		OntologyHandler handler = new OntologyHandler();
		handler.readOntology(w.toString());

		ContextBuilder builder = CBFactory.build(null);
		JsonObject context = builder.buildContext(handler, subject.toString(), graph);
		System.out.println(context);
	}


}
