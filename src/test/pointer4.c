

int
main(int argc, char** argv)
{
    int i = 777;
    printf("%d\n", *&i);
    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}
