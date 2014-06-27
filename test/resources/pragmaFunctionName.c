typedef int* __WAIT_STATUS;
#pragma critTer:1:resources/functionName.c:
int functionName_good(int a) {
#pragma critTer:2:resources/functionName.c:
	return a;
#pragma critTer:3:resources/functionName.c:
}
#pragma critTer:4:resources/functionName.c:

#pragma critTer:5:resources/functionName.c:
int bad_func(int x) {
#pragma critTer:6:resources/functionName.c:
	return 0;
#pragma critTer:7:resources/functionName.c:
}
#pragma critTer:8:resources/functionName.c:

#pragma critTer:9:resources/functionName.c:
int reallybad(int z) {
#pragma critTer:10:resources/functionName.c:
	return 1;
#pragma critTer:11:resources/functionName.c:
}
