package jano;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jano.builder.OntologyHandler;



@DisplayName("OntologyHandler tests")
public class TestOntologyHandler {

	private OntologyHandler handler;

	@BeforeEach
    void init() {
		 handler = new OntologyHandler();
    }

	@Test
    @DisplayName("correct saref:Device datatype properties extraction (no traversing)")
	public void test01() {
		handler.loadOntology("https://saref.etsi.org/core/v3.1.1/");
		OntClass device = handler.getOwlClass("https://saref.etsi.org/core/Profile");
		Map<Resource, String> dp = handler.getDatatypeProperties(device, true);
		System.out.println(dp);
		assertEquals(2,2);
	}

	@Test
    @DisplayName("correct saref:Device datatype properties extraction (no traversing)")
	public void test02() {
		handler.loadOntology("https://saref.etsi.org/core/v3.1.1/");
		handler.listDatatypeProperties(true).entrySet().parallelStream().forEach(elem -> System.out.println(elem));
		assertEquals(2,2);
	}

	@Test
    @DisplayName("correct saref:Device datatype properties extraction (no traversing)")
	public void test03() {
		handler.loadOntology("http://www.w3.org/2006/time#");
		handler.listDatatypeProperties(true).entrySet().parallelStream().forEach(elem -> System.out.println(elem));
		assertEquals(2,2);
	}
}
