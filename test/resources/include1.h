typedef int* __WAIT_STATUS;
#pragma critTer:1:../test/resources/include1.h:
#pragma critTer:1:include1.h
#pragma critTer:2:../test/resources/include1.h:
#ifndef INCLUDE1_INCLUDED
#pragma critTer:3:../test/resources/include1.h:
#define INCLUDE1_INCLUDED
#pragma critTer:4:../test/resources/include1.h:
#pragma critTer:startStudentInclude:
#pragma critTer:5:../test/resources/include1.h:
#pragma critTer:startStudentInclude:
#include "include2.h"
#pragma critTer:endStudentInclude:
#pragma critTer:6:../test/resources/include1.h:

#pragma critTer:7:../test/resources/include1.h:
#pragma critTer:startStdInclude:
#include <assert.h>
#pragma critTer:endStdInclude:
#pragma critTer:8:../test/resources/include1.h:

#pragma critTer:9:../test/resources/include1.h:
void freeAll(void);
#pragma critTer:10:../test/resources/include1.h:

#pragma critTer:11:../test/resources/include1.h:
#endif
