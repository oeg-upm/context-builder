package jano.builder;

import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.XSD;

public class BuilderConfiguration {

	private Property prefLabel;
	private boolean traverseParents;
	private String base;
	private String root;
	private boolean useDefaultXSD;
	private String defaultXSD;
	private List<Triple<Property, Resource, Property>> indexedNodes;

	public BuilderConfiguration() {
		super();
		setDefault();
	}

	public void setDefault() {
		traverseParents = true;
		defaultXSD = XSD.xstring.getURI();
		useDefaultXSD = true;
		indexedNodes = Lists.newArrayList();
	}

	public Property getPrefLabel() {
		return prefLabel;
	}
	public void setPrefLabel(Property prefLabel) {
		this.prefLabel = prefLabel;
	}
	public boolean isTraverseParents() {
		return traverseParents;
	}
	public void setTraverseParents(boolean traverseParents) {
		this.traverseParents = traverseParents;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public boolean isUseDefaultXSD() {
		return useDefaultXSD;
	}

	public void setUseDefaultXSD(boolean useDefaultXSD) {
		this.useDefaultXSD = useDefaultXSD;
	}

	public String getDefaultXSD() {
		return defaultXSD;
	}

	public void setDefaultXSD(String defaultXSD) {
		this.defaultXSD = defaultXSD;
	}

	public List<Triple<Property, Resource, Property>> getIndexedNodes() {
		return indexedNodes;
	}

	public void setIndexedNodes(List<Triple<Property, Resource, Property>> indexedNodes) {
		this.indexedNodes = indexedNodes;
	}






}
