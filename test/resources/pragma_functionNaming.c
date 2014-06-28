typedef int* __WAIT_STATUS;
#pragma critTer:1:../../test/resources/functionNaming.c:
/* bad comment */
#pragma critTer:2:../../test/resources/functionNaming.c:
int prefix_Func(int something) {
#pragma critTer:3:../../test/resources/functionNaming.c:
  int i;
#pragma critTer:4:../../test/resources/functionNaming.c:

#pragma critTer:5:../../test/resources/functionNaming.c:
  i = 2;
#pragma critTer:6:../../test/resources/functionNaming.c:
}
#pragma critTer:7:../../test/resources/functionNaming.c:

#pragma critTer:8:../../test/resources/functionNaming.c:
int badprefix_Func(int b) {
#pragma critTer:9:../../test/resources/functionNaming.c:
  int i;
#pragma critTer:10:../../test/resources/functionNaming.c:
  i = 1;
#pragma critTer:11:../../test/resources/functionNaming.c:
}
