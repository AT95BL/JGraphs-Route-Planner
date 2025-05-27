package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Ovu klasu biste morali ručno da parsirate iz JSON-a
// Bez Gson-a, to bi bilo dosta posla
// public static class TransportData { ... }
// public static class Station { ... }
// public static class Departure { ... }


public class SimpleJsonParser {

    private String jsonString;
    private int index;

    public SimpleJsonParser(String jsonString) {
        this.jsonString = jsonString;
        this.index = 0;
    }

    private void skipWhitespace() {
        while (index < jsonString.length() && Character.isWhitespace(jsonString.charAt(index))) {
            index++;
        }
    }

    private char peek() {
        skipWhitespace();
        return jsonString.charAt(index);
    }

    private char consume() {
        skipWhitespace();
        return jsonString.charAt(index++);
    }

    public Object parse() {
        char c = peek();
        if (c == '{') {
            return parseObject();
        } else if (c == '[') {
            return parseArray();
        } else if (c == '"') {
            return parseString();
        } else if (Character.isDigit(c) || c == '-') {
            return parseNumber();
        } else if (jsonString.startsWith("true", index)) {
            consume(); consume(); consume(); consume(); // "true"
            return true;
        } else if (jsonString.startsWith("false", index)) {
            consume(); consume(); consume(); consume(); consume(); // "false"
            return false;
        } else if (jsonString.startsWith("null", index)) {
            consume(); consume(); consume(); consume(); // "null"
            return null;
        } else {
            throw new RuntimeException("Unexpected character: " + c);
        }
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> object = new HashMap<>();
        consume(); // '{'
        skipWhitespace();

        while (peek() != '}') {
            String key = parseString();
            consume(); // ':'
            Object value = parse();
            object.put(key, value);

            skipWhitespace();
            if (peek() == ',') {
                consume(); // ','
            }
        }
        consume(); // '}'
        return object;
    }

    private List<Object> parseArray() {
        List<Object> array = new ArrayList<>();
        consume(); // '['
        skipWhitespace();

        while (peek() != ']') {
            array.add(parse());
            skipWhitespace();
            if (peek() == ',') {
                consume(); // ','
            }
        }
        consume(); // ']'
        return array;
    }

    private String parseString() {
        consume(); // '"'
        StringBuilder sb = new StringBuilder();
        while (peek() != '"') {
            sb.append(consume());
        }
        consume(); // '"'
        return sb.toString();
    }

    private Number parseNumber() {
        StringBuilder sb = new StringBuilder();
        while (index < jsonString.length() && (Character.isDigit(jsonString.charAt(index)) || jsonString.charAt(index) == '.' || jsonString.charAt(index) == '-')) {
            sb.append(jsonString.charAt(index++));
        }
        try {
            if (sb.toString().contains(".")) {
                return Double.parseDouble(sb.toString());
            } else {
                return Long.parseLong(sb.toString());
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format: " + sb.toString());
        }
    }

    // Main metoda za testiranje (samo za demonstraciju parsiranja generičkih JSON struktura)
    public static void main(String[] args) {
        String jsonFilePath = "transport_data.json"; // Vaš generisani fajl

        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            SimpleJsonParser parser = new SimpleJsonParser(jsonContent);
            Object parsedData = parser.parse();

            System.out.println("Parsed data (raw Java objects):");
            System.out.println(parsedData);

            // Ovdje biste morali ručno mapirati ove generičke Map i List objekte
            // u vaše TransportData, Station, Departure klase.
            // Ovo bi bilo prilično složeno i podložno greškama jer nemate tipsku sigurnost.

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}