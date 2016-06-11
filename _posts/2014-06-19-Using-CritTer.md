---
title: "Using CritTer2"
---

## Install and Build ##

Install and build CritTer2 through the following commands from the command-line, 
in the directory you want CritTer2 installed:

```bash
$ git clone https://github.com/alicek112/critter2
$ cd critter2
$ ./gradlew
```

CritTer2 uses the gradle builder to automate routine build tasks. The default
gradle build (`./gradlew`) compiles and runs a series of unit tests. Some
other useful uses of gradle include making a javadoc:

```bash
$ ./gradlew javadoc
```

running style checks:

```bash
$ ./gradlew check
```

and running unit tests (included in default build):

```bash
$ ./gradlew test
```

## Run ##

To run CritTer2 on all c files in the working directory:

```bash
$ critTer *.c
```

To run CritTer2 on a specific file:

```bash
$ critTer filename.c
```

There is no need to include any header files in the call to CritTer2, simply
make sure the header files needed are in the directory CritTer2 is being
called from.

## Customizing CritTer2 ##

You can change CritTer2 by editing the source code. To do this, go into the
directory where CritTer2 is installed and then access the source code files:

```bash
$ cd critter2/src/critter2
```

`Critter.java` is the file that runs the checks on C source code. To change
which checks are being run, simply add or remove checks from the array
`CritterCheck[] checks`, as indicated in the code.

To change the specifics of a check, go into the `checks`
directory, where each check is listed as a separate class. Those checks that
have built-in customizable values, like `CheckFileLength.java`, will have
those as static variables that you can change.

When you are done customizing CritTer2, don't forget to re-build it by 
running the `./gradlew` command from the `critter2` directory.

## Creating New Checks ##

To add new checks to CritTer2, go into the source code as you would to 
customize a check. To create a new check, create a new class in the `checks` 
directory, based on the template of `critter2/checks/SampleCheck.java`.
Change the function 'check()' to implement the style check you wish to add. 

Errors are reported using the `reportErrorPos()` function, which takes as
arguments the CETUS parse tree node where the error occured, and a format
string describing the error. For example, the following code will produce
the following error:

```java
reportErrorPos(t, "No puns in code.");
```

```bash
filename.c: line 20: No puns in code.
```

You should also write unit tests for the checks you've written. To add a 
unit test, create a new class in the `test/critter2/checks/` directory.
You can put necessary resources (c files to test) into `test/resources/`.
These tests will be run by the default gradle build command.

To contribute these changes to the central repository, please clone git 
repository, commit your changes to cloned repository, and send me a pull 
request.
