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

export fn print(v: i8*) -> void {
	@printf("%s", v);
}

export fn println(v: i8*) -> void {
	@printf("%s\n", v);
}

export fn print(v: i8) -> void {
	@printf("%c", v);
}

export fn println(v: i8) -> void {
	@printf("%c\n", v);
}

export fn print(v: i32) -> void {
	@printf("%d", v);
}

export fn println(v: i32) -> void {
	@printf("%d\n", v);
}

export fn print(v: i64) -> void {
	@printf("%lld", v);
}

export fn println(v: i64) -> void {
	@printf("%lld\n", v);
}

export fn print(v: f32) -> void {
	@printf("%.2f", v);
}

export fn println(v: f32) -> void {
	@printf("%.2f\n", v);
}

export fn print(v: f64) -> void {
	@printf("%.2f", v);
}

export fn println(v: f64) -> void {
	@printf("%.2f\n", v);
}

export fn print(v: bool) -> void {
	if v {
		@printf("true");
	} else {
		@printf("false");
	}
}

export fn println(v: bool) -> void {
	if v {
		@printf("true\n");
	} else {
		@printf("false\n");
	}
}