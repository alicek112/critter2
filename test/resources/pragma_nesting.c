typedef int* __WAIT_STATUS;
#pragma critTer:1:../test/resources/nesting.c:
/* Comment */
#pragma critTer:2:../test/resources/nesting.c:
int main() {
#pragma critTer:3:../test/resources/nesting.c:
	int i;
#pragma critTer:4:../test/resources/nesting.c:
	int j;
#pragma critTer:5:../test/resources/nesting.c:
	int k;
#pragma critTer:6:../test/resources/nesting.c:
	int x = 1;
#pragma critTer:7:../test/resources/nesting.c:
	
#pragma critTer:8:../test/resources/nesting.c:
	for (i = 0; i < 10; i++) {
#pragma critTer:9:../test/resources/nesting.c:
		for (j = 0; j < i; j++) {
#pragma critTer:10:../test/resources/nesting.c:
			for (k = 0; k < j; k++) {
#pragma critTer:11:../test/resources/nesting.c:
				if (x > 2) {
#pragma critTer:12:../test/resources/nesting.c:
					return 1;
#pragma critTer:13:../test/resources/nesting.c:
				}
#pragma critTer:14:../test/resources/nesting.c:
			}
#pragma critTer:15:../test/resources/nesting.c:
		}
#pragma critTer:16:../test/resources/nesting.c:
	}
#pragma critTer:17:../test/resources/nesting.c:
	
#pragma critTer:18:../test/resources/nesting.c:
	return 0;
#pragma critTer:19:../test/resources/nesting.c:
}
