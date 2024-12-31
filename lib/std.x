using libc;

export let I8_MIN: i8 = -128;
export let I8_MAX: i8 = 127;

export let I32_MIN: i32 = -2147483648;
export let I32_MAX: i32 = 2147483647;

export let I64_MIN: i64 = -9223372036854775808;
export let I64_MAX: i64 = 9223372036854775807;

export let U8_MIN: u8 = 0;
export let U8_MAX: u8 = 255;

export let U32_MIN: u32 = 0;
export let U32_MAX: u32 = 4294967295;

export let U64_MIN: u64 = 0;
export let U64_MAX: u64 = 18446744073709551615;

export enum StdErrors -> {
    MEMORY_ERROR,
    NO_ERROR
}

export fn malloc(v: i64) -> void* {
	return @malloc(v);
}

export fn calloc(n: i64, size_of: i64) -> void* {
	return @calloc(n, size_of);
}

export fn free(v: void*) -> void {
	@free(v);
}

export fn realloc(v: void*, size_of: i64) -> void* {
	return @realloc(v, size_of);
}

export fn exit(v: i64) -> void {
	@exit(v);
}
