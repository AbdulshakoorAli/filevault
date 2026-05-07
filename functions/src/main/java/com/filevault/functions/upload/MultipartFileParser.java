package com.filevault.functions.upload;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Minimal multipart/form-data parser for a single {@code file} field (browser upload).
 */
public final class MultipartFileParser {

    public record ParsedFile(String filename, String contentType, byte[] data) {
    }

    private MultipartFileParser() {
    }

    public static ParsedFile parse(byte[] body, String contentTypeHeader) {
        if (contentTypeHeader == null || !contentTypeHeader.toLowerCase(Locale.ROOT).contains("multipart/form-data")) {
            throw new IllegalArgumentException("Expected multipart/form-data");
        }
        String boundary = extractBoundary(contentTypeHeader);
        byte[] delimiter = ("--" + boundary).getBytes(StandardCharsets.ISO_8859_1);
        int pos = indexOf(body, delimiter, 0);
        if (pos < 0) {
            throw new IllegalArgumentException("Invalid multipart body");
        }
        pos += delimiter.length;
        while (pos < body.length) {
            if (pos + 2 <= body.length && body[pos] == '\r' && body[pos + 1] == '\n') {
                pos += 2;
            }
            if (pos + 2 <= body.length && body[pos] == '-' && body[pos + 1] == '-') {
                break;
            }
            int headerEnd = indexOf(body, new byte[]{'\r', '\n', '\r', '\n'}, pos);
            if (headerEnd < 0) {
                break;
            }
            String headers = new String(body, pos, headerEnd - pos, StandardCharsets.ISO_8859_1);
            pos = headerEnd + 4;
            int next = indexOf(body, delimiter, pos);
            if (next < 0) {
                next = body.length;
            }
            int partBodyEnd = next;
            if (partBodyEnd >= 2 && body[partBodyEnd - 1] == '\n' && body[partBodyEnd - 2] == '\r') {
                partBodyEnd -= 2;
            }
            if (headers.contains("name=\"file\"") || headers.contains("name='file'")) {
                String filename = extractFilename(headers);
                String partContentType = extractHeaderValue(headers, "Content-Type:");
                int len = Math.max(0, partBodyEnd - pos);
                byte[] data = new byte[len];
                System.arraycopy(body, pos, data, 0, len);
                return new ParsedFile(filename, partContentType != null ? partContentType : "application/octet-stream", data);
            }
            pos = next + delimiter.length;
        }
        throw new IllegalArgumentException("No file part found");
    }

    private static String extractBoundary(String contentTypeHeader) {
        String lower = contentTypeHeader.toLowerCase(Locale.ROOT);
        int idx = lower.indexOf("boundary=");
        if (idx < 0) {
            throw new IllegalArgumentException("Missing boundary");
        }
        int start = idx + "boundary=".length();
        while (start < contentTypeHeader.length() && (contentTypeHeader.charAt(start) == ' ' || contentTypeHeader.charAt(start) == '"')) {
            start++;
        }
        int end = start;
        while (end < contentTypeHeader.length()) {
            char c = contentTypeHeader.charAt(end);
            if (c == '"' || c == ';' || c == ' ' || c == '\r' || c == '\n') {
                break;
            }
            end++;
        }
        if (end <= start) {
            throw new IllegalArgumentException("Empty boundary");
        }
        return contentTypeHeader.substring(start, end);
    }

    private static String extractFilename(String headers) {
        String lower = headers.toLowerCase(Locale.ROOT);
        int fn = lower.indexOf("filename=");
        if (fn < 0) {
            return "upload";
        }
        int start = fn + "filename=".length();
        while (start < headers.length() && (headers.charAt(start) == ' ' || headers.charAt(start) == '"')) {
            start++;
        }
        int end = start;
        while (end < headers.length()) {
            char c = headers.charAt(end);
            if (c == '"' || c == '\r' || c == '\n') {
                break;
            }
            end++;
        }
        return headers.substring(start, end).trim();
    }

    private static String extractHeaderValue(String headers, String name) {
        int idx = headers.toLowerCase(Locale.ROOT).indexOf(name.toLowerCase(Locale.ROOT));
        if (idx < 0) {
            return null;
        }
        int start = idx + name.length();
        while (start < headers.length() && (headers.charAt(start) == ' ' || headers.charAt(start) == '\t')) {
            start++;
        }
        int end = start;
        while (end < headers.length() && headers.charAt(end) != '\r' && headers.charAt(end) != '\n') {
            end++;
        }
        String v = headers.substring(start, end).trim();
        return v.isEmpty() ? null : v;
    }

    private static int indexOf(byte[] haystack, byte[] needle, int from) {
        outer:
        for (int i = from; i + needle.length <= haystack.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
