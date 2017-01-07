

union nums {
    unsigned char cs[8];
    unsigned short s;
    unsigned int i;
    unsigned long l;
};

int
main(int argc, char **argv)
{
    union nums u;

    u.cs[0] = (unsigned char)1;
    u.cs[1] = (unsigned char)2;
    printf("%hhd;%hhd;%hd\n", u.cs[0], u.cs[1], u.s);

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}