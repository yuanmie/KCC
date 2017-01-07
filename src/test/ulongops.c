

int
main(int argc, char **argv)
{
    unsigned long i = 1;
    
    printf("%lu", i);
    i <<= 1;
    printf(";%lu", i);
    i <<= 61;
    printf(";%lu", i);
    i <<= 1;
    printf(";%lu", i);
    i <<= 1;
    printf(";%lu", i);

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}
