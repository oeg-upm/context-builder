package jano.builder;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import com.github.jsonldjava.shaded.com.google.common.collect.Maps;

public class OntologyHandler {

	private OntModel model;
	private String base;

	public OntologyHandler() {
		super();
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF);
	}


	private void extractBase() {
		Map<String, String> prefixes = model.getNsPrefixMap();
		for (Map.Entry<String, String> entry : prefixes.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}

		if(prefixes.containsKey("")) {
			base = prefixes.get("");
		}
		if(base==null)
			model.listSubjectsWithProperty(RDF.type, OWL.Ontology).forEach(elem -> base= elem.toString());
		if(base==null)
			throw new IllegalArgumentException("Provided ontology has not a @base prefix declared or a URI rdf:type owl:Ontology statement");
	}

	public void readOntology(String ttl) {
		this.model.read(new ByteArrayInputStream(ttl.getBytes()), null, "TURTLE");
		this.model.loadImports();
		extractBase();
	}


	public void loadOntology(String url) {
		this.model.read(url);
		this.model.loadImports();
		extractBase();
	}

	public void loadOntology(String content, String format) {
		this.model.read(new ByteArrayInputStream(content.getBytes()), null, format);
		this.model.loadImports();
		extractBase();
	}

	public OntModel getOntologyModel() {
		return model;
	}

	/**
	 * Finds all the classes connected to the one provided
	 * @param model
	 * @param owlClass
	 * @param traveseParents if true includes in the search all the classes connected to the parents of the provided one
	 * @return
	 */
	public Map<Resource, Resource> getNeighbours(OntClass owlClass, boolean traveseParents) {
		if(!model.containsResource(owlClass))
			throw new IllegalArgumentException("Provided class is not contained in the model");

		List<OntClass> owlClasses = Lists.newArrayList(owlClass);
		if(traveseParents)
			owlClass.listSuperClasses().filterDrop(elem -> elem.isAnon()).forEach(elem -> owlClasses.add(elem));

		return owlClasses.parallelStream().map(owlC ->neighbourhood(owlC)).reduce((firstMap, secondMap) -> {
            firstMap.putAll(secondMap);
            return firstMap;
         }).orElse(null);

	}

	private static final String NEIGHBOURHOOD_QUERY = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n SELECT ?relationship ?rangeClass { ?relationship rdfs:domain <%s> . ?relationship rdfs:range ?rangeClass . FILTER (!strstarts(str(?rangeClass), \"http://www.w3.org/2001/XMLSchema#\") )}";
	private static final String NEIGHBOURHOOD_QUERY_REPLACEMENT ="%s";
	private static final String NEIGHBOURHOOD_QUERY_VAR1 ="?relationship";
	private static final String NEIGHBOURHOOD_QUERY_VAR2 ="?rangeClass";

	private Map<Resource, Resource> neighbourhood(OntClass owlClass){
		Map<Resource, Resource>  neighbour = new HashMap<>();

		String queryString  = NEIGHBOURHOOD_QUERY.replaceFirst(NEIGHBOURHOOD_QUERY_REPLACEMENT, owlClass.toString());
		Query query = QueryFactory.create(queryString) ;
		  try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
		    ResultSet results = qexec.execSelect() ;
		    results.forEachRemaining(elem -> neighbour.put(elem.get(NEIGHBOURHOOD_QUERY_VAR1).asResource(), elem.get(NEIGHBOURHOOD_QUERY_VAR2).asResource()));
		  }catch(Exception e) {
			  System.out.println("here");
			  e.printStackTrace();
		  }
		return neighbour;
	}


	public List<OntClass> getOwlClasses(){
		return model.listClasses().filterDrop(elem -> elem.isAnon()).toList();
	}



	public String getBase() {
		return base;
	}



	public OntClass getOwlClass(String owlClass) {
		return model.getOntClass(owlClass);
	}


	public Map<Resource, Map<Resource, String>> listDatatypeProperties(boolean traverseParents){
		Map<Resource, Map<Resource, String>> dpListing = Maps.newHashMap();
		getOwlClasses().parallelStream()
			.forEach(ontClass -> dpListing.put(ontClass,getDatatypeProperties(ontClass,traverseParents)));
		return dpListing;

	}

	public Map<Resource, String> getDatatypeProperties(OntClass owlClass, boolean traverseParents){
		List<OntClass> owlClasses = Lists.newArrayList(owlClass);
		if(traverseParents && owlClass!=null)
			owlClass.listSuperClasses().filterDrop(elem -> elem.isAnon()).forEach(elem -> owlClasses.add(elem));
		Map<Resource, String> dps= owlClasses.parallelStream().map(owlC ->getDatatypeProperties(owlC, DATATYPE_PROPERTIES_QUERY_1)).reduce((firstMap, secondMap) -> {
            firstMap.putAll(secondMap);
            return firstMap;
         }).orElse(new HashMap<>());
 		Map<Resource, String> dps2= owlClasses.parallelStream().map(owlC ->getDatatypeProperties(owlC, DATATYPE_PROPERTIES_QUERY_2)).reduce((firstMap, secondMap) -> {
            firstMap.putAll(secondMap);
            return firstMap;
         }).orElse(new HashMap<>());
		dps.putAll(dps2);

		for (Map.Entry<Resource, String> entry : dps.entrySet()) {
			System.out.println(entry.getKey().toString() + " -----> " + entry.getValue());
		}

		return dps;
	}

	private static final String DATATYPE_PROPERTIES_QUERY_2 = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n prefix owl: <http://www.w3.org/2002/07/owl#> \n \n SELECT ?relationship ?type {  ?relationship a owl:DatatypeProperty . ?relationship rdfs:domain <%s> . OPTIONAL { ?relationship rdfs:range ?type . }}";

	private static final String DATATYPE_PROPERTIES_QUERY_1 = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n prefix owl: <http://www.w3.org/2002/07/owl#> \n \n SELECT ?relationship ?type { <%s> rdfs: ?b .\n  ?b a owl:Restriction .\n ?b owl:onProperty ?relationship .\n OPTIONAL { ?b owl:allValuesFrom ?type .} OPTIONAL { ?b owl:someValuesFrom ?type .}  \n ?relationship a owl:DatatypeProperty . }";
	private static final String DATATYPE_PROPERTIES_QUERY_REPLACEMENT ="%s";
	private static final String DATATYPE_PROPERTIES_QUERY_VAR1 ="?relationship";
	private static final String DATATYPE_PROPERTIES_QUERY_VAR2 ="?type";
	private Map<Resource,String> getDatatypeProperties(OntClass owlClass, String queryStr){
		Map<Resource, String> dp = new HashMap<>();
		String queryString  = queryStr.replaceFirst(DATATYPE_PROPERTIES_QUERY_REPLACEMENT, owlClass.toString());
		Query query = QueryFactory.create(queryString) ;
		  try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
		    ResultSet results = qexec.execSelect() ;
		    results.forEachRemaining(elem -> {
		    	RDFNode type = elem.get(DATATYPE_PROPERTIES_QUERY_VAR2);
		    	if(type==null) {
		    		dp.put(elem.get(DATATYPE_PROPERTIES_QUERY_VAR1).asResource(), null);
		    	}else {
			    	dp.put(elem.get(DATATYPE_PROPERTIES_QUERY_VAR1).asResource(), type.toString());
		    	}
		    	});
		  }catch(Exception e) {
			  e.printStackTrace();
		  }
		  return dp;
	}




}
