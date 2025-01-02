using libc;

export fn seed_random(v: i32) -> void {
	@srand(v);
}

export fn get_random_int() -> i32 {
	return @rand();
}

export fn get_random_int(upper_lim: i64) -> i32 {
    return @rand() % upper_lim;
}

export fn get_random_int(lower_lim: i64, upper_lim: i64) -> i32 {
    return (@rand() + lower_lim) % upper_lim;
}
