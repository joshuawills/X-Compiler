import std;

export fn str_len(mut v: i8*) -> i64 {
	let mut s: i8*;
	let mut count = 0;
	for s = v; *s != '\0'; s += 1 {
		count += 1;
	}
	return count;
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