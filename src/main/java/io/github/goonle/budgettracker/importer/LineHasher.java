package io.github.goonle.budgettracker.importer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

/** Turns a raw CSV line into the fingerprint used for the duplicate check. */
public final class LineHasher {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private LineHasher() {
        // static helpers only
    }

    /** Trims the line and collapses every run of whitespace (spaces, tabs, \r) into one space. */
    public static String normalize(String line) {
        return WHITESPACE.matcher(line.strip()).replaceAll(" ");
    }

    /** SHA-256 of the normalized line, as 64 lowercase hex characters. */
    public static String hash(String line) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha256.digest(normalize(line).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // every Java runtime must support SHA-256, so this never happens in practice
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
