using str;

export enum ConvErrors -> {
    INVALID_CHAR,
    INPUT_ERROR,
    NO_ERROR,
    MEMORY_ERROR
}

export struct conv_err -> {
    isError: bool,
    code: ConvErrors,
    message: i8*
}

export fn (mut v: i64) to_str() -> (str*, conv_err) {

    let mut is_negative: bool = false;
    let mut s, err = Str("");
    if err.isError {
        return (s, conv_err { true, ConvErrors.MEMORY_ERROR, "Memory error" });
    }

    if v == 0 {
        s.push("0");
        return (s, conv_err { false, ConvErrors.NO_ERROR, "No error" });
    }

    if v < 0 {
        is_negative = true;
        v = -v;
    }

    while v > 0 {
		let b = ((v % 10) + '0') as i8;
		s.push(b);
        v /= 10;
    }
	
	if is_negative {
		s.push("-");
	}

    s.reverse();
    return (s, conv_err { false, ConvErrors.NO_ERROR, "No error" });
}

export fn (mut v: i8*) to_i64() -> (i64, conv_err) {
    let mut res: i64 = 0;
    let mut is_negative: bool = false;

    while (*v == ' ') {
        v += 1;
    }

    if (*v == '-') {
        is_negative = true;
        v += 1;
    } else if (*v == '+') {
        v += 1;
    }

    while *v >= '0' && *v <= '9' {
        res = res * 10 + (*v - '0');
        v += 1;
    }

    if *v != '\0' {
        return (0, conv_err { true, ConvErrors.INVALID_CHAR, "Invalid character in input" });
    }

    if is_negative {
        res = -res;
    }

    return (res, conv_err { false, ConvErrors.NO_ERROR, "No error" });
}