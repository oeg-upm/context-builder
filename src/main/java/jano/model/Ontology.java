package jano.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
@Data
@AllArgsConstructor
public class Ontology {
    String title;
    String id;

    public static Ontology create(String title) {
        return new Ontology(title, UUID.randomUUID().toString());
    }
}
