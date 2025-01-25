extern fn memcmp(i8*, i8*, i64) -> i64;

extern fn printf(i8*, ...) -> i64;
extern fn fgets(i8*, i32, void*) -> i8*;
extern fn getchar() -> i32;
extern fn __isoc99_scanf(i8*, ...) -> i32;

extern let stdin: void*;

extern fn malloc(i64) -> void*;
extern fn calloc(i64, i64) -> void*;
extern fn free(void*) -> void;
extern fn realloc(void*, i64) -> void*;
extern fn exit(i64) -> void;

extern fn sin(f64) -> f64;
extern fn cos(f64) -> f64;
extern fn pow(f64, f64) -> f64;
extern fn fmod(f64, f64) -> f64;
extern fn fabs(f64) -> f64;

extern fn srand(i32) -> void;
extern fn rand() -> i32;

extern fn time(void*) -> i64;

extern fn fopen(i8*, i8*) -> void*;
extern fn fseek(void*, i64, i32) -> i32;
extern fn ftell(void*) -> i64;
extern fn fread(void*, i64, i64, void*) -> i64;
extern fn fclose(void*) -> i32;
extern fn rewind(void*) -> void;
extern fn fwrite(i8*, i64, i64, void*) -> i64;

extern fn stat(i8*, void*) -> i32;