export let PI: f64 = 3.14159265;
export let E: f64 = 2.71828182;

export enum MathErrors -> {
	NO_ERROR,
	INVALID_INPUT
}

export struct math_error -> {
	isError: bool,
	code: MathErrors,
	message: i8*
}

export enum TrigOptions -> {
	RADIANS,
	DEGREES
}

// In radians by default
export fn sin(val: f64) -> f64 {
	return @sin(val);
}

export fn sin(val: f64, opt: TrigOptions) -> f64 {
	if opt == TrigOptions.RADIANS {
		return sin(val);
	}	
	return sin(val * (PI / 180));
}

// In radians by default
export fn cos(val: f64) -> f64 {
	return @cos(val);
}

export fn cos(val: f64, opt: TrigOptions) -> f64 {
	if opt == TrigOptions.RADIANS {
		return cos(val);
	}	
	return cos(val * (PI / 180));
}

export fn tan(val: f64) -> (f64, math_error) {
	if is_tan_undefined(val){
		return (0, math_error { true, MathErrors.INVALID_INPUT, "tan(PI / 2) is undefined" });
	}
	return (sin(val) / cos(val), math_error { false, MathErrors.NO_ERROR, "" });
}

export fn tan(val: f64, opt: TrigOptions) -> (f64, math_error) {
	if opt == TrigOptions.RADIANS {
		let v = tan(val);
		return v;
	}	
	let v = tan(val * (PI / 180));
	return v;
}

fn is_tan_undefined(val: f64) -> bool {
	let mut normalized_angle = fmod(val, 2.0 * PI);
	if normalized_angle < 0 {
		normalized_angle += 2.0 * PI;
	}
	let tolerance = 0.0000001;
	return fabs(fmod(normalized_angle, PI) - (PI / 2)) < tolerance;
}

export fn power(base: f64, exponent: f64) -> f64 {
	return @pow(base, exponent);
}

export fn fmod(val: f64, divisor: f64) -> f64 {
	return @fmod(val, divisor);
}

export fn fabs(val: f64) -> f64 {
	return @fabs(val);
}
