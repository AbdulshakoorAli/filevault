package com.filevault.functions.upload;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultipartFileParserTest {

    @Test
    void parseExtractsFileFieldAndBytes() {
        String boundary = "----testBoundary";
        String partHeaders = """
                Content-Disposition: form-data; name="file"; filename="hello.txt"\r
                Content-Type: text/plain\r
                \r
                """.replace("\n", "\r\n");
        String bodyText = "Hello";
        byte[] body = buildMultipart(boundary, partHeaders, bodyText);

        MultipartFileParser.ParsedFile parsed = MultipartFileParser.parse(
                body,
                "multipart/form-data; boundary=" + boundary);

        assertEquals("hello.txt", parsed.filename());
        assertEquals("text/plain", parsed.contentType());
        assertArrayEquals("Hello".getBytes(StandardCharsets.UTF_8), parsed.data());
    }

    @Test
    void parseRejectsNonMultipartContentType() {
        byte[] body = "x".getBytes(StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class,
                () -> MultipartFileParser.parse(body, "text/plain"));
    }

    private static byte[] buildMultipart(String boundary, String partHeaders, String partBody) {
        String start = "--" + boundary + "\r\n" + partHeaders + "\r\n";
        String end = "\r\n--" + boundary + "--\r\n";
        byte[] a = start.getBytes(StandardCharsets.ISO_8859_1);
        byte[] b = partBody.getBytes(StandardCharsets.UTF_8);
        byte[] c = end.getBytes(StandardCharsets.ISO_8859_1);
        byte[] out = new byte[a.length + b.length + c.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        System.arraycopy(c, 0, out, a.length + b.length, c.length);
        return out;
    }
}
