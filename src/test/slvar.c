

int
main(int argc, char **argv)
{
    static int static_variable = 1;
    static char *static_string = "OK";

    printf("%d", static_variable);
    static_variable = 2;
    printf(";%d", static_variable);
    printf(";%s", static_string);
    static_string = "NEW";
    printf(";%s", static_string);

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}