using std, str, libc;

struct stat -> {
    st_dev: u64,
    st_ino: u64,
    st_mode: u32,
    st_nlink: u32,
    st_uid: u32,
    st_gid: u32,
    st_rdev: u64,
    st_size: i64,
    st_blksize: i64,
    st_blocks: i64,
    st_atime: i64,
    st_mtime: i64,
    st_ctime: i64
}


export struct File -> {
    mut name: i8*
}

export enum fileErrors -> {
    MEMORY_ERROR,
    FILE_OK,
    FILE_NOT_FOUND,
    FILE_PERMISSION_DENIED,
    FILE_ALREADY_EXISTS,
    FILE_NO_SPACE,
    FILE_UNKNOWN
}

export struct file_error -> {
    isError: bool,
    code: fileErrors,
    message: i8*
}

export fn File(mut v: i8*) -> (File*, file_error) {
    let file = malloc(size(File)) as File*;
    if file == null {
        return (file, file_error { true, fileErrors.MEMORY_ERROR, "Memory error" });
    }
    file->name = v;
    return (file, file_error { false, fileErrors.FILE_OK, "No error" });
}

export fn (mut f: File*) free() -> void {
    free(f);
}

export fn (mut f: File*) write(s: str*) -> file_error {
    let fp = @fopen(f->name, "w");
    if fp == null {
        return file_error { true, fileErrors.FILE_NOT_FOUND, "File not found" };
    }
    @fwrite(s->s, 1, s->len, fp);
    @fclose(fp);
    return file_error { false, fileErrors.FILE_OK, "No error" };
}

export fn (mut f: File*) read() -> (str*, file_error) {

    let mut s, err = Str("");
    if err.isError {
        return (s, file_error { true, fileErrors.MEMORY_ERROR, "Memory error" });
    }

    let fp = @fopen(f->name, "r");
    if fp == null {
        return (s, file_error { true, fileErrors.FILE_NOT_FOUND, "File not found" });
    }

    @fseek(fp, 0, 2);
    let size_of = @ftell(fp);
    @rewind(fp);
    let mut buffer = malloc(size_of + 1) as i8*;
    if buffer == null {
        return (s, file_error { true, fileErrors.MEMORY_ERROR, "Memory error" });
    }
    @fread(buffer, 1, size_of, fp);
    @fclose(fp);
    buffer[size_of] = '\0';

    let err = s.push(buffer);
    if err.isError {
        return (s, file_error { true, fileErrors.MEMORY_ERROR, "Memory error" });
    }

    free(buffer);
    return (s, file_error { false, fileErrors.FILE_OK, "No error" });
}
