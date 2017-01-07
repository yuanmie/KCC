extern int printf(char * format, ...);
enum e{E, F = 2, G = 5, H = G + 1, J = G + F};


int
main(int argc, char **argv)
{
   int a = E;
   enum e b = E;
   enum e *aaa = &b;
    switch(a){
        case E:
           printf("%d\n", a);
            break;
        case F:
         printf("%d\n", a);
        break;
    }
    return 0;
}
