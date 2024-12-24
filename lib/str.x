import std, io;

// Basic C-Like string functions

export fn (mut v: i8*) len() -> i64 {
	let mut s: i8*;
	let mut count = 0;
	for s = v; *s != '\0'; s += 1 {
		count += 1;
	}
	return count;
}

export fn (mut a: i8*) equal(mut b: i8*) -> bool {
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

export fn (mut src: i8*) copy(mut dest: i8*) -> void {
	let mut s: i8* = src;
	let mut d: i8* = dest;
	while *s != '\0' {
		*d = *s;
		s += 1;
		d += 1;
	}
	*d = '\0';
}

export fn (mut s: i8*) contains(mut v: i8*) -> bool {
	let len: i64 = v.len();
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

export fn (mut s: i8*) is_upper() -> bool {
	let mut c: i8* = s;
	while *c != '\0' {
		let is_uppercase = *c >= 'A' && *c <= 'Z';
		if !is_uppercase {
			return false;
		}
		c += 1;
	}
	return true;
}

export fn (mut s: i8*) is_lower() -> bool {
	let mut c: i8* = s;
	while *c != '\0' {
		let is_lowercase = *c >= 'a' && *c <= 'z';
		if !is_lowercase {
			return false;
		}
		c += 1;
	}
	return true;
}

export fn (mut s: i8*) is_alpha() -> bool {
	let mut c: i8* = s;
	while *c != '\0' {
		let is_lowercase = *c >= 'a' && *c <= 'z';
		let is_uppercase = *c >= 'A' && *c <= 'Z';
		if !is_lowercase && !is_uppercase {
			return false;
		}
		c += 1;
	}
	return true;
}

export fn (mut s: i8*) is_digit() -> bool {
	let mut c: i8* = s;
	while *c != '\0' {
		let is_digit = *c >= '0' && *c <= '9';
		if !is_digit {
			return false;
		}
		c += 1;
	}
	return true;
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

export fn (mut v: str*) free() -> void {
	std::free(v->s);
	std::free(v);
}

export fn Str(mut start: i8*) -> (str*, str_error) {
    let mut new_str: str* = std::malloc(size(str));
    if new_str == null {
        return (new_str, str_error { true, StrErrors.MEMORY_ERROR, "Memory error" });
    }
	new_str->s = std::malloc(start.len() + 1);
	if new_str->s == null {
		return (new_str, str_error { true, StrErrors.MEMORY_ERROR, "Memory error" });
	}
	start.copy(new_str->s);
	new_str->len = start.len();
	new_str->cap = STR_CAP;
	return (new_str, str_error { false, StrErrors.NO_ERROR, "No error" });
} 

export fn (v: str*) empty() -> bool {
	return v->len == 0;
}

export fn (v: str*) contains(s: str*) -> bool {
	return (v->s).contains(s->s);
}

export fn (v: str*) contains(s: i8*) -> bool {
	return (v->s).contains(s);
}

export fn (v: str*) len() -> i64 {
	return v->len;
}

export fn (v: str*) equal(s: str*) -> bool {
	return (v->s).equal(s->s);
}

export fn (mut v: str*) is_alpha() -> bool {
	return (v->s).is_alpha();
}

export fn (mut v: str*) is_digit() -> bool {
	return (v->s).is_digit();
}

export fn (mut v: str*) is_upper() -> bool {
	return (v->s).is_upper();
}

export fn (mut v: str*) is_lower() -> bool {
	return (v->s).is_lower();
}

export fn (mut v: str*) push(mut s: i8*) -> str_error {
	let mut new_len = v.len() + s.len();
	if new_len >= v->cap {
		let mut new_cap = v->cap * 2;
		let mut new_s: i8* = std::malloc(new_cap);
		if new_s == null {
			return str_error { true, StrErrors.MEMORY_ERROR, "Memory error" };
		}
		(v->s).copy(new_s);
		std::free(v->s);
		v->s = new_s;
		v->cap = new_cap;
	}
	let mut addr: i8* = v->s + v->len;
	s.copy(addr);
	v->len = new_len;
	return str_error { false, StrErrors.NO_ERROR, "No error" };
}

export fn (mut v: str*) push(mut s: i8) -> str_error {
	let mut new_len = v.len() + 1;
	if new_len >= v->cap {
		let mut new_cap = v->cap * 2;
		let mut new_s: i8* = std::malloc(new_cap);
		if new_s == null {
			return str_error { true, StrErrors.MEMORY_ERROR, "Memory error" };
		}
		(v->s).copy(new_s);
		std::free(v->s);
		v->s = new_s;
		v->cap = new_cap;
	}
	let mut addr: i8* = v->s + v->len;
	*addr = s;
	v->len = new_len;
	return str_error { false, StrErrors.NO_ERROR, "No error" };
}

export fn (mut v: str*) reverse() -> void {
	let mut start: i8* = v->s;
	let mut end: i8* = v->s + v->len - 1;
	while (start < end) {
		let mut temp: i8 = *start;
		*start = *end;
		*end = temp;
		start += 1;
		end -= 1;
	}
}

export fn print(v: str*) -> void {
	@printf("%s", v->s);
}

export fn println(v: str*) -> void {
	@printf("%s\n", v->s);
}