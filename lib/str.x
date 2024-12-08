import std;

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

// export enum StrErrors -> {
//     MEMORY_ERROR,
//     NO_ERROR
// }

// export struct str_error -> {
//     isError: bool,
//     code: StrErrors,
//     message: i8*
// }

// export struct str -> {
//     s: i8*,
//     len: i64,
//     cap: i64
// }

// export fn str_len(v: i8*) -> i64 {
//     let mut len: i64 = 0;
// }

// export fn new_str(start: i8*) -> (str*, str_error) {
//     let mut new_str: str* = std::malloc(size(str));
//     if new_str == null {
//         return (null, str_error { true, StrErrors.MEMORY_ERROR, "Memory error" });
//     }

// } 