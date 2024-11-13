export struct Vec2 -> {
    mut x: int,
    mut y: int
}

export enum Boolean -> {
	TRUE, FALSE
}

export let Zero: int = 0;

export fn add(x: int, y: int) -> int {
    return x + y;
}

export fn log_vec2(val: Vec2) -> void {
    outInt(val.x);
    outInt(val.y);
}
