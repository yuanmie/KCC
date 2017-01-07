

int
main(int argc, char **argv)
{
    printf("%d;%d;%d", (5 < 3), (5 < 5), (3 < 5));

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}