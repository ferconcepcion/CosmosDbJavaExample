package com.mycompany.cosmosdbexample;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class CosmosDbExample {
    private static final String ENDPOINT = "ENDPOINT";
    private static final String KEY = "KEY";
    private static final String DATABASE_ID = "CosmosDbExample";
    private static final String CONTAINER_ID = "CosmosDbContainerExample";

    public static void main(String[] args) throws Exception {
        CosmosDbExample c = new CosmosDbExample();
        
        // Obtiene cliente
        CosmosClient client = c.createCosmosClient();
        
        // Obtiene la base de datos y el contenedor
        CosmosDatabase database = c.createDatabaseIfNotExists(client);
        CosmosContainer container = c.createContainerIfNotExists(client, database);
        
        // Genera listado de personas
        List<Person> personListToInsert = c.buildPersonList(50);

        // Inserta listado de personas
        //c.insertPersonList(container, personListToInsert);
        
        String query = "SELECT * FROM c";

        // Ejecutar query e imprimir resultados
        c.printPersonList(client, container, query);
        
        // Actualizar objeto de tipo Person con los nuevos datos
        Person updatedPerson = new Person("1", "John", "Doe", "johndoe@example.com", 30);
        c.updatePerson(client, container, updatedPerson);
        System.out.println("Objeto Person actualizado: " + updatedPerson.toString());
        
        // Clean up resources
        client.close();
    }
    
    private CosmosClient createCosmosClient(){
        // Build a CosmosClient object to connect to your Cosmos DB instance
        //CosmosClientBuilder clientBuilder = new CosmosClientBuilder();
        //CosmosClient client = clientBuilder
        //    .endpoint(ENDPOINT)
        //    .key(KEY)
        //    .directMode()
        //    .buildClient();
        
        //ConnectionPolicy defaultPolicy = ConnectionPolicy.getDefaultPolicy();
        
        //  Create sync client
        //  <CreateSyncClient>
        CosmosClient client = new CosmosClientBuilder()
            .endpoint(ENDPOINT)
            .key(KEY)
            .consistencyLevel(ConsistencyLevel.EVENTUAL)
            .buildClient();
            //.setConnectionPolicy(defaultPolicy)
        return client;
    }
    
    private CosmosDatabase createDatabaseIfNotExists(CosmosClient client){
        client.createDatabaseIfNotExists(DATABASE_ID);
        CosmosDatabase database = client.getDatabase(DATABASE_ID);
        
        return database;
    }
    
    private CosmosContainer createContainerIfNotExists(CosmosClient client, CosmosDatabase database){
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(CONTAINER_ID, "/id");
        
        database.createContainerIfNotExists(containerProperties);
        
        CosmosContainer container = database.getContainer(CONTAINER_ID);
        
        return container;
    }
    
    private List<Person> buildPersonList(int num){
        PersonGenerator generator = new PersonGenerator();
        List<Person> personListToInsert = generator.generatePeopleList(num);
        
        return personListToInsert;
    }
    
    private void insertPersonList(
            CosmosContainer container, 
            List<Person> personListToInsert){
        
        ObjectMapper mapper = new ObjectMapper();
        
        for (Person p : personListToInsert) {
        	
        	// Crea un nuevo objeto JsonNode y agrega algunas propiedades
            JsonNode item = mapper.createObjectNode()
                .put("id", p.getId())
                .put("firstName", p.getFirstName())
                .put("lastName", p.getLastName())
                .put("email", p.getEmail())
                .put("age", p.getAge());
            
            // Insert the item into Cosmos DB
            container.createItem(item);
        } 
    }
    
    private void printPersonList(CosmosClient client, CosmosContainer container, String query){
        // Set some common query options
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        //queryOptions.setEnableCrossPartitionQuery(true); //No longer necessary in SDK v4
        //  Set query metrics enabled to get metrics around query executions
        queryOptions.setQueryMetricsEnabled(true);

        CosmosPagedIterable<JsonNode> personPagedIterable = container.queryItems(
        		query, queryOptions, JsonNode.class);

        personPagedIterable.iterableByPage(10).forEach(cosmosItemPropertiesFeedResponse -> {
            List<JsonNode> personList = cosmosItemPropertiesFeedResponse
                .getResults();

    		for (JsonNode p : personList) {
    			System.out.println(p.toString());
    		}     
            
        });
        
    }
    
    private Person updatePerson(CosmosClient client, CosmosContainer container, Person updatedPerson){
        
        // Actualizar el objeto en Cosmos DB
        CosmosItemResponse<Person> response = container.replaceItem(updatedPerson, "1", new PartitionKey("1"), null);

        // Obtener el objeto actualizado de la respuesta
        Person updatedPersonResponse = response.getItem();

        return updatedPersonResponse;
    }
}
