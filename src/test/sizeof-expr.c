

struct s {
    int x;
    int y;
    int z;
};

union u {
    char c;
    short s;
    int i;
    long l;
    int* p;
    int a[4];
};

typedef struct s mytype;

int
main(int argc, char **argv)
{
    char c;
    short s;
    int i;
    long l;
    int *p;
    int a[2];
    struct s st;
    union u u;
    mytype m;

    printf("%ld", sizeof c);
    printf(";%ld", sizeof s);
    printf(";%ld", sizeof i);
    printf(";%ld", sizeof l);
    printf(";%ld", sizeof p);
    printf(";%ld", sizeof a);
    printf(";%ld", sizeof st);
    printf(";%ld", sizeof u);
    printf(";%ld", sizeof m);


    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}
