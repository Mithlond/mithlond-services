# About Mithlond Services: organisation-model

A "Model" project contains the Entity classes that define the meaning (*semantics*) of data read from persistent 
storage such as a database and the clients communicating with the service. In Java terms, Model project should 
contain POJO entity classes decorated with appropriate JPA- and JAXB-annotations. Also, model projects should
define unit tests which illustrate and validate the standard conversion between transport forms and objects.

## Model entities and systems integration

POJO entity classes correspond to data describing the system's model/state. For the most part, this implies that
POJO entities must be JPA annotated. However, to simplify unit tests and transporting data between systems (a.k.a. 
systems integration), entity classes should also be able to marshal into XML or JSON forms. Model entities form the 
basis of normal systems integration; as illustrated in the image below, entities are normally ...

1. read from database tables
2. marshalled into message payload
3. transmitted across systems boundaries
4. unmarshalled from message payloads
5. inserted into database tables

<img src="images/integration101.png" style="border: solid DarkGray 1px;" />
 
Typically, a POJO entity represents a "thing" within a system. This "thing" is normally stored as a row within a 
table (or set of tables related with joins) within a database, but should also be transportable between systems as 
payload of messages or requests/remote calls.   

## Tutorials 

The documented tutorials/walk-throughs define how to... 

1. [... create a POJO entity class](./creating_pojo_entities.html), and
2. [... create unit tests for the POJO entity](./creating_entity_unit_tests.html)
3. [... create integration tests for the POJO entity](./creating_entity_integration_tests.html)
4. [... call named query](./call_named_query.md)

