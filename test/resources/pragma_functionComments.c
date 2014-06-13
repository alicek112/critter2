typedef int* __WAIT_STATUS;
#pragma critTer:1:../test/resources/functionCommentParam.c:
int global = 5;
#pragma critTer:2:../test/resources/functionCommentParam.c:

#pragma critTer:3:../test/resources/functionCommentParam.c:
int func (int n, int a, int b, int c, int d, int e, int f, int g) {
#pragma critTer:4:../test/resources/functionCommentParam.c:
  int i;
#pragma critTer:5:../test/resources/functionCommentParam.c:
  int x = 0;
#pragma critTer:6:../test/resources/functionCommentParam.c:
  int s = 0;;
#pragma critTer:7:../test/resources/functionCommentParam.c:
  
#pragma critTer:8:../test/resources/functionCommentParam.c:
  for (i = 0; i < n; i++) {
#pragma critTer:9:../test/resources/functionCommentParam.c:
  	x += n;
#pragma critTer:10:../test/resources/functionCommentParam.c:
  	y *= n;
#pragma critTer:11:../test/resources/functionCommentParam.c:
  }
#pragma critTer:12:../test/resources/functionCommentParam.c:
  
#pragma critTer:13:../test/resources/functionCommentParam.c:
  if (x < 100)
#pragma critTer:14:../test/resources/functionCommentParam.c:
  	x = 100;
#pragma critTer:15:../test/resources/functionCommentParam.c:
  	
#pragma critTer:16:../test/resources/functionCommentParam.c:
  if (x == y)
#pragma critTer:17:../test/resources/functionCommentParam.c:
  	return y;
#pragma critTer:18:../test/resources/functionCommentParam.c:
  
#pragma critTer:19:../test/resources/functionCommentParam.c:
  if (y < 100)
#pragma critTer:20:../test/resources/functionCommentParam.c:
  	y = 100;
#pragma critTer:21:../test/resources/functionCommentParam.c:
  	
#pragma critTer:22:../test/resources/functionCommentParam.c:
  for (i = 0; i < y; i++)
#pragma critTer:23:../test/resources/functionCommentParam.c:
	{}
#pragma critTer:24:../test/resources/functionCommentParam.c:
  	
#pragma critTer:25:../test/resources/functionCommentParam.c:
  if (x < y)
#pragma critTer:26:../test/resources/functionCommentParam.c:
  	return y;
#pragma critTer:27:../test/resources/functionCommentParam.c:
  
#pragma critTer:28:../test/resources/functionCommentParam.c:
  return x;
#pragma critTer:29:../test/resources/functionCommentParam.c:
}
