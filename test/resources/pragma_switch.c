typedef int* __WAIT_STATUS;
#pragma critTer:1:../test/resources/switch.c:
/* Comment */
#pragma critTer:2:../test/resources/switch.c:

#pragma critTer:3:../test/resources/switch.c:
int main(void) {
#pragma critTer:4:../test/resources/switch.c:
	char x = 'a';
#pragma critTer:5:../test/resources/switch.c:
	
#pragma critTer:6:../test/resources/switch.c:
	switch(x) {
#pragma critTer:7:../test/resources/switch.c:
		case 'a':
#pragma critTer:8:../test/resources/switch.c:
			printf("a");
#pragma critTer:9:../test/resources/switch.c:
		case 'b':
#pragma critTer:10:../test/resources/switch.c:
			printf("b");
#pragma critTer:11:../test/resources/switch.c:
			break;
#pragma critTer:12:../test/resources/switch.c:
	}
#pragma critTer:13:../test/resources/switch.c:
	
#pragma critTer:14:../test/resources/switch.c:
	return 0;
#pragma critTer:15:../test/resources/switch.c:
}
