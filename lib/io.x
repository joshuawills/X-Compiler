export enum IOErrors -> {
	MEMORY_ERROR,
	NUMBER_ERROR,
	IO_ERROR,
	NO_ERROR
}

export struct io_err -> {
	isError: bool, 
	code: IOErrors,
	message: i8*
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

export fn print(v: u8) -> void {
	@printf("%hhu", v);
}

export fn println(v: u8) -> void {
	@printf("%hhu\n", v);
}

export fn print(v: u32) -> void {
	@printf("%u", v);
}

export fn println(v: u32) -> void {
	@printf("%u\n", v);
}

export fn print(v: u64) -> void {
	@printf("%llu", v);
}

export fn println(v: u64) -> void {
	@printf("%llu\n", v);
}

export fn printaddr(v: void*) -> void {
	@printf("%p", v);
}

export fn printaddrln(v: void*) -> void {
	@printf("%p\n", v);
}

export fn read_i64() -> i64 {
	let mut v: i64;
	@__isoc99_scanf("%lld", &v);
	@getchar();
	return v;
}

export fn read_f64() -> f64 {
	let mut v: f64;
	@__isoc99_scanf("%lf", &v);
	@getchar();
	return v;
}

export fn read_i32() -> i32 {
	let mut v: i32;
	@__isoc99_scanf("%d", &v);
	@getchar();
	return v;
}

export fn read_char() -> i8 {
	let mut v: i8;
	@__isoc99_scanf("%c", &v);
	@getchar();
	return v;
}

export fn read_str(v: i8*, l: i64) -> io_err {
	let res = @fgets(v, l as i32, @stdin);
	if res == null {
		return io_err { true, IOErrors.IO_ERROR, "IO error" };
	}
	return io_err { false, IOErrors.NO_ERROR, "" };
}
