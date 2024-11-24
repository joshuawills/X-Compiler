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