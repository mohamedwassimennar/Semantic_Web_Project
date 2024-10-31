package tn.sem.websem;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.*;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

@RestController
public class RestApi {

    private static final String NS = "http://rescuefood.org/ontology#";
    Model model = JenaEngine.readModel("data/rescuefood.owl");


    private String resultSetToJson(ResultSet results) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{ \"results\": [");

        boolean first = true;
        while (results.hasNext()) {
            if (!first) {
                jsonBuilder.append(", ");
            }
            QuerySolution solution = results.nextSolution();
            jsonBuilder.append("{");

            // Loop through each variable in the result
            for (String var : results.getResultVars()) {
                RDFNode node = solution.get(var);
                jsonBuilder.append("\"").append(var).append("\": ");

                // Check if the node is null, handle it accordingly
                if (node != null) {
                    jsonBuilder.append("\"").append(node.toString()).append("\"");
                } else {
                    jsonBuilder.append("\"\""); // Default to empty string if node is null
                }
                jsonBuilder.append(", ");
            }
            // Remove the last comma and space
            jsonBuilder.setLength(jsonBuilder.length() - 2);
            jsonBuilder.append("}");
            first = false;
        }

        jsonBuilder.append("]}");
        return jsonBuilder.toString();
    }


    // Inventory CRUD Operations
    @GetMapping("/inventory")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> getInventories() {
        if (model != null) {
            String sparqlQuery = """
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX j.0: <http://rescuefood.org/ontology#>
                
                SELECT ?inventory ?currentQuantity ?food ?foodType ?quantity ?expiryDate
                WHERE {
                    ?inventory rdf:type j.0:Inventory .
                    ?inventory j.0:currentQuantity ?currentQuantity .
                    OPTIONAL {
                        ?inventory j.0:stores ?food .
                        ?food j.0:foodType ?foodType .
                        ?food j.0:quantity ?quantity .
                        ?food j.0:expiryDate ?expiryDate .
                    }
                }
                """;

            Query query = QueryFactory.create(sparqlQuery);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
                ResultSet results = qexec.execSelect();
                String jsonResults = resultSetToJson(results);
                return new ResponseEntity<>(jsonResults, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error executing SPARQL query: " + e.getMessage(), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Error when reading model from ontology", 
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/inventory")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addInventory(@RequestBody Map<String, Object> payload) {
        if (model != null) {
            try {
                String inventoryId = "Inventory" + UUID.randomUUID();
                String uniqueFoodId = (String) payload.get("foodId");
                String fullFoodId = "http://rescuefood.org/ontology#Food" + uniqueFoodId;

                String updateQuery = """
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX j.0: <http://rescuefood.org/ontology#>
                
                INSERT DATA {
                    j.0:%s rdf:type j.0:Inventory ;
                        j.0:currentQuantity "%s" .
                    j.0:%s j.0:stores <%s> .
                }
                """.formatted(
                        inventoryId,
                        payload.get("currentQuantity"),
                        inventoryId,
                        fullFoodId
                );

                UpdateRequest updateRequest = UpdateFactory.create(updateQuery);
                Dataset dataset = DatasetFactory.create(model);
                UpdateProcessor processor = UpdateExecutionFactory.create(updateRequest, dataset);
                processor.execute();

                return new ResponseEntity<>("Inventory added successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding inventory: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Error when reading model from ontology",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/inventory/{id}")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> updateInventory(@PathVariable String id, @RequestBody InventoryDto inventoryDto) {
        if (model != null) {
            try {
                String sparqlUpdate = String.format(
                    "PREFIX j: <http://rescuefood.org/ontology#> " +
                    "DELETE { " +
                    "   j:%s j:currentQuantity ?oldQuantity " +
                    "} " +
                    "INSERT { " +
                    "   j:%s j:currentQuantity \"%f\" " +
                    "} " +
                    "WHERE { " +
                    "   j:%s j:currentQuantity ?oldQuantity " +
                    "}",
                    id, id, inventoryDto.getCurrentQuantity(), id
                );

                JenaEngine.executeUpdate(sparqlUpdate, model);
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Inventory updated successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error updating inventory: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/inventory/{id}")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> deleteInventory(@PathVariable String id) {
        if (model != null) {
            String updateQuery = """
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX j: <http://rescuefood.org/ontology#>
            
            DELETE {
                j:%s ?p ?o .
            }
            WHERE {
                j:%s ?p ?o .
            }
            """.formatted(id, id);

            try {
                UpdateRequest updateRequest = UpdateFactory.create(updateQuery);
                Dataset dataset = DatasetFactory.create(model);
                UpdateProcessor processor = UpdateExecutionFactory.create(updateRequest, dataset);
                processor.execute();

                return new ResponseEntity<>("Inventory deleted successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting inventory: " + e.getMessage(), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Error when reading model from ontology", 
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Manager CRUD Operations
    @GetMapping("/manager")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> getManagers() {
        if (model != null) {
            try {
                String sparqlQuery = new String(Files.readAllBytes(Paths.get("data/query_Manager.txt")), StandardCharsets.UTF_8);
                Query query = QueryFactory.create(sparqlQuery);

                try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
                    ResultSet results = qexec.execSelect();
                    String jsonResults = resultSetToJson(results);
                    return new ResponseEntity<>(jsonResults, HttpStatus.OK);
                }
            } catch (Exception e) {
                return new ResponseEntity<>("Error executing SPARQL query: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/manager")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> createManager(@RequestBody ManagerDto managerDto) {
        if (model != null) {
            try {
                String managerUri = "http://rescuefood.org/ontology#Manager" + UUID.randomUUID().toString();
                
                String sparqlInsert = String.format(
                    "PREFIX j: <http://rescuefood.org/ontology#> " +
                    "INSERT DATA { " +
                    "   <%s> a j:Manager ; " +
                    "       j:name \"%s\" ; " +
                    "       j:contact \"%s\" . " +
                    "}", 
                    managerUri,
                    managerDto.getName(),
                    managerDto.getContact()
                );

                JenaEngine.executeUpdate(sparqlInsert, model);
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Manager created successfully", HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error creating manager: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/manager/{id}")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> updateManager(@PathVariable String id, @RequestBody ManagerDto managerDto) {
        if (model != null) {
            try {
                String sparqlUpdate = String.format(
                    "PREFIX j: <http://rescuefood.org/ontology#> " +
                    "DELETE { " +
                    "   j:%s j:name ?oldName ; " +
                    "        j:contact ?oldContact . " +
                    "} " +
                    "INSERT { " +
                    "   j:%s j:name \"%s\" ; " +
                    "        j:contact \"%s\" . " +
                    "} " +
                    "WHERE { " +
                    "   j:%s j:name ?oldName ; " +
                    "        j:contact ?oldContact . " +
                    "}",
                    id, id, managerDto.getName(), managerDto.getContact(), id
                );

                JenaEngine.executeUpdate(sparqlUpdate, model);
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Manager updated successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error updating manager: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/manager/{id}")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> deleteManager(@PathVariable String id) {
        if (model != null) {
            try {
                String sparqlDelete = String.format(
                    "PREFIX j: <http://rescuefood.org/ontology#> " +
                    "DELETE WHERE { " +
                    "   j:%s ?p ?o " +
                    "}",
                    id
                );

                JenaEngine.executeUpdate(sparqlDelete, model);
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Manager deleted successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting manager: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @PostMapping("/addInventory")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addInventory(@RequestBody InventoryDto inventoryDto) {
        if (model != null) {
            try {
                // Generate a random URI for the inventory resource using UUID
                String inventoryResourceUri = "http://rescuefood.org/ontology#Inventory" + UUID.randomUUID().toString();

                // Create the resource with the generated URI
                Resource inventoryResource = model.createResource(inventoryResourceUri);

                // Define the properties for the Inventory class
                Property currentQuantityProperty = model.createProperty("http://rescuefood.org/ontology#currentQuantity");

                // Add RDF type for the resource (define as an Inventory type)
                model.add(inventoryResource, RDF.type, model.createResource("http://rescuefood.org/ontology#Inventory"));

                // Add the currentQuantity property
                model.addLiteral(inventoryResource, currentQuantityProperty, inventoryDto.getCurrentQuantity());

                // Save the model
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Inventory added successfully: " + inventoryResourceUri, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding Inventory: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updateInventory")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> updateInventory(@RequestBody InventoryDto inventoryDto) {
        if (model != null) {
            try {
                // Récupérer la ressource inventaire en utilisant son URI
                Resource inventoryResource = model.getResource(inventoryDto.getInventory());

                if (inventoryResource != null && inventoryResource.hasProperty(RDF.type, model.createResource("http://rescuefood.org/ontology#Inventory"))) {
                    // Supprimer l'ancienne valeur de currentQuantity
                    Property currentQuantityProperty = model.createProperty("http://rescuefood.org/ontology#currentQuantity");
                    model.removeAll(inventoryResource, currentQuantityProperty, null);

                    // Ajouter la nouvelle valeur de currentQuantity
                    model.addLiteral(inventoryResource, currentQuantityProperty, inventoryDto.getCurrentQuantity());

                    // Sauvegarder le modèle
                    JenaEngine.saveModel(model, "data/rescuefood.owl");

                    return new ResponseEntity<>("Inventory updated successfully: " + inventoryDto.getInventory(), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Inventory not found", HttpStatus.NOT_FOUND);
                }
            } catch (Exception e) {
                return new ResponseEntity<>("Error updating Inventory: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/addManager")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addManager(@RequestBody ManagerDto managerDto) {
        if (model != null) {
            try {
                // Générer une URI unique pour le manager
                String managerUri = "http://rescuefood.org/ontology#Manager" + UUID.randomUUID().toString();

                // Créer la ressource Manager avec l'URI générée
                Resource managerResource = model.createResource(managerUri);

                // Définir les propriétés pour la ressource Manager
                Property contactProperty = model.createProperty("http://rescuefood.org/ontology#contact");
                Property nameProperty = model.createProperty("http://rescuefood.org/ontology#name");

                // Ajouter le type RDF pour la ressource
                model.add(managerResource, RDF.type, model.createResource("http://rescuefood.org/ontology#Manager"));

                // Ajouter les propriétés à la ressource
                model.add(managerResource, contactProperty, managerDto.getContact());
                model.add(managerResource, nameProperty, managerDto.getName());

                // Sauvegarder le modèle
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Manager added successfully: " + managerUri, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding Manager: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint pour mettre à jour un manager existant
    @PutMapping("/updateManager")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> updateManager(@RequestBody ManagerDto managerDto) {
        if (model != null) {
            try {
                // Récupérer la ressource Manager en utilisant l'URI
                Resource managerResource = model.getResource(managerDto.getManager()); // assuming URI based on contact info

                if (managerResource != null && managerResource.hasProperty(RDF.type, model.createResource("http://rescuefood.org/ontology#Manager"))) {
                    // Supprimer les anciennes valeurs
                    Property contactProperty = model.createProperty("http://rescuefood.org/ontology#contact");
                    Property nameProperty = model.createProperty("http://rescuefood.org/ontology#name");
                    model.removeAll(managerResource, contactProperty, null);
                    model.removeAll(managerResource, nameProperty, null);

                    // Ajouter les nouvelles valeurs
                    model.add(managerResource, contactProperty, managerDto.getContact());
                    model.add(managerResource, nameProperty, managerDto.getName());

                    // Sauvegarder le modèle
                    JenaEngine.saveModel(model, "data/rescuefood.owl");

                    return new ResponseEntity<>("Manager updated successfully: " + managerDto.getContact(), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Manager not found", HttpStatus.NOT_FOUND);
                }
            } catch (Exception e) {
                return new ResponseEntity<>("Error updating Manager: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint pour supprimer un manager

    @GetMapping("/restaurant")
    @CrossOrigin(origins = "http://localhost:4200")
    public String afficherRestaurant() {
        String NS = "";
        if (model != null) {
            NS = model.getNsPrefixURI("");

            Model inferedModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            OutputStream res = JenaEngine.executeQueryFile(inferedModel, "data/query_Restaurant.txt");

            System.out.println(res);
            return res.toString();

        } else {
            return ("Error when reading model from ontology");
        }
    }
    @PostMapping("/addRestaurant2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addRestaurant2(@RequestBody RestaurantDto restaurantDto) {
        if (model != null) {
            try {
                // Define the SPARQL INSERT query
                String insertQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "INSERT { " +
                                "  ?restaurant rdf:type rescue:Restaurant . " +
                                "  ?restaurant rescue:name \"" + restaurantDto.getName() + "\" . " +
                                "  ?restaurant rescue:contact \"" + restaurantDto.getContact() + "\" . " +
                                "  ?restaurant rescue:address \"" + restaurantDto.getAddress() + "\" . " +
                                "} WHERE { " +
                                "  BIND(IRI(CONCAT(\"http://rescuefood.org/ontology/Restaurant_\", STRUUID())) AS ?restaurant) " +
                                "}";

                // Create the update request and execute it
                UpdateRequest updateRequest = UpdateFactory.create(insertQuery);
                UpdateAction.execute(updateRequest, model);

                // Save the updated model to the ontology file
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Restaurant added successfully", HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding restaurant: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/modifyRestaurant2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> modifyRestaurant2(@RequestBody RestaurantDto restaurantDto) {
        if (model != null) {
            try {
                // Ensure the restaurant resource exists
                Resource restaurantResource = model.getResource(restaurantDto.getRestaurant());
                if (restaurantResource == null) {
                    return new ResponseEntity<>("Restaurant not found", HttpStatus.NOT_FOUND);
                }

                // Define the SPARQL DELETE/INSERT query
                String modifyQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "DELETE { " +
                                "  ?restaurant rescue:name ?oldName . " +
                                "  ?restaurant rescue:contact ?oldContact . " +
                                "  ?restaurant rescue:address ?oldAddress . " +
                                "} " +
                                "INSERT { " +
                                "  ?restaurant rescue:name \"" + restaurantDto.getName() + "\" . " +
                                "  ?restaurant rescue:contact \"" + restaurantDto.getContact() + "\" . " +
                                "  ?restaurant rescue:address \"" + restaurantDto.getAddress() + "\" . " +
                                "} " +
                                "WHERE { " +
                                "  BIND(<" + restaurantDto.getRestaurant() + "> AS ?restaurant) ." +
                                "  OPTIONAL { ?restaurant rescue:name ?oldName } ." +
                                "  OPTIONAL { ?restaurant rescue:contact ?oldContact } ." +
                                "  OPTIONAL { ?restaurant rescue:address ?oldAddress } ." +
                                "}";

                // Create and execute the update request
                UpdateRequest updateRequest = UpdateFactory.create(modifyQuery);
                UpdateAction.execute(updateRequest, model);

                // Save the updated model to the ontology file
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Restaurant modified successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error modifying restaurant: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/deleteRestaurant2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> deleteRestaurant2(@RequestBody RestaurantDto restaurantDto) {
        String restaurantUri = restaurantDto.getRestaurant();

        System.out.println("Received request to delete restaurant: " + restaurantUri);

        if (model != null) {
            try {
                // Define the SPARQL DELETE query
                String deleteQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "DELETE WHERE { " +
                                "  <" + restaurantUri + "> ?p ?o ." +
                                "}";

                // Create and execute the update request
                UpdateRequest updateRequest = UpdateFactory.create(deleteQuery);
                UpdateAction.execute(updateRequest, model);

                // Save the updated model to the ontology file
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Restaurant deleted successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting restaurant: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/restaurants/sorted")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> getSortedRestaurants() {
        if (model != null) {
            try {
                // Define the SPARQL query to retrieve sorted restaurants by name
                String queryString =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "SELECT ?restaurant ?name ?contact ?address " +
                                "WHERE { " +
                                "  ?restaurant rdf:type rescue:Restaurant . " +
                                "  ?restaurant rescue:name ?name . " +
                                "} " +
                                "ORDER BY ?name"; // Sort by restaurant name

                // Execute the query on the inferred model
                OutputStream res = JenaEngine.executeQuery(model, queryString);

                // Convert the OutputStream to String for the response
                return new ResponseEntity<>(res.toString(), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error retrieving sorted restaurants: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/addRestaurant")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addRestaurant(@RequestBody RestaurantDto restaurantDto) {
        if (model != null) {
            try {
                // Generate a random URL for the restaurant resource using UUID
                String restaurantResourceUri = "http://rescuefood.org/ontology#Restaurant" + UUID.randomUUID().toString();

                // Create the resource with the generated URL
                Resource restaurantResource = model.createResource(restaurantResourceUri);

                Property nameProperty = model.createProperty("http://rescuefood.org/ontology#name");
                Property contactProperty = model.createProperty("http://rescuefood.org/ontology#contact");
                Property addressProperty = model.createProperty("http://rescuefood.org/ontology#address");

                // Add RDF type for the resource
                model.add(restaurantResource, RDF.type, model.createResource("http://rescuefood.org/ontology#Restaurant"));

                // Add properties to the resource
                model.add(restaurantResource, nameProperty, restaurantDto.getName());
                model.add(restaurantResource, contactProperty, restaurantDto.getContact());
                model.add(restaurantResource, addressProperty, restaurantDto.getAddress());

                // Save the model
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Restaurant added successfully: " + restaurantResourceUri, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding restaurant: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/modifyRestaurant")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> modifyRestaurant(@RequestBody RestaurantDto restaurantDto) {
        if (model != null) {
            try {
                Resource restaurantResource = model.getResource(restaurantDto.getRestaurant());
                if (restaurantResource == null) {
                    return new ResponseEntity<>("Restaurant not found", HttpStatus.NOT_FOUND);
                }

                Property nameProperty = model.createProperty("http://rescuefood.org/ontology#name");
                Property contactProperty = model.createProperty("http://rescuefood.org/ontology#contact");
                Property addressProperty = model.createProperty("http://rescuefood.org/ontology#address");

                model.removeAll(restaurantResource, nameProperty, null);
                model.removeAll(restaurantResource, contactProperty, null);
                model.removeAll(restaurantResource, addressProperty, null);

                model.add(restaurantResource, nameProperty, restaurantDto.getName());
                model.add(restaurantResource, contactProperty, restaurantDto.getContact());
                model.add(restaurantResource, addressProperty, restaurantDto.getAddress());

                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Restaurant modified successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error modifying restaurant: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/deleteRestaurant")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> deleteRestaurant(@RequestBody RestaurantDto restaurantDto) {
        String restaurantUri = restaurantDto.getRestaurant();

        System.out.println("Received request to delete restaurant: " + restaurantUri);

        if (model != null) {
            try {
                Resource restaurantResource = model.getResource(restaurantUri);
                if (restaurantResource == null) {
                    return new ResponseEntity<>("Restaurant not found", HttpStatus.NOT_FOUND);
                }

                model.removeAll(restaurantResource, null, null);

                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Restaurant deleted successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting restaurant: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }







    @GetMapping("/food")
    @CrossOrigin(origins = "http://localhost:4200")
    public String afficherFood() {
        String NS = "";
        if (model != null) {
            NS = model.getNsPrefixURI("");

            Model inferedModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            OutputStream res = JenaEngine.executeQueryFile(inferedModel, "data/query_Food.txt");

            System.out.println(res);
            return res.toString();

        } else {
            return ("Error when reading model from ontology");
        }
    }
    @PostMapping("/addFood2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addFood3(@RequestBody FoodDto foodDto) {
        if (model != null) {
            try {
                // Construct SPARQL INSERT query
                String insertQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "INSERT { " +
                                "  ?food rdf:type rescue:Food . " +
                                "  ?food rescue:foodType \"" + foodDto.getFoodType() + "\" . " +
                                "  ?food rescue:quantity \"" + foodDto.getQuantity() + "\" . " +
                                "  ?food rescue:expiryDate \"" + foodDto.getExpiryDate() + "\" . " +
                                "} " +
                                "WHERE { " +
                                "  BIND(IRI(CONCAT(\"http://rescuefood.org/resource/Food/\", STRUUID())) AS ?food) " +
                                "}";

                // Create and execute the update request
                UpdateRequest updateRequest = UpdateFactory.create(insertQuery);
                UpdateAction.execute(updateRequest, model);

                // Save the updated model to the ontology file
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Food added successfully", HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding food: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/updateFood2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> updateFood2(@RequestBody FoodDto foodDto) {
        if (model != null) {
            try {
                // Create the SPARQL update query
                String updateQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "DELETE { " +
                                "  ?food rescue:foodType ?oldFoodType . " +
                                "  ?food rescue:quantity ?oldQuantity . " +
                                "  ?food rescue:expiryDate ?oldExpiryDate . " +
                                "} " +
                                "INSERT { " +
                                "  ?food rescue:foodType \"" + foodDto.getFoodType() + "\" . " +
                                "  ?food rescue:quantity \"" + foodDto.getQuantity() + "\" . " +
                                "  ?food rescue:expiryDate \"" + foodDto.getExpiryDate() + "\" . " +
                                "} " +
                                "WHERE { " +
                                "  ?food rdf:type rescue:Food . " +
                                "  ?food rescue:foodType ?oldFoodType . " +
                                "  FILTER(?food = <" + foodDto.getFood() + ">) " +
                                "}";

                // Create and execute the update request
                UpdateRequest updateRequest = UpdateFactory.create(updateQuery);
                UpdateAction.execute(updateRequest, model);

                // Save the updated model to the ontology file
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Food updated successfully: " + foodDto.getFood(), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error updating food: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/deleteFood2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> deleteFood2(@RequestBody FoodDto foodDto) {
        if (model != null) {
            try {
                // Create the resource URI from the FoodDto
                String foodResourceUri = foodDto.getFood(); // Assuming the URI is passed in the foodDto

                // Create a SPARQL DELETE query
                String deleteQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "DELETE { " +
                                "  ?food ?p ?o . " + // Delete all predicates and objects for the food resource
                                "} " +
                                "WHERE { " +
                                "  BIND(<" + foodResourceUri + "> AS ?food) . " +
                                "  ?food ?p ?o . " + // Match all triples for the specified food resource
                                "}";

                // Create and execute the update request
                UpdateRequest updateRequest = UpdateFactory.create(deleteQuery);
                UpdateAction.execute(updateRequest, model);

                // Save the updated model to the ontology file
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Food deleted successfully: " + foodResourceUri, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting food: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/addFood")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addFood2(@RequestBody FoodDto foodDto) {
        if (model != null) {
            try {
                // Generate a random URL for the food resource using UUID
                String foodResourceUri = "http://rescuefood.org/ontology#Food" + UUID.randomUUID().toString();

                // Create the resource with the generated URL
                Resource restaurantResource = model.createResource(foodResourceUri);

                Property foodTypeProperty = model.createProperty("http://rescuefood.org/ontology#foodType");
                Property quantityProperty = model.createProperty("http://rescuefood.org/ontology#quantity");
                Property expiryDateProperty = model.createProperty("http://rescuefood.org/ontology#expiryDate");

                // Add RDF type for the resource
                model.add(restaurantResource, RDF.type, model.createResource("http://rescuefood.org/ontology#Food"));

                // Add properties to the resource
                model.add(restaurantResource, foodTypeProperty, foodDto.getFoodType());
                model.add(restaurantResource, quantityProperty, foodDto.getQuantity());
                model.add(restaurantResource, expiryDateProperty, foodDto.getExpiryDate());

                // Save the model
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Food added successfully: " + foodResourceUri, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding Food: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/updateFood")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> updateFood(@RequestBody FoodDto foodDto) {
        if (model != null) {
            try {
                // Create the resource URI from the FoodDto
                String foodResourceUri = foodDto.getFood(); // Assuming the URI is passed in the foodDto

                // Check if the resource exists
                Resource restaurantResource = model.getResource(foodResourceUri);
                if (restaurantResource == null) {
                    return new ResponseEntity<>("Food resource not found", HttpStatus.NOT_FOUND);
                }

                Property foodTypeProperty = model.createProperty("http://rescuefood.org/ontology#foodType");
                Property quantityProperty = model.createProperty("http://rescuefood.org/ontology#quantity");
                Property expiryDateProperty = model.createProperty("http://rescuefood.org/ontology#expiryDate");

                // Update properties of the existing resource
                model.removeAll(restaurantResource, foodTypeProperty, null); // Remove existing foodType
                model.add(restaurantResource, foodTypeProperty, foodDto.getFoodType()); // Add updated foodType

                model.removeAll(restaurantResource, quantityProperty, null); // Remove existing quantity
                model.add(restaurantResource, quantityProperty, foodDto.getQuantity()); // Add updated quantity

                model.removeAll(restaurantResource, expiryDateProperty, null); // Remove existing expiryDate
                model.add(restaurantResource, expiryDateProperty, foodDto.getExpiryDate()); // Add updated expiryDate

                // Save the updated model
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Food updated successfully: " + foodResourceUri, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error updating Food: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteFood")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> deleteFood(@RequestBody FoodDto foodDto) {
        if (model != null) {
            try {
                // Create the resource URI from the FoodDto
                String foodResourceUri = foodDto.getFood(); // Assuming the URI is passed in the foodDto

                // Check if the resource exists
                Resource restaurantResource = model.getResource(foodResourceUri);
                if (restaurantResource == null) {
                    return new ResponseEntity<>("Food resource not found", HttpStatus.NOT_FOUND);
                }

                // Remove the resource from the model
                model.removeAll(restaurantResource, null, null); // Remove all triples associated with this resource
                model.remove(restaurantResource, RDF.type, model.createResource("http://rescuefood.org/ontology#Food")); // Specifically remove the RDF type

                // Save the updated model
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Food deleted successfully: " + foodResourceUri, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting Food: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/notification")
    @CrossOrigin(origins = "http://localhost:4200")
    public String afficherNotification() {
        String NS = "";
        if (model != null) {
            NS = model.getNsPrefixURI("");

            Model inferedModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            OutputStream res = JenaEngine.executeQueryFile(inferedModel, "data/query_Notification.txt");

            System.out.println(res);
            return res.toString();

        } else {
            return ("Error when reading model from ontology");
        }
    }


    @PostMapping("/addNotification")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addNotification(@RequestBody NotificationDto notificationDto) {
        if (model != null) {
            try {
                Resource notificationResource = model.createResource(notificationDto.getNotification());

                Property recipientProperty = model.createProperty("http://rescuefood.org/ontology#recipient");
                Property notificationTypeProperty = model.createProperty("http://rescuefood.org/ontology#notificationType");

                model.add(notificationResource, RDF.type, model.createResource("http://rescuefood.org/ontology#Notification"));
                model.add(notificationResource, recipientProperty, notificationDto.getRecipient());
                model.add(notificationResource, notificationTypeProperty, notificationDto.getNotificationType());

                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Notification ajoutée avec succès", HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Erreur lors de l'ajout de la notification : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Erreur lors de la lecture du modèle de l'ontologie", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/modifyNotification")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> modifyNotification(@RequestBody NotificationDto notificationDto) {
        if (model != null) {
            try {
                Resource notificationResource = model.getResource(notificationDto.getNotification());
                if (notificationResource == null) {
                    return new ResponseEntity<>("Notification not found", HttpStatus.NOT_FOUND);
                }

                Property recipientProperty = model.createProperty("http://rescuefood.org/ontology#recipient");
                Property notificationTypeProperty = model.createProperty("http://rescuefood.org/ontology#notificationType");

                model.removeAll(notificationResource, recipientProperty, null);
                model.removeAll(notificationResource, notificationTypeProperty, null);

                model.add(notificationResource, recipientProperty, notificationDto.getRecipient());
                model.add(notificationResource, notificationTypeProperty, notificationDto.getNotificationType());

                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Notification modified successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error modifying notification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/deleteNotification")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> deleteNotification(@RequestBody NotificationDto notificationDto) {
        String notificationUri = notificationDto.getNotification();

        System.out.println("Received request to delete notification: " + notificationUri);

        if (model != null) {
            try {
                Resource notificationResource = model.getResource(notificationUri);
                if (notificationResource == null) {
                    return new ResponseEntity<>("Notification not found", HttpStatus.NOT_FOUND);
                }

                model.removeAll(notificationResource, null, null);

                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Notification deleted successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting notification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/director")
    @CrossOrigin(origins = "http://localhost:4200")
    public String afficherDirector() {
        String NS = "";
        if (model != null) {
            NS = model.getNsPrefixURI("");
            Model inferredModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");
            OutputStream res = JenaEngine.executeQueryFile(inferredModel, "data/query_Director.txt");
            System.out.println(res);
            return res.toString();
        } else {
            return ("Error when reading model from ontology");
        }
    }

    @PostMapping("/addDirector")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addDirector(@RequestBody DirectorDto directorDto) {
        if (model != null) {
            try {
                // Generate a random URL for the director resource using UUID
                String directorResourceUri = "http://rescuefood.org/ontology#Director" + UUID.randomUUID().toString();

                // Create the resource with the generated URL
                Resource directorResource = model.createResource(directorResourceUri);

                // Define properties
                Property contactProperty = model.createProperty("http://rescuefood.org/ontology#contact");
                Property nameProperty = model.createProperty("http://rescuefood.org/ontology#name");

                // Add RDF type for the resource
                model.add(directorResource, RDF.type, model.createResource("http://rescuefood.org/ontology#Director"));

                // Add properties to the resource
                model.add(directorResource, contactProperty, directorDto.getContact());
                model.add(directorResource, nameProperty, directorDto.getName());

                // Save the model
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Director added successfully: " + directorResourceUri, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding Director: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updateDirector")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> updateDirector(@RequestBody DirectorDto directorDto) {
        if (model != null) {
            try {
                // Create the resource URI from the DirectorDto
                String directorResourceUri = directorDto.getDirector(); // Assuming the URI is passed in the directorDto

                // Check if the resource exists
                Resource directorResource = model.getResource(directorResourceUri);
                if (directorResource == null) {
                    return new ResponseEntity<>("Director resource not found", HttpStatus.NOT_FOUND);
                }

                Property contactProperty = model.createProperty("http://rescuefood.org/ontology#contact");
                Property nameProperty = model.createProperty("http://rescuefood.org/ontology#name");

                // Update properties of the existing resource
                model.removeAll(directorResource, contactProperty, null); // Remove existing contact
                model.add(directorResource, contactProperty, directorDto.getContact()); // Add updated contact

                model.removeAll(directorResource, nameProperty, null); // Remove existing name
                model.add(directorResource, nameProperty, directorDto.getName()); // Add updated name

                // Save the updated model
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Director updated successfully: " + directorResourceUri, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error updating Director: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteDirector")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> deleteDirector(@RequestBody DirectorDto directorDto) {
        if (model != null) {
            try {
                // Create the resource URI from the DirectorDto
                String directorResourceUri = directorDto.getDirector(); // Assuming the URI is passed in the directorDto

                // Check if the resource exists
                Resource directorResource = model.getResource(directorResourceUri);
                if (directorResource == null) {
                    return new ResponseEntity<>("Director resource not found", HttpStatus.NOT_FOUND);
                }

                // Remove the resource from the model
                model.removeAll(directorResource, null, null); // Remove all triples associated with this resource
                model.remove(directorResource, RDF.type, model.createResource("http://rescuefood.org/ontology#Director")); // Specifically remove the RDF type

                // Save the updated model
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Director deleted successfully: " + directorResourceUri, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting Director: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @GetMapping("/collectionevent")
    @CrossOrigin(origins = "http://localhost:4200")
    public String afficherCollectionEvent() {
        String NS = "";
        if (model != null) {
            NS = model.getNsPrefixURI("");
            Model inferredModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");
            OutputStream res = JenaEngine.executeQueryFile(inferredModel, "data/query_CollectionEvent.txt");
            System.out.println(res);
            return res.toString();
        } else {
            return ("Error when reading model from ontology");
        }
    }

    @PostMapping("/addCollectionEvent")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addCollectionEvent(@RequestBody CollectionEventDto collectionEventDto) {
        if (model != null) {
            try {
                // Generate a random URI for the CollectionEvent resource
                String eventResourceUri = "http://rescuefood.org/ontology#CollectionEvent" + UUID.randomUUID().toString();

                // Create the resource with the generated URI
                Resource eventResource = model.createResource(eventResourceUri);

                // Define the date property
                Property dateProperty = model.createProperty("http://rescuefood.org/ontology#date");

                // Add RDF type and properties for the resource
                model.add(eventResource, RDF.type, model.createResource("http://rescuefood.org/ontology#CollectionEvent"));
                model.add(eventResource, dateProperty, collectionEventDto.getDate());

                // Save the updated model
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("CollectionEvent added successfully: " + eventResourceUri, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding CollectionEvent: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updateCollectionEvent")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> updateCollectionEvent(@RequestBody CollectionEventDto collectionEventDto) {
        if (model != null) {
            try {
                // Get the resource URI from the CollectionEventDto
                String eventResourceUri = collectionEventDto.getEvent(); // Assuming the URI is passed in the collectionEventDto

                // Check if the resource exists
                Resource eventResource = model.getResource(eventResourceUri);
                if (eventResource == null) {
                    return new ResponseEntity<>("CollectionEvent resource not found", HttpStatus.NOT_FOUND);
                }

                // Define the date property
                Property dateProperty = model.createProperty("http://rescuefood.org/ontology#date");

                // Update the date property
                model.removeAll(eventResource, dateProperty, null); // Remove existing date
                model.add(eventResource, dateProperty, collectionEventDto.getDate()); // Add updated date

                // Save the updated model
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("CollectionEvent updated successfully: " + eventResourceUri, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error updating CollectionEvent: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteCollectionEvent")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> deleteCollectionEvent(@RequestBody CollectionEventDto collectionEventDto) {
        if (model != null) {
            try {
                // Get the resource URI from the CollectionEventDto
                String eventResourceUri = collectionEventDto.getEvent(); // Assuming the URI is passed in the collectionEventDto

                // Check if the resource exists
                Resource eventResource = model.getResource(eventResourceUri);
                if (eventResource == null) {
                    return new ResponseEntity<>("CollectionEvent resource not found", HttpStatus.NOT_FOUND);
                }

                // Remove the resource from the model
                model.removeAll(eventResource, null, null); // Remove all triples associated with this resource
                model.remove(eventResource, RDF.type, model.createResource("http://rescuefood.org/ontology#CollectionEvent")); // Specifically remove the RDF type

                // Save the updated model
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("CollectionEvent deleted successfully: " + eventResourceUri, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting CollectionEvent: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/request")
    @CrossOrigin(origins = "http://localhost:4200")
    public String afficherRequest() {
        String NS = "";
        if (model != null) {
            NS = model.getNsPrefixURI("");
            Model inferredModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");
            OutputStream res = JenaEngine.executeQueryFile(inferredModel, "data/query_Request.txt");
            System.out.println(res);
            return res.toString();
        } else {
            return ("Error when reading model from ontology");
        }

    }
    @GetMapping("/feedback")
    @CrossOrigin(origins = "http://localhost:4200")
    public String afficherFeedback() {
        String NS = "";
        if (model != null) {
            NS = model.getNsPrefixURI("");
            Model inferredModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");
            OutputStream res = JenaEngine.executeQueryFile(inferredModel, "data/query_Feedback.txt");
            System.out.println(res);
            return res.toString();
        } else {
            return ("Error when reading model from ontology");
        }
    }
    @PostMapping("/addFeedback")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addFeedback(@RequestBody FeedbackDto feedbackDto) {
        if (model != null) {
            try {
                // Générer une URL aléatoire pour la ressource de feedback en utilisant UUID
                String feedbackResourceUri = "http://rescuefood.org/ontology#Feedback" + UUID.randomUUID().toString();

                // Créer la ressource avec l'URL générée
                Resource feedbackResource = model.createResource(feedbackResourceUri);

                Property feedbackProperty = model.createProperty("http://rescuefood.org/ontology#feedback");
                Property commentProperty = model.createProperty("http://rescuefood.org/ontology#comment");
                Property ratingProperty = model.createProperty("http://rescuefood.org/ontology#rating");

                // Ajouter le type RDF pour la ressource
                model.add(feedbackResource, RDF.type, model.createResource("http://rescuefood.org/ontology#Feedback"));

                // Ajouter les propriétés à la ressource
                model.add(feedbackResource, feedbackProperty, feedbackResourceUri);
                model.add(feedbackResource, commentProperty, feedbackDto.getComment());
                model.add(feedbackResource, ratingProperty, feedbackDto.getRating().toString());


                // Sauvegarder le modèle
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Feedback ajouté avec succès : " + feedbackResourceUri, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Erreur lors de l'ajout du feedback : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Erreur lors de la lecture du modèle à partir de l'ontologie", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PutMapping("/modifyFeedback")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> modifyFeedback(@RequestBody FeedbackDto feedbackDto) {
        if (model != null) {
            try {
                Resource feedbackResource = model.getResource(feedbackDto.getFeedback());
                if (feedbackResource == null) {
                    return new ResponseEntity<>("Feedback not found", HttpStatus.NOT_FOUND);
                }

                Property ratingProperty = model.createProperty("http://rescuefood.org/ontology#rating");
                Property commentProperty = model.createProperty("http://rescuefood.org/ontology#comment");

                model.removeAll(feedbackResource, ratingProperty, null);
                model.removeAll(feedbackResource, commentProperty, null);

                model.add(feedbackResource, ratingProperty, feedbackDto.getRating().toString());
                model.add(feedbackResource, commentProperty, feedbackDto.getComment());

                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Feedback modified successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error modifying feedback: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/deleteFeedback")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> deleteFeedback(@RequestBody FeedbackDto feedbackDto) {
        // Obtenez l'URI du feedback à partir du DTO
        String feedbackUri = feedbackDto.getFeedback();

        System.out.println("Received request to delete feedback: " + feedbackUri);

        // Vérifiez si le modèle est disponible
        if (model != null) {
            try {
                // Récupérez la ressource de feedback à partir de l'URI
                Resource feedbackResource = model.getResource(feedbackUri);
                if (feedbackResource == null) {
                    return new ResponseEntity<>("Feedback not found", HttpStatus.NOT_FOUND);
                }

                // Supprimez toutes les triplets associés à la ressource de feedback
                model.removeAll(feedbackResource, null, null);

                // Supprimez également le type RDF si nécessaire
                model.remove(feedbackResource, RDF.type, model.createResource("http://rescuefood.org/ontology#Feedback"));

                // Enregistrez le modèle mis à jour
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Feedback deleted successfully: " + feedbackUri, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting feedback: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updateFeedback")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> updateFeedback(@RequestBody FeedbackDto feedbackDto) {
        if (model != null) {
            try {
                // Assurez-vous que le feedback existe
                Resource feedbackResource = model.getResource(feedbackDto.getFeedback());
                if (feedbackResource == null) {
                    return new ResponseEntity<>("Feedback not found", HttpStatus.NOT_FOUND);
                }

                // Définir la requête SPARQL DELETE/INSERT
                String modifyQuery =
                        "PREFIX j0: <http://rescuefood.org/ontology#> " +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "DELETE { " +
                                "  ?feedback j0:rating ?oldRating . " +
                                "  ?feedback j0:comment ?oldComment . " +
                                "} " +
                                "INSERT { " +
                                "  ?feedback j0:rating \"" + feedbackDto.getRating() + "\" . " +
                                "  ?feedback j0:comment \"" + feedbackDto.getComment() + "\" . " +
                                "} " +
                                "WHERE { " +
                                "  BIND(<" + feedbackDto.getFeedback() + "> AS ?feedback) ." +
                                "  OPTIONAL { ?feedback j0:rating ?oldRating } ." +
                                "  OPTIONAL { ?feedback j0:comment ?oldComment } ." +
                                "}";

                // Créer et exécuter la requête de mise à jour
                UpdateRequest updateRequest = UpdateFactory.create(modifyQuery);
                UpdateAction.execute(updateRequest, model);

                // Enregistrer le modèle mis à jour dans le fichier ontologique
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Feedback updated successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error updating feedback: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/addFeedback2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addFeedback2(@RequestBody FeedbackDto feedbackDto) {
        if (model != null) {
            try {
                // Définir la requête SPARQL INSERT
                String insertQuery =
                        "PREFIX j0: <http://rescuefood.org/ontology#> " +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "INSERT { " +
                                "  ?feedback rdf:type j0:Feedback . " +
                                "  ?feedback j0:rating \"" + feedbackDto.getRating() + "\" . " +
                                "  ?feedback j0:comment \"" + feedbackDto.getComment() + "\" . " +
                                "} WHERE { " +
                                "  BIND(IRI(CONCAT(\"http://rescuefood.org/ontology/Feedback_\", STRUUID())) AS ?feedback) " +
                                "}";

                // Créer la requête de mise à jour et l'exécuter
                UpdateRequest updateRequest = UpdateFactory.create(insertQuery);
                UpdateAction.execute(updateRequest, model);

                // Enregistrer le modèle mis à jour dans le fichier ontologique
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Feedback added successfully", HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding feedback: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/filterFeedback")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Object> filtrerFeedback(@RequestBody FeedbackDto feedbackDto) {
        String NS = "http://rescuefood.org/ontology#";

        // Charger le modèle à partir du fichier ontologie
        Model model = JenaEngine.readModel("data/rescuefood.owl");
        if (model == null) {
            return new ResponseEntity<>("Erreur lors du chargement du modèle.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Vérifier les valeurs de feedbackDto
        if (feedbackDto.getComment() == null) {
            return new ResponseEntity<>("Commentaire requis.", HttpStatus.BAD_REQUEST);
        }

        try {
            // Créer une requête SPARQL SELECT pour filtrer les feedbacks par commentaire
            String sparqlSelect =
                    "PREFIX j0: <" + NS + ">\n" +
                            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                            "SELECT ?feedback ?rating ?comment WHERE { " +
                            "  ?feedback rdf:type j0:Feedback . " +
                            "  ?feedback j0:rating ?rating . " +
                            "  ?feedback j0:comment ?comment . " +
                            "  FILTER (contains(?comment, \"" + feedbackDto.getComment() + "\")) . " +
                            "}";

            // Afficher la requête pour débogage
            System.out.println("Requête SPARQL : " + sparqlSelect);

            // Exécuter la requête SPARQL SELECT sur le modèle
            try (QueryExecution qexec = QueryExecutionFactory.create(sparqlSelect, model)) {
                ResultSet results = qexec.execSelect();
                List<Map<String, String>> feedbackList = new ArrayList<>();

                while (results.hasNext()) {
                    QuerySolution solution = results.nextSolution();
                    Map<String, String> feedback = new HashMap<>();
                    feedback.put("feedback", solution.get("feedback").toString()); // URI du feedback
                    feedback.put("rating", solution.get("rating").toString());     // Valeur littérale de la note
                    feedback.put("comment", solution.get("comment").toString());   // Valeur littérale du commentaire
                    feedbackList.add(feedback);
                }

                return new ResponseEntity<>(feedbackList, HttpStatus.OK); // Renvoie la liste des feedbacks en JSON
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la filtration du feedback : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/addCollectionEvent2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addCollectionEvent2(@RequestBody CollectionEventDto collectionEventDto) {
        if (model != null) {
            try {
                // Construct SPARQL INSERT query
                String insertQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "INSERT { " +
                                "  ?event rdf:type rescue:CollectionEvent . " +
                                "  ?event rescue:date \"" + collectionEventDto.getDate() + "\" . " +
                                "} " +
                                "WHERE { " +
                                "  BIND(IRI(CONCAT(\"http://rescuefood.org/resource/CollectionEvent/\", STRUUID())) AS ?event) " +
                                "}";

                // Create and execute the update request
                UpdateRequest updateRequest = UpdateFactory.create(insertQuery);
                UpdateAction.execute(updateRequest, model);

                // Save the updated model to the ontology file
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("CollectionEvent added successfully", HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding CollectionEvent: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/updateCollectionEvent2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> updateCollectionEvent2(@RequestBody CollectionEventDto collectionEventDto) {
        if (model != null) {
            try {
                // URI de l'événement à mettre à jour
                String eventResourceUri = collectionEventDto.getEvent(); // L'URI doit être passée dans collectionEventDto

                // Construire la requête SPARQL UPDATE
                String updateQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "DELETE { <" + eventResourceUri + "> rescue:date ?oldDate . } " +
                                "INSERT { <" + eventResourceUri + "> rescue:date \"" + collectionEventDto.getDate() + "\" . } " +
                                "WHERE { <" + eventResourceUri + "> rescue:date ?oldDate . }";

                // Créer et exécuter la requête de mise à jour
                UpdateRequest updateRequest = UpdateFactory.create(updateQuery);
                UpdateAction.execute(updateRequest, model);

                // Sauvegarder le modèle mis à jour dans le fichier RDF
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("CollectionEvent updated successfully: " + eventResourceUri, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error updating CollectionEvent: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/deleteCollectionEvent2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> deleteCollectionEvent2(@RequestBody CollectionEventDto collectionEventDto) {
        if (model != null) {
            try {
                // URI de l'événement à supprimer
                String eventResourceUri = collectionEventDto.getEvent(); // L'URI doit être passée dans collectionEventDto

                // Construire la requête SPARQL DELETE
                String deleteQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "DELETE WHERE { <" + eventResourceUri + "> ?p ?o . }";

                // Créer et exécuter la requête de suppression
                UpdateRequest updateRequest = UpdateFactory.create(deleteQuery);
                UpdateAction.execute(updateRequest, model);

                // Sauvegarder le modèle mis à jour dans le fichier RDF
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("CollectionEvent deleted successfully: " + eventResourceUri, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting CollectionEvent: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/addDirector2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> addDirector2(@RequestBody DirectorDto directorDto) {
        if (model != null) {
            try {
                // Générer un URI unique pour le director en utilisant UUID
                String directorResourceUri = "http://rescuefood.org/ontology#Director" + UUID.randomUUID().toString();

                // Construire la requête SPARQL INSERT
                String insertQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "INSERT DATA { " +
                                "<" + directorResourceUri + "> rdf:type rescue:Director ; " +
                                "rescue:contact \"" + directorDto.getContact() + "\" ; " +
                                "rescue:name \"" + directorDto.getName() + "\" . }";

                // Créer et exécuter la requête d'insertion
                UpdateRequest updateRequest = UpdateFactory.create(insertQuery);
                UpdateAction.execute(updateRequest, model);

                // Sauvegarder le modèle mis à jour
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Director added successfully: " + directorResourceUri, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding Director: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/updateDirector2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> updateDirector2(@RequestBody DirectorDto directorDto) {
        if (model != null) {
            try {
                // URI du directeur à mettre à jour
                String directorResourceUri = directorDto.getDirector(); // L'URI doit être passée dans directorDto

                // Construire la requête SPARQL UPDATE
                String updateQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "DELETE { <" + directorResourceUri + "> rescue:contact ?oldContact ; rescue:name ?oldName . } " +
                                "INSERT { <" + directorResourceUri + "> rescue:contact \"" + directorDto.getContact() + "\" ; " +
                                "rescue:name \"" + directorDto.getName() + "\" . } " +
                                "WHERE { <" + directorResourceUri + "> rescue:contact ?oldContact ; rescue:name ?oldName . }";

                // Créer et exécuter la requête de mise à jour
                UpdateRequest updateRequest = UpdateFactory.create(updateQuery);
                UpdateAction.execute(updateRequest, model);

                // Sauvegarder le modèle mis à jour
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Director updated successfully: " + directorResourceUri, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error updating Director: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/deleteDirector2")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> deleteDirector2(@RequestBody DirectorDto directorDto) {
        if (model != null) {
            try {
                // URI du directeur à supprimer
                String directorResourceUri = directorDto.getDirector(); // L'URI doit être passée dans directorDto

                // Construire la requête SPARQL DELETE
                String deleteQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#> " +
                                "DELETE WHERE { <" + directorResourceUri + "> ?p ?o . }";

                // Créer et exécuter la requête de suppression
                UpdateRequest updateRequest = UpdateFactory.create(deleteQuery);
                UpdateAction.execute(updateRequest, model);

                // Sauvegarder le modèle mis à jour
                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return new ResponseEntity<>("Director deleted successfully: " + directorResourceUri, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting Director: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/allAssociations")
    public String afficherAssociations() {
        if (model != null) {
            // Obtain the inferred model from the ontology
            Model inferredModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            // Check if the inferred model is null
            if (inferredModel == null) {
                return "Error: Inferred model is null.";
            }

            // Execute the SPARQL query to get all associations
            OutputStream res = JenaEngine.executeQueryFile(inferredModel, "data/query_Association.txt");

            // Check if the result is null
            if (res == null) {
                return "Error: Query execution returned null.";
            }

            // Return the results as a string
            return res.toString();
        } else {
            return "Error when reading model from ontology.";
        }
    }


    @PostMapping("/addAssociation")
    public ResponseEntity<String> addAssociation(@RequestBody AssociationDto associationDto) {
        if (model != null) {
            try {
                // Define the SPARQL INSERT query
                String insertQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#Association> " +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "INSERT { " +
                                "  ?association rdf:type rescue:Association . " +
                                "  ?association rescue:nom \"" + associationDto.getNom() + "\" . " +
                                "  ?association rescue:adresse \"" + associationDto.getAdresse() + "\" . " +
                                "  ?association rescue:contact \"" + associationDto.getContact() + "\" . " +
                                "} WHERE { " +
                                "  BIND(IRI(CONCAT(\"http://rescuefood.org/ontology/Association_\", STRUUID())) AS ?association) " +
                                "}";

                UpdateRequest updateRequest = UpdateFactory.create(insertQuery);
                UpdateAction.execute(updateRequest, model);

                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return ResponseEntity.status(HttpStatus.CREATED).body("Association added successfully");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding association: " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when reading model from ontology");
        }
    }

    @PutMapping("/modifyAssociation")
    public ResponseEntity<String> modifyAssociation(@RequestBody AssociationDto associationDto) {
        if (model != null) {
            try {
                Resource associationResource = model.getResource(associationDto.getNom());
                if (associationResource == null) {
                    return new ResponseEntity<>("Association not found", HttpStatus.NOT_FOUND);
                }

                String modifyQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#Association> " +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "DELETE { " +
                                "  ?association rescue:nom ?oldNom . " +
                                "  ?association rescue:adresse ?oldAdresse . " +
                                "  ?association rescue:contact ?oldContact . " +
                                "} " +
                                "INSERT { " +
                                "  ?association rescue:nom \"" + associationDto.getNom() + "\" . " +
                                "  ?association rescue:adresse \"" + associationDto.getAdresse() + "\" . " +
                                "  ?association rescue:contact \"" + associationDto.getContact() + "\" . " +
                                "} " +
                                "WHERE { " +
                                "  BIND(<" + associationDto.getNom() + "> AS ?association) ." +
                                "  OPTIONAL { ?association rescue:nom ?oldNom } ." +
                                "  OPTIONAL { ?association rescue:adresse ?oldAdresse } ." +
                                "  OPTIONAL { ?association rescue:contact ?oldContact } ." +
                                "}";

                UpdateRequest updateRequest = UpdateFactory.create(modifyQuery);
                UpdateAction.execute(updateRequest, model);

                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return ResponseEntity.ok("Association modified successfully");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error modifying association: " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when reading model from ontology");
        }
    }

    @DeleteMapping("/deleteAssociation")
    public ResponseEntity<String> deleteAssociation(@RequestBody AssociationDto associationDto) {
        String associationUri = associationDto.getNom();

        if (model != null) {
            try {
                String deleteQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#Association> " +
                                "DELETE WHERE { " +
                                "  <" + associationUri + "> ?p ?o ." +
                                "}";

                UpdateRequest updateRequest = UpdateFactory.create(deleteQuery);
                UpdateAction.execute(updateRequest, model);

                JenaEngine.saveModel(model, "data/rescuefood.owl");

                return ResponseEntity.ok("Association deleted successfully");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting association: " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when reading model from ontology");
        }
    }
    @GetMapping("/allDemande")
    public String getAllDemandes() {
        if (model != null) {
            try {
                Model inferedModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");
                OutputStream res = JenaEngine.executeQueryFile(inferedModel, "data/query_Demande.txt");
                System.out.println(res);  // Print the result for debugging
                return res.toString();
            } catch (Exception e) {
                e.printStackTrace();  // Log the exception for debugging
                return "Error when executing query: " + e.getMessage();
            }
        } else {
            return "Error when reading model from ontology";
        }
    }


    // Create operation: Add a new demande
    @PostMapping("/addDemande")
    public ResponseEntity<String> addDemande(@RequestBody Map<String, Object> demandeData) {
        if (model != null) {
            try {
                String typeNourriture = (String) demandeData.get("typeNouritture");
                Float quantiteDemande = Float.parseFloat(demandeData.get("quantiteDemande").toString());
                String date = (String) demandeData.get("date");

                String insertQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#Demande> " +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "INSERT { " +
                                "  ?demande rdf:type rescue:Demande . " +
                                "  ?demande rescue:typeNourriture \"" + typeNourriture + "\" . " +
                                "  ?demande rescue:quantiteDemande " + quantiteDemande + " . " +
                                "  ?demande rescue:date \"" + date + "\" . " +
                                "} WHERE { " +
                                "  BIND(IRI(CONCAT(\"http://rescuefood.org/ontology/Demande_\", STRUUID())) AS ?demande) " +
                                "}";

                UpdateRequest updateRequest = UpdateFactory.create(insertQuery);
                UpdateAction.execute(updateRequest, model);

                JenaEngine.saveModel(model, "data/rescuefood.owl");
                return new ResponseEntity<>("Demande added successfully", HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error adding demande: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update operation: Modify an existing demande
    @PutMapping("/modifyDemande")
    public ResponseEntity<String> modifyDemande(@RequestBody Map<String, Object> demandeData) {
        if (model != null) {
            try {
                String demandeUri = (String) demandeData.get("demandeUri");
                String typeNourriture = (String) demandeData.get("typeNouritture");
                Float quantiteDemande = Float.parseFloat(demandeData.get("quantiteDemande").toString());
                String date = (String) demandeData.get("date");

                String modifyQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#Demande> " +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "DELETE { " +
                                "  ?demande rescue:typeNourriture ?oldType . " +
                                "  ?demande rescue:quantiteDemande ?oldQuantite . " +
                                "  ?demande rescue:date ?oldDate . " +
                                "} " +
                                "INSERT { " +
                                "  ?demande rescue:typeNourriture \"" + typeNourriture + "\" . " +
                                "  ?demande rescue:quantiteDemande " + quantiteDemande + " . " +
                                "  ?demande rescue:date \"" + date + "\" . " +
                                "} " +
                                "WHERE { " +
                                "  BIND(<" + demandeUri + "> AS ?demande) ." +
                                "  OPTIONAL { ?demande rescue:typeNourriture ?oldType } ." +
                                "  OPTIONAL { ?demande rescue:quantiteDemande ?oldQuantite } ." +
                                "  OPTIONAL { ?demande rescue:date ?oldDate } ." +
                                "}";

                UpdateRequest updateRequest = UpdateFactory.create(modifyQuery);
                UpdateAction.execute(updateRequest, model);

                JenaEngine.saveModel(model, "data/rescuefood.owl");
                return new ResponseEntity<>("Demande modified successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error modifying demande: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete operation: Delete a demande
    @DeleteMapping("/deleteDemande")
    public ResponseEntity<String> deleteDemande(@RequestBody Map<String, Object> demandeData) {
        if (model != null) {
            try {
                String demandeUri = (String) demandeData.get("demandeUri");

                String deleteQuery =
                        "PREFIX rescue: <http://rescuefood.org/ontology#Demande> " +
                                "DELETE WHERE { " +
                                "  <" + demandeUri + "> ?p ?o ." +
                                "}";

                UpdateRequest updateRequest = UpdateFactory.create(deleteQuery);
                UpdateAction.execute(updateRequest, model);

                JenaEngine.saveModel(model, "data/rescuefood.owl");
                return new ResponseEntity<>("Demande deleted successfully", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error deleting demande: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Error when reading model from ontology", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}