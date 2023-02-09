package jano.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OntologyDao {
    private static final List<Ontology> DATA = new ArrayList<>();

    public static void add(Ontology ontology) {
        DATA.add(ontology);
    }

    public static Ontology find(String id) {
        return DATA.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    public static void update(String id, String title) {
        find(id).setTitle(title);
    }

    public static void remove(String id) {
        DATA.remove(find(id));
    }

    public static List<Ontology> all() {
        return DATA;
    }
}
