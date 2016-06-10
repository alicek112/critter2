typedef int* __WAIT_STATUS;
#pragma critTer:1:hello.c:
#pragma critTer:startStdInclude:
#include <stdio.h>
#pragma critTer:endStdInclude:
#pragma critTer:startStudentInclude:
#include "include3.h"
#pragma critTer:endStudentInclude:
#pragma critTer:2:hello.c:

#pragma critTer:3:hello.c:
int helloWorld(int x) {
#pragma critTer:4:hello.c:
  char pcLine[1000];
#pragma critTer:5:hello.c:
  printf("Hello world %s", argv[1]);
#pragma critTer:8:hello.c:
}
#pragma critTer:9:hello.c:
/* This is a function comment for a static function. */
#pragma critTer:6:hello.c:
static void foo(int c) {
#pragma critTer:7:hello.c:
  printf("Hello world.\n");
#pragma critTer:8:hello.c:
}
#pragma critTer:9:hello.c:
int foo2(int x, int a, int b) {
#pragma critTer:10:hello.c:
   printf("Return.\n");
#pragma critTer:11:hello.c:
}
