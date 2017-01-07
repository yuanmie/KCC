extern int printf(char * format, ...);
struct ss{
    int a;
    int b;
};

struct ss returnstruct(void){
    struct ss s1 = {1, 2};
    return s1;
}

int
main(int argc, char **argv)
{
    struct ss s = returnstruct();
    printf("%f\n", s.b);
    return 0;
}


