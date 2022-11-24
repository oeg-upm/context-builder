package jano.main;

import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.staticFileLocation;

import java.util.HashMap;
import java.util.Map;

import jano.controller.ControllerOntology;
import jano.view.VelocityRenderer;
import spark.Spark;


public class App {

	public static void main(String[] args) {
		VelocityRenderer render = new VelocityRenderer();
		Map<String, Object> map  =new HashMap<>();
		staticFileLocation("/public");
		System.out.println(Spark.staticFiles);
		get("/", (req, res) -> {return render.render(map, "index.html");});
		get("", (req, res) -> {return render.render(map, "index.html");});
		get("/test", ControllerOntology.test);

		path("/api", () -> {
	        	// translation tasks CRUD
				get("/classes", ControllerOntology.listOwlClasses);
		        get("/ontology", ControllerOntology.loadOwl);
		        get("/topology", ControllerOntology.topology);
		 });
	}
}
