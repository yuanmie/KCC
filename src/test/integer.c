

int
main(int argc, char **argv)
{
    printf("%d;%d;%d", 0, 00, 0x0);
    printf(";%d;%d;%d", 1, 01, 0x1);
    printf(";%d;%d;%d", 9, 011, 0x9);
    printf(";%d;%d;%d", 17, 021, 0x11);

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}