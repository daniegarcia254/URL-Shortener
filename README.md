# URL-Shortener

## Projects

* [Common](common) is the project that provides a minimum set of shared features for short url's.
* [mediumCandy](mediumCandy) is the project that contains the client web app and the new feaures implemented.([doc available](mediumCandy/doc))

## Functionalities implemented:

* Short an url if reachable and valid
* Short url's included in a CSV file
* Short url with personalized branded-links
* Get info and stats over a shortened url

## Run the project
You only need go to the _mediumCandy_ directory and exec `gradle run`.

Now the app will be available at `localhost:8080`


## Build the project --> Generate WAR file to deploy on Tomcat
You only need go to the _mediumCandy_ directory and exec `gradle war`.

The .war file will be generated and saved in the _buil/libs_ folder.


*Important note*: for run and build the project, use gradle 2.10 version
