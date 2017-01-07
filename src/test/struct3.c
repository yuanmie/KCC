

struct st {
    int x[4];
};

int
main(void)
{
    struct st s;
    s.x[1] = 7;
    printf("%d\n", s.x[1]);
    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}