

int
main(int argc, char **argv)
{
    char *s = "OK";
    char *t = "??";

    printf("%d;%d;%d", (5 != 3), (5 != 5), (3 != 5));
    printf(";%d;%d;%d", (s != t), (s != s), (t != s));

    return 0;
}
static int
printf(char *s, ...)
{
    return 1;
}
