export struct Vec2 -> {
    mut x: i64,
    mut y: i64
}

export enum Boolean -> {
	TRUE, FALSE
}

export let Zero: i64 = 0;

export fn add(x: i64, y: i64) -> i64 {
    return x + y;
}

export fn log_vec2(val: Vec2) -> void {
    outI64(val.x);
    outI64(val.y);
}
