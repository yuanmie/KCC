extern int printf(char * format, ...);
extern char b;
extern int printf2(char * format, ...);
extern int a;

int
main(int argc, char **argv)
{
       float a = 1.5;
       float b = 3.2;
       float* p = &a;

       *p = b + b;
       a = f(a+b, b);
        printf("%d\n", a);

       return 0;
}


float f(float a, float b){
   return a + b;
}




