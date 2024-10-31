package tn.sem.websem;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.FileManager;
import org.apache.jena.update.*;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;

/**
 *
 * @author DO.ITSUDPARIS
 */
public class JenaEngine {

    //static private String RDF = "http://www.semanticweb.org/wassim/ontologies/2024/9/untitled-ontology-17";
    static private String RDF ="http://rescuefood.org/ontology#";

    /**
     * Charger un modle partir dun fichier owl
     * @param args
     * + Entree: le chemin vers le fichier owl
     * + Sortie: l'objet model jena
     */
    static public Model readModel(String inputDataFile) {
// create an empty model
        Model model = ModelFactory.createDefaultModel();

// use the FileManager to find the input file
        InputStream in = FileManager.get().open(inputDataFile);
        if (in == null) {
            System.out.println("Ontology file: " + inputDataFile + "not found");
            return null;
        }
// read the RDF/XML file
        model.read(in, "");

        try {
            in.close();
        } catch (IOException e) {
// TODO Auto-generated catch block
            return null;
        }
        return model;
    }
    /**
     * Faire l'inference
     * @param args
     * + Entree: l'objet model Jena avec le chemin du fichier de
    regles
     * + Sortie: l'objet model infere Jena
     */
    static public Model readInferencedModelFromRuleFile(Model model,
                                                        String inputRuleFile) {
        InputStream in = FileManager.get().open(inputRuleFile);
        if (in == null) {
            System.out.println("Rule File: " + inputRuleFile + " not found");
            return null;
        } else {
            try {
                in.close();
            } catch (IOException e) {
// TODO Auto-generated catch block
                return null;
            }
        }
        List rules = Rule.rulesFromURL(inputRuleFile);
        GenericRuleReasoner reasoner = new
                GenericRuleReasoner(rules);
        reasoner.setDerivationLogging(true);
        reasoner.setOWLTranslation(true); // not needed in RDFS case
        reasoner.setTransitiveClosureCaching(true);
        InfModel inf = ModelFactory.createInfModel(reasoner, model);
        return inf;
    }

    /**
     * Executer une requete
     * @param args
     * + Entree: l'objet model Jena avec une chaine des caracteres
    SparQL
     * + Sortie: le resultat de la requete en String
     */
    static public OutputStream executeQuery(Model model, String
            queryString) {
        Query query = QueryFactory.create(queryString);
// No reasoning
// Execute the query and obtain results
        QueryExecution qe = QueryExecutionFactory.create(query,

                model);
        ResultSet results = qe.execSelect();
        OutputStream output = new OutputStream() {
            private StringBuilder string = new StringBuilder();
            public void write(int b) throws IOException {
                this.string.append((char) b);
            }
            //Netbeans IDE automatically overrides this toString()
            public String toString() {
                return this.string.toString();
            }
        };
        ResultSetFormatter.outputAsJSON(output, results);
        return output;
    }
    /**
     * Executer un fichier d'une requete
     * @param args
     * + Entree: l'objet model Jena avec une chaine des caracteres
    SparQL
     * + Sortie: le resultat de la requete en String
     */
    static public OutputStream executeQueryFile(Model model, String
            filepath) {
        File queryFile = new File(filepath);
// use the FileManager to find the input file
        InputStream in = FileManager.get().open(filepath);
        if (in == null) {
            System.out.println("Query file: " + filepath + " not found");
            return null;
        } else {
            try {
                in.close();
            } catch (IOException e) {
// TODO Auto-generated catch block
                return null;
            }
        }
        String queryString = FileTool.getContents(queryFile);
        return executeQuery(model, queryString);
    }
    /**
     * Save a Jena model to a specified file.
     *
     * @param model     The Jena model to be saved.
     * @param filePath  The file path where the model will be saved.
     */
    static public void saveModel(Model model, String filePath) {
        try (OutputStream out = new FileOutputStream(filePath)) {
            model.write(out, "RDF/XML"); // Change format to "TURTLE" or others as needed
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String readFileAsString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }


    static public void executeUpdate(String updateString, Model model) {
        UpdateRequest updateRequest = UpdateFactory.create(updateString);
        // Create a GraphStore/Dataset from the model
        Dataset dataset = DatasetFactory.create(model);
        // Execute the update on the dataset
        UpdateProcessor processor = UpdateExecutionFactory.create(updateRequest, dataset);
        processor.execute();
    }

}