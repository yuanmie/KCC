

static int g = 8;
static int c;

int
main(int argc, char **argv)
{
    int i = 1;
    int j;

    printf("%d", 1 - 0);
    printf(";%d", 3 - i);
    i = 4;
    j = 1;
    printf(";%d", i - j);
    printf(";%d", g - i);
    i = 13;
    printf(";%d", i - g);
    printf(";%d", f(7));
    printf(";%d", f(9) - 1);
    i = 1;
    printf(";%d", f(10) - i);
    c = 2;
    printf(";%d", f(12) - c);
    i = 11;
    printf(";%d", f(i));
    i = 13;
    j = 1;
    printf(";%d", f(i) - j);
    i = 15;
    j = 4;
    printf(";%d", i - f(j));
    i = 25;
    j = 12;
    printf(";%d", f(i) - f(j));

    return 0;
}

int
f(int i)
{
    return i - 1;
}

static int
printf(char *s, ...)
{
    return 1;
}
