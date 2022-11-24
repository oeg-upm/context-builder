package jano.builder.implementations;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import com.google.gson.JsonObject;

import jano.builder.OntologyHandler;

class AdvancedContextBuilder extends BasicContextBuilder {


	protected static final String TAG_CONTAINER = "@container";
	private static final String TAG_INDEX = "@index";

	public AdvancedContextBuilder() {
		super();
	}

	@Override
	protected JsonObject maObjectPropertyToContext(OntologyHandler handler, Resource objectProperty, Resource targetOwlClass, Model graph) {
		JsonObject definitions = buildContextNode(objectProperty, targetOwlClass);

		// RECURSIVE
		JsonObject nestedContext = buildContext(handler, targetOwlClass.toString(), graph);
		if (nestedContext != null) {
			JsonObject nestedAux = nestedContext.remove(TAG_CONTEXT).getAsJsonObject();
			if (!nestedAux.keySet().isEmpty())
				definitions.add(TAG_CONTEXT, nestedAux);
		}
		// --
		String label = extractLabel(handler.getOntologyModel(), objectProperty);
		JsonObject partialContext = new JsonObject();
		partialContext.add(label, definitions);
		return partialContext;
	}

	private JsonObject buildContextNode(Resource objectProperty, Resource targetOwlClass) {
		JsonObject definitions = new JsonObject();
		definitions.addProperty(TAG_ID, objectProperty.toString());
		//
		Optional<Triple<Property, Resource, Property>> isIndexedNodeOpt = configuration.getIndexedNodes().parallelStream().filter(elem -> elem.getLeft().equals(objectProperty) && elem.getMiddle().equals(targetOwlClass)).findFirst();
				if(isIndexedNodeOpt.isPresent()) {// indexed option
					Triple<Property, Resource, Property> indexedNode = isIndexedNodeOpt.get();
					definitions.addProperty(TAG_CONTAINER, TAG_INDEX);
					// TODO: check that the property belongs to this class
					if(indexedNode.getRight()!=null)
						definitions.addProperty(TAG_INDEX, indexedNode.getRight().toString());
				}


		return definitions;
	}



}
