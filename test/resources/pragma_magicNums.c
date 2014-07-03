typedef int* __WAIT_STATUS;
#pragma critTer:1:magicNums.c:
#pragma critTer:startStdInclude:
#define SIGQUIT 99
#pragma critTer:endStdInclude:
#pragma critTer:2:magicNums.c:

#pragma critTer:3:magicNums.c:
int func(int a) {
#pragma critTer:4:magicNums.c:
    int x = 99;
#pragma critTer:5:magicNums.c:
    switch(a) {
#pragma critTer:6:magicNums.c:
        case 1:
#pragma critTer:7:magicNums.c:
            printf(SIGQUIT);
#pragma critTer:8:magicNums.c:
        case b:
#pragma critTer:9:magicNums.c:
            printf("b");
#pragma critTer:10:magicNums.c:
            break;
#pragma critTer:11:magicNums.c:
    }
#pragma critTer:12:magicNums.c:
    
#pragma critTer:13:magicNums.c:
    if (a < 15.2)
#pragma critTer:14:magicNums.c:
        return 22;
#pragma critTer:15:magicNums.c:
    else
#pragma critTer:16:magicNums.c:
        return a;
#pragma critTer:17:magicNums.c:
}
