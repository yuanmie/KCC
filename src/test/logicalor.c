

int
main(int argc, char **argv)
{
    printf("%d;%d;%d;%d", 0||0, 1||0, 0||1, 1||2);
    printf(";%s", 0 || "OK");

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}