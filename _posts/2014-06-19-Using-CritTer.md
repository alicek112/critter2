---
title: "Using CritTer2"
---

## Install and Build ##

Install and build CritTer2 through the following commands from the command-line, 
in the directory you want CritTer2 installed:

```java
$ git clone https://github.com/alicek112/critter2
$ cd critter2
$ ./gradlew
```

Then add it to your PATH:

     $ export PATH=.:$PATH

## Run ##

To run CritTer2 on all c files in the working directory:

```java
$ critTer2 *.c
```

To run CritTer2 on a specific file:

```java
$ critTer2 filename.c
```

There is no need to include any header files in the call to CritTer2, simply
make sure the header files needed are in the directory CritTer2 is being
called from.

## Customizing CritTer2 ##

You can change CritTer2 by editing the source code. To do this, go into the
directory where CritTer2 is installed and then access the source code files:

```java
$ cd critter2/src/critter2
```

`Critter.java` is the file that runs the checks on C source code. To change
which checks are being run, simply add or remove checks from the array
`CritterCheck[] checks`, as indicated in the code.

To add new checks, or change the specifics of a check, go into the `checks`
directory, where each check is listed as a separate class. Those checks that
have built-in customizable values, like `CheckFileLength.java`, will have
those as static variables that you can change. If you want to add a new
check, simply create a new class in the `checks` directory, following the
template of `SampleCheck.java`.

When you are done customizing CritTer2, don't forget to re-build it by 
running the `./gradle build` command from the `critter2` directory.