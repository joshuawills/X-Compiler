export fn malloc(v: i64) -> void* {
	return @malloc(v);
}

export fn free(v: void*) -> void {
	@free(v);
}
