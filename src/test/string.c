

int
main(int argc, char **argv)
{
    printf("");
    printf(";");
    printf(";a");
    printf(";aa;b");
    printf(";\"");
    printf(";\'");
    printf(";\a\b\e\f\n\r\t\v");
    printf(";\101\102\103\141\142\143");

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}