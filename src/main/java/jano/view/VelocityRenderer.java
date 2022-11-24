package jano.view;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

public class VelocityRenderer {

	private VelocityTemplateEngine engine;
	private ObjectMapper mapper;

	public VelocityRenderer() {
		engine = new VelocityTemplateEngine();
		mapper = new ObjectMapper();
	}

	public String render(Map<String, Object> model, String template) {
			return engine.render(new ModelAndView(model, template));
	}



	public Map<String, Object> toModel(Object object) {
		@SuppressWarnings("unchecked")
		Map<String, Object> model = mapper.convertValue(object, Map.class);
		return model;
	}


	public String render(Object object, String template) {
		return render(toModel(object), template);
	}

}
