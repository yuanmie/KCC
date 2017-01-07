

int
main(int argc, char **argv)
{
    int i;
    int *ptr;

    ptr = &i;
    *ptr = 5;
    printf("%d;%d\n", i, *ptr);
    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}
