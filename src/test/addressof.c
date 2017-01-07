struct s {
        char[8] str;
};

int
main(int argc, char** argv)
{
        struct s s;
        struct s * sp;
        char *str;

        str = s.str;

        str = &s.str;
        sp = &s;
        str = sp->str;

        sp = &s;
        str = &sp->str;
        return 0;
}
