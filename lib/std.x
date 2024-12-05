export enum StdErrors -> {
	MEMORY_ERROR,
	NUMBER_ERROR
}

export struct error -> {
	isError: bool, 
	code: StdErrors,
	message: i8*
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

export fn outBool(v: bool) -> void {
	if v {
		outStr("true\n");
	} else {
		outStr("false\n");
	}
}