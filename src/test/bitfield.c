extern int printf(char * format, ...);


struct s{
    int b;
    unsigned b0 : (10 + 9) * 1;
    unsigned b1: 16;
    unsigned b2 : 10;
    unsigned b3 : 16;
};

int
main(int argc, char **argv)
{
    struct s ss;
    ss.b0 = 0;
    printf("ss.b0 value is %u", ss.b0);
    return 0;
}
