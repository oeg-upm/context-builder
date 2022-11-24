package jano.builder.implementations;

import org.apache.jena.rdf.model.Model;

import com.google.gson.JsonObject;

import jano.builder.BuilderConfiguration;
import jano.builder.OntologyHandler;

public interface ContextBuilder {

	JsonObject buildContext(OntologyHandler handler, String root, Model graph);
	BuilderConfiguration getContextBuilderConfiguration();
	void setContextBuilderConfiguration(BuilderConfiguration configuration);

}