

int
main(int argc, char **argv)
{
    printf("%d;%d;%d", !0, !1, !2);
    printf(";%d", !"str");
    printf(";%d", !0);

    return 0;
}
static int
printf(char *s, ...)
{
    return 1;
}
