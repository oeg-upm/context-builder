package jano.builder.adapters;

import org.apache.jena.rdf.model.Model;

import jano.builder.exceptions.ReaderException;

public interface Adapter {

	Model adapt(String... args) throws ReaderException;

}