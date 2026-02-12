package com.leawsic.autoupdate.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class HashCodeGenerator {
    public static String getSha1FromFile(File file) throws IOException {
        return DigestUtils.sha1Hex(new FileInputStream(file));
    }
    public static String getSha1FromPath(Path path) throws IOException {
        return DigestUtils.sha1Hex(Files.newInputStream(path, StandardOpenOption.READ));
    }
}
