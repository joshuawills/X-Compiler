// Array list boolean

import "../../lib/std.x" as std;

let SECTION_SIZE: i64 = 100;

struct List -> {
    mut array: bool*,
    mut current_size: i64,
    mut section_size: i64
}

fn init_list() -> List* {
    let mut list: List* = std::malloc(size(List));
    list->current_size = 0;
    list->section_size = SECTION_SIZE;
    list->array = std::calloc(list->section_size, size(bool));
    return list;
}

fn add_to_list(list: List*, val: bool) -> void {
    if list->current_size != 0 && list->current_size % list->section_size == 0 {
        let num_iterations = list->current_size / list->section_size;
        list->array = std::realloc(list->array, (num_iterations + 1) * list->section_size * size(bool)) ;
    }
    list->array[list->current_size] = val;
    list->current_size += 1;
}

fn outBool(x: bool) -> void {
    if x {
        outStr("true\n");
    } else {
        outStr("false\n");
    }
}

fn print_list(list: List*) -> void {
    loop i in list->current_size {
        outBool(list->array[i]);
    }
}

fn free_list(list: List*) -> void {
    std::free(list->array);
    std::free(list);
}

fn main() -> void {

    let mut list: List* = init_list();
    
    outStr("====\n");
    loop i in 5 {
        add_to_list(list, i % 2 == 0);
    }
    outStr("====\n");

    print_list(list);

    free_list(list);

    std::exit(0);
}