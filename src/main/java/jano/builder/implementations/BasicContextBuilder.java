package jano.builder.implementations;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import jano.builder.BuilderConfiguration;
import jano.builder.OntologyHandler;

class BasicContextBuilder implements ContextBuilder {

	protected static final String TAG_BASE = "@base";
	protected static final String TAG_CONTEXT = "@context";
	protected static final String TAG_ID = "@id";
	protected static final String TAG_TYPE = "@type";
	protected BuilderConfiguration configuration;
	protected static final Logger logger = LoggerFactory.getLogger(ContextBuilder.class);

	public BasicContextBuilder() {
		super();
		configuration = new BuilderConfiguration();
	}

	@Override
	public void setContextBuilderConfiguration(BuilderConfiguration configuration) {
		if (configuration != null)
			this.configuration = configuration;
	}

	@Override
	public BuilderConfiguration getContextBuilderConfiguration() {
		return this.configuration;
	}

	@Override
	public JsonObject buildContext(OntologyHandler handler, String root, Model graph) {
		JsonObject context = new JsonObject();
		JsonObject partialContext = new JsonObject();

		List<JsonObject> partialContextsJson = Lists.newArrayList();
		// 1. Genera context para el root
		OntClass rootOwlClass = handler.getOwlClass(root);
		List<JsonObject> dpsContext = toDatatypePartialContext(handler, rootOwlClass);
		// 2. para cada nodo rango del graph llamada recursiva
		List<JsonObject> opsContext = toObjectPartialContext(handler, rootOwlClass, graph);
		// 3. Añade el resultado definiendo el xontex de un OP
		partialContextsJson.addAll(dpsContext);
		partialContextsJson.addAll(opsContext);
		partialContextsJson.parallelStream().flatMap(elem -> elem.entrySet().stream())
				.forEach(entry -> partialContext.add(entry.getKey(), entry.getValue()));
		if (configuration.getBase() != null)
			partialContext.addProperty(TAG_BASE, configuration.getBase());

		context.add(TAG_CONTEXT, partialContext);
		return context;
	}

	protected List<JsonObject> toObjectPartialContext(OntologyHandler handler, OntClass node, Model graph) {
		List<Statement> statements = graph.listStatements(node, null, (RDFNode) null).toList();
		statements.forEach(st -> graph.remove(st));
		return statements.parallelStream().map(st -> maObjectPropertyToContext(handler, st.getPredicate().asResource(),
				st.getObject().asResource(), graph)).collect(Collectors.toList());
	}

	protected JsonObject maObjectPropertyToContext(OntologyHandler handler, Resource objectProperty,
			Resource targetOwlClass, Model graph) {
		JsonObject definitions = new JsonObject();
		definitions.addProperty(TAG_ID, objectProperty.toString());
		definitions.addProperty(TAG_TYPE, TAG_ID);
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

	protected List<JsonObject> toDatatypePartialContext(OntologyHandler handler, OntClass node) {
		return handler.getDatatypeProperties(node, configuration.isTraverseParents()).entrySet().parallelStream().map(
				entry -> mapDatatypePropertyToContext(handler.getOntologyModel(), entry.getKey(), entry.getValue()))
				.toList();
	}

	protected JsonObject mapDatatypePropertyToContext(OntModel model, Resource dataProperty, String datatype) {
		JsonObject definitions = new JsonObject();
		definitions.addProperty(TAG_ID, dataProperty.toString());
		if (datatype == null)
			datatype = XSD.xstring.getURI();
		definitions.addProperty(TAG_TYPE, datatype);

		String label = extractLabel(model, dataProperty);
		JsonObject partialContext = new JsonObject();
		partialContext.add(label, definitions);
		return partialContext;
	}

	protected String extractLabel(OntModel model, Resource dataProperty) {
		String label = dataProperty.getLocalName();
		Optional<String> labelOpt = null;
		if (configuration.getPrefLabel() != null) {
			labelOpt = model.listObjectsOfProperty(dataProperty, configuration.getPrefLabel()).toList().parallelStream()
					.map(e -> e.asLiteral().getValue().toString()).findFirst();
		}
		if (labelOpt != null && labelOpt.isPresent()) {
			label = labelOpt.get();
		} else if (labelOpt != null && !labelOpt.isPresent()) {
			logger.warn("Missing the preferred label '" + configuration.getPrefLabel()
					+ "' in the datatype property definition of '" + dataProperty.toString()
					+ "' using instead the datatype property local name");
		} else {
			logger.warn(
					"Preferred is null, using datatype property '" + dataProperty.toString() + "' local name instead");
		}
		return label;
	}

}
