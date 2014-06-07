typedef int* __WAIT_STATUS;
#pragma critTer:1:hello.c:
#pragma critTer:startStdInclude:
#include <stdio.h>
#pragma critTer:endStdInclude:
#pragma critTer:startStudentInclude:
#include "include1.h"
#pragma critTer:endStudentInclude:
#pragma critTer:2:hello.c:

#pragma critTer:3:hello.c:
int main(int argc, char *argv[]) {
#pragma critTer:4:hello.c:
  char pcLine[1000];
#pragma critTer:5:hello.c:
  printf("Hello world %s", argv[1]);
#pragma critTer:8:hello.c:
  if (true) 
    printf("True");
#pragma critTer:9:hello.c:
  else {printf("False");}
#pragma critTer:6:hello.c:
  return 0;
#pragma critTer:7:hello.c:
}
