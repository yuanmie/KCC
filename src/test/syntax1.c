
char c;
short s;
int ii;
long l;

unsigned char uc;
unsigned short us;
unsigned int ui;
unsigned long ul;

int* p;
int** pp;
int*** ppp;
int**** pppp;
int***** ppppp;

int a[1];
int aa[1][1];
int aaa[1][1][1];

int* pa[1];
int* ap[1];
int** ppa[1];
int** pap[1];
int** app[1];
int* aap[1][1];
int* apa[1][1];
int* paa[1][1];

struct file {
    int fd;
    char *path;
};

struct ifnode {
    union node *cond;
    union node *then_body;
    union node *else_body;
};

struct stmt {
    union node *expr;
};

union node {
    struct ifnode ifnode;
    struct stmt stmt;
};

typedef int myint;

int
main(int argc, char** argv)
{
    ff();
    return f(1, 2, 3);
}

void ff(void) { return; }

int
f(int i, int j, int k)
{
    i++;
    ++i;
    i--;
    --i;
    printf("%d, %d, %d\n", 1, *(int*)&j, (int)h());
    return g(g(i, j % 18), 3 + 5 * 7);
}

int g(int i, int j) {
     return i * h() * *(myint*)&j  ; }

myint h(void) { return 0; }

static int
printf(char *s, ...)
{
    return 1;
}