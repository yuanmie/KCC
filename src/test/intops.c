

int
main(int argc, char **argv)
{
    int i = 1;
    
    i <<= 1;
    printf("%d", i);
    i <<= 29;
    printf(";%d", i);
    i <<= 1;
    printf(";%d", i);
    i <<= 1;
    printf(";%d", i);

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}