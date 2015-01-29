# UrlShortener2014

This is the shared repository for the project developed in this course.

## Projects

* [Common](common) is the project that provides a minimum set of shared features.
* [Demo](demo) is the template project and the sandbox for solving blocking issues.

## Teams

* [BangladeshGreen](bangladeshGreen): Alberto (leader), Óscar and Roberto
* [GoldenBrown](goldenBrown): Javier (leader), Jorge and Gabriel
* [GoldenPoppy](goldenPoppy): Adrián (leader), Javier and Andrés
* [MediumCandy](mediumCandy): Carlos (leader), Daniel, Alberto and Guillermo
* [OldBurgundy](oldBurgundy): Pablo (leader), Jorge and Ismael
* [RichCarmine](richCarmine): Adrian (leader), Sandra and David
* [TaupeGray](taupeGray): Rubén (leader), Victor and Marcela
* [DimGray](dimGray): Carlos (leader) and Paulo

## Starting procedure

* The team leader forks this repository
* Each team member forks the fork of the respective team leader
* Import your own fork into eclipse. You must import the common project, the demo project and your project. For example, if your project is _BangladeshGreen_ you must import `UrlShortener2014.common`, `UrlShortener2014.demo` and `UrlShortener2014.bangladeshGreen`. Other projects are optional.
* Create the folder `src\main\java` in your project.
* Copy the contents of `src\main\java` from `UrlShortener2014.demo` into your project.
* Rename the package `urlshortener2014.demo` to your color. For example if your project is _BangladeshGreen_ you must rename it to `urlshortener2014.bangladeshgreen`.
* Test that your program run using command line (`gradle run`) or within eclipse (either as Java application or as Gradle application).
* Do `$ curl -v -d "url=http://www.unizar.es/" -X POST http://localhost:8080/link` and check that appears a line in the console that contains `u.d.web.UrlShortenerControllerWithLogs`

Now you can start to add new functionality to your project.

## Push & Pull

* Each team member should work in its local repository.
* Periodically, each team member must __push__ its work to its repository in GitHub and then make a __pull request__ for sent your changes to the repository of the team.
* Periodically, each team member must __pull__ from GitHub to fetch and merge changes from remote repositories (your team changes or changes in the original repository).

Do it frequently, as this is one of the factors that will be taken into account in the evaluation of your work.
