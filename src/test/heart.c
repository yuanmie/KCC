extern int printf(char *format, ...);

int main() {
    float x;
    float y;
    x = 0.0f;
    float a, b;
    for ( y = 1.5f; y > -1.5f; y -= 0.1f) {
        for ( x = -1.5f; x < 1.5f; x += 0.05f) {
             a = x * x + y * y - 1.0;
             b = a * a * a - x * x * y * y * y ;
             printf("%c",b <= 0.0 ? '*' : ' ');
        }
       printf("\n");
    }
}
