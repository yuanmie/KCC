

int
main(int argc, char **argv)
{
    unsigned char i = -1;
    
    i <<= 1;
    printf("%hhu", i);
    i <<= 5;
    printf(";%hhu", i);
    i <<= 1;
    printf(";%hhu", i);
    i <<= 1;
    printf(";%hhu", i);

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}