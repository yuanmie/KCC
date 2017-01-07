

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
    printf("%ld", sizeof(char));
    printf(";%ld", sizeof(unsigned char));
    printf(";%ld", sizeof(short));
    printf(";%ld", sizeof(unsigned short));
    printf(";%ld", sizeof(int));
    printf(";%ld", sizeof(unsigned int));
    printf(";%ld", sizeof(long));
    printf(";%ld", sizeof(unsigned long));
    printf(";%ld", sizeof(int*));
    printf(";%ld", sizeof(int**));
    printf(";%ld", sizeof(int[]));
    printf(";%ld", sizeof(int[4]));
    printf(";%ld", sizeof(struct s));
    printf(";%ld", sizeof(union u));
    printf(";%ld", sizeof(mytype));


    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}
