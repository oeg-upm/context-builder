package jano.builder.implementations;

import jano.builder.BuilderConfiguration;

public class CBFactory {

	private CBFactory() {
		super();
	}

	public static ContextBuilder build(BuilderConfiguration configuration) {
		ContextBuilder cb =  new BasicContextBuilder();
		if(configuration!=null && !configuration.getIndexedNodes().isEmpty())
			cb = new AdvancedContextBuilder();
		cb.setContextBuilderConfiguration(configuration);

		return cb;
	}
}
