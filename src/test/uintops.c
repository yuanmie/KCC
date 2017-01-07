

int
main(int argc, char **argv)
{
    unsigned int i = 1;
    
    i <<= 1;
    printf("%u", i);
    i <<= 29;
    printf(";%u", i);
    i <<= 1;
    printf(";%u", i);
    i <<= 1;
    printf(";%u", i);

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}