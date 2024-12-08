import std;

// Basic C-Like string functions

export fn str_len(mut v: i8*) -> i64 {
	let mut s: i8*;
	let mut count = 0;
	for s = v; *s != '\0'; s += 1 {
		count += 1;
	}
	return count;
}

export fn str_equal(mut a: i8*, mut b: i8*) -> bool {
	let mut s1: i8* = a;
	let mut s2: i8* = b;
	while *s1 != '\0' && *s2 != '\0' {
		if *s1 != *s2 {
			return false;
		}
		s1 += 1;
		s2 += 1;
	}
	return *s1 == *s2;
}

export fn str_copy(mut src: i8*, mut dest: i8*) -> void {
	let mut s: i8* = src;
	let mut d: i8* = dest;
	while *s != '\0' {
		*d = *s;
		s += 1;
		d += 1;
	}
	*d = '\0';
}

export fn str_contains_substring(mut s: i8*, mut v: i8*) -> bool {
	let len: i64 = str_len(v);
	let mut s1: i8* = s;
	while *s1 != '\0' {
		s1 += 1;
		let res = @memcmp(s1, v, len);
		if res == 0 {
			return true;
		}
	}
	return false;
}

// String struct and associated functions

export enum StrErrors -> {
    MEMORY_ERROR,
    NO_ERROR
}

export struct str_error -> {
    isError: bool,
    code: StrErrors,
    message: i8*
}

export struct str -> {
    mut s: i8*,
    mut len: i64,
    mut cap: i64
}

let STR_CAP: i64 = 1024;

export fn str_free(mut v: str*) -> void {
	std::free(v->s);
	std::free(v);
}

export fn new_str(start: i8*) -> (str*, str_error) {
    let mut new_str: str* = std::malloc(size(str));
    if new_str == null {
        return (new_str, str_error { true, StrErrors.MEMORY_ERROR, "Memory error" });
    }
	new_str->s = std::malloc(str_len(start) + 1);
	if new_str->s == null {
		return (new_str, str_error { true, StrErrors.MEMORY_ERROR, "Memory error" });
	}
	str_copy(start, new_str->s);
	new_str->len = str_len(start);
	new_str->cap = STR_CAP;
	return (new_str, str_error { false, StrErrors.NO_ERROR, "No error" });
} 

export fn string_empty(v: str*) -> bool {
	return v->len == 0;
}

export fn string_contains(v: str*, s: str*) -> bool {
	return str_contains_substring(v->s, s->s);
}

export fn string_contains(v: str*, s: i8*) -> bool {
	return str_contains_substring(v->s, s);
}

export fn print(v: str*) -> void {
	@printf("%s", v->s);
}

export fn println(v: str*) -> void {
	@printf("%s\n", v->s);
}