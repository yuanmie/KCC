

int
main(int argc, char **argv)
{
    unsigned short i = -1;
    
    i <<= 1;
    printf("%hu", i);
    i <<= 13;
    printf(";%hu", i);
    i <<= 1;
    printf(";%hu", i);
    i <<= 1;
    printf(";%hu", i);

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}