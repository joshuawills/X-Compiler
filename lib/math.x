export let PI: f64 = 3.14159265;
export let E: f64 = 2.71828182;

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

export fn power(base: f64, exponent: f64) -> f64 {
	return @pow(base, exponent);
}