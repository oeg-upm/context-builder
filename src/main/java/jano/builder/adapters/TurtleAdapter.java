package jano.builder.adapters;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

import jano.builder.BuilderConfiguration;
import jano.builder.OntologyHandler;
import jano.builder.exceptions.ReaderException;

public class TurtleAdapter implements Adapter {


	@Override
	public Model adapt(String ... args) throws ReaderException {
		if(args==null || args.length == 0)
			throw new IllegalArgumentException("TurtleAdapter needs one argument as input, which must be an ontology in turtle annotated");
		String ttl = args[0];
		OntologyHandler handler = createHandler(ttl) ;
		BuilderConfiguration config = createConfig(handler);

		Model graph = buildGraph(handler, handler.getOwlClass(config.getRoot()), config.isTraverseParents(), Sets.newHashSet());
		return graph;
	}



	private BuilderConfiguration createConfig(OntologyHandler handler) throws ReaderException {
		Resource root = findRoot(handler);
		BuilderConfiguration config = new BuilderConfiguration();

		config.setBase(handler.getBase());
		config.setRoot(root.toString());
		return config;
	}

	private Model buildGraph(OntologyHandler handler, OntClass root, Boolean traverseParents, Set<String> visitedNodes) {
		Model graph = ModelFactory.createDefaultModel();

		Map<Resource, Resource> neighbours = handler.getNeighbours(root, traverseParents);
		if(root.toString().endsWith("ConstructorSpecification"))
			System.out.println("gerte");
		for(Entry<Resource, Resource> entry: neighbours.entrySet()) {
			Resource property = entry.getKey();
			Resource object = entry.getValue();
			try {
				OntClass ontClass = handler.getOwlClass(object.toString());
				//if(!visitedNodes.contains(object.toString())) { TODO: no sirve este criterio de ciclo
					visitedNodes.add(root.toString());
					graph.add(root, ResourceFactory.createProperty(property.toString()), object);
					Model subGraph = buildGraph( handler,  ontClass, traverseParents, visitedNodes);
					graph.add(subGraph);
				//}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		return graph;
	}



	private OntologyHandler createHandler(String ttl) {
		OntologyHandler handler = new OntologyHandler();
		handler.readOntology(ttl);
		return handler;
	}

	public Resource findRoot(OntologyHandler handler) throws ReaderException {
		List<Statement> stmts = handler.getOntologyModel().listStatements(null, RDF.type, ResourceFactory.createProperty("http://jano.org/def#Root")).toList();
		if(stmts.isEmpty()) {
			throw new ReaderException("Turtle reader expects one owl:Class to be labeled as http://jano.org/def#Root for acting as root node for the @context, none was provided");
		}else if(stmts.size() > 1) {
			throw new ReaderException("Turtle reader expects one owl:Class to be labeled as http://jano.org/def#Root for acting as root node for the @context, multiple where provided: "+stmts.toString());
		}else {
			return stmts.get(0).getSubject();
		}
	}



}
