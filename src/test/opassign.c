
int
main(int argc, char **argv)
{
    int i = 3;

    printf("%d", i);
    i += 1;
    printf(";%d", i);
    i -= 1;
    printf(";%d", i);
    i *= 4;
    printf(";%d", i);
    i /= 3;
    printf(";%d", i);
    i %= 3;
    printf(";%d", i);
    i &= 7;
    printf(";%d", i);
    i |= 6;
    printf(";%d", i);
    i ^= 2;
    printf(";%d", i);
    i >>= 2;
    printf(";%d", i);
    i <<= 2;
    printf(";%d", i);


    {
        char *string = "Hello, World!";
        char *p;

        p = string;
        p += 1;
        printf(";%c", *p);

        p -= 1;
        printf(";%c", *p);
    }


    {
        int[4] xs;
        int* p;

        xs[0] = 75;
        xs[1] = 76;
        xs[2] = 77;
        xs[3] = 78;

        p = xs;
        p += 1;
        printf(";%d", *p);

        p -= 1;
        printf(";%d", *p);
    }

    {
        int x = 0;
        int *p = &x;

        *p += 1;
        printf(";%d", *p);

        p[0] += 2;
        printf(";%d", *p);

        *&*p += 3;
        printf(";%d", *p);
    }

    {
        int[2] a;

        a[0] = 77;
        a[1] = 78;

        a[0] += 5;
        printf(";%d", a[0]);

        *(a + 1) += 3;
        printf(";%d", a[1]);
    }

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}
