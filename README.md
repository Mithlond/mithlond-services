# 1. Mithlond: Services

The Mithlond: Services project structure contains Software Components (i.e. collaborating Maven projects) for
shared backend services in typical restful style.

## 1.1. Release Documentation

Release documentation (including Maven site documentation) can be found
at [The Mithlond: Services Documentation Site](http://mithlond.github.io/mithlond-services).
Select the release version you are interested in, to find its full Maven site documentation.

# 2. Getting and building mithlond-services

The mithlond-services is a normal Git-based Maven project.
It is simple to get and build it.

## 2.1. Getting the repository

Clone the repository, and fetch all tags:

```
git clone https://github.com/Mithlond/mithlond-services.git

cd mithlond-services

git fetch --tags
```

## 2.2. Building the Services project

For the latest development build, simply run the build against the latest master branch revision:

```
mvn clean install
```

For a particular version, checkout its release tag and build normally:

```
git checkout mithlond-services-1.0.1

mvn clean install
```

All tags (and hence also all release versions) are visible using the command

```
git tag -l
```

### 2.2.1. Building with different Maven versions

For building the project with another Maven version, simply run the following
script, where the `${MAVEN_VERSION}` should be substituted for a version number
such as `3.3.3`:

```
mvn -N io.takari:maven:wrapper -Dmaven=${MAVEN_VERSION}

./mvnw --show-version --errors --batch-mode validte dependency:go-offline

./mvnw --show-version --errors --batch-mode clean verify site
```

In the windows operating system, use `mvnw.bat` instead.