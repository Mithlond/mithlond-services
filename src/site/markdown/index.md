# About Mithlond: Services

The Mithlond: Services reactor contains a suite of
[Nazgul Software Components](http://www.jguru.se/nazgul/nazgul_tools/current/theory/software_components.html)
which are used to implement web services according to the RESTful paradigm and pattern.
This means that the projects within the Mithlond: Services reactor contains

The overall enterprise architecture places the Mithlond: Services as the primary RESTful service
provider communicating with different kinds of services. In addition to the RESTful WAR (containing all the service
implementations themselves), the Mithlond services rely on an external identity management server ("IDM") to
authenticate all service usage. In the image below, the sections describe the following:

1. **clients**. Applications presenting data to users or other applications.

2. **servers**. Server applications required to interact with services.

3. **databases**. Contains the data shown to or manipulated by calls from clients.

<img src="images/plantuml/system_structure.png" style="border: solid DarkGray 1px;" />


## The Mithlond: Services build reactor

The reactor consists of 3 main structures:

1. **shared**. Shared utilities for simplified testing and generally available algorithms.
   Also contains a commonly used model and API for Authorization.
 
2. **organisation**. A [Nazgul Software Components](http://www.jguru.se/nazgul/nazgul_tools/current/theory/software_components.html)
   which presents a model, API (including JAXB/JAX-RS transport model) and EJB implementation project for services
   related to the common needs of all organisations (memberships, users, groups, foods, activities etc.)
   
3. **content**. A [Nazgul Software Components](http://www.jguru.se/nazgul/nazgul_tools/current/theory/software_components.html)
   which presents a model, API (including JAXB/JAX-RS transport model) and EJB implementation project for services
   related to retrieving content for clients. In this context, "content" typically refers to either menu structures
   (for navigation) or article data (text to be rendered within clients).

## Getting the Services project

Clone the repository, and get all tags:

```
git clone https://github.com/Mithlond/mithlond-services.git

cd mithlond-services

git fetch --tags
```

## Building the Services project

For the latest development build, simply run the build against the latest master branch revision:  

```
mvn clean install
```

For a particular version, checkout its release tag and build normally:
 
```
git checkout mithlond-services-1.13.2

mvn clean install
```

This produces a WAR which should contain all of the built data. However, this RESTful WAR is not
ready to be set into production, as it will