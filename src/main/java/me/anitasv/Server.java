package me.anitasv;

import com.google.ortools.Loader;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import me.anitasv.jigsaw.*;
import me.anitasv.sat.GoogleModel;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Server {

    static void writePlainText(HttpExchange exchange, String plainText, int statusCode)
            throws IOException {
        byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "text/plain; utf-8");
        headers.set("Cache-Control", "no-cache; no-store; must-revalidate");
        exchange.sendResponseHeaders(statusCode, plainTextBytes.length);
        exchange.getResponseBody().write(plainTextBytes);
        exchange.getResponseBody().close();
    }

    record FileHandler(String fileName,
                       String contentType) implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                File file = new File(fileName);
                byte[] bytes = Files.readAllBytes(file.toPath());

                Headers headers = exchange.getResponseHeaders();
                headers.set("Content-Type", contentType);
                headers.set("Cache-Control", "no-cache; no-store; must-revalidate");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, String> queryParams(URI uri) {
        String query = uri.getQuery();
        String[] queryParts = query.split("&");
        Map<String, String> qv = new LinkedHashMap<>();
        for (String part : queryParts) {
            int keyLoc = part.indexOf('=');
            if (keyLoc == -1) {
                qv.put(part, null);
            } else {
                String key = part.substring(0, keyLoc);
                String value = part.substring(keyLoc + 1);
                String parsedKey = URLDecoder.decode(key, StandardCharsets.UTF_8);
                String parsedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);
                qv.put(parsedKey, parsedValue);
            }
        }
        return qv;
    }

    static class RandomJigsaw implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> query = queryParams(exchange.getRequestURI());

            try {
                int M = Integer.parseInt(query.get("M"));
                int N = Integer.parseInt(query.get("N"));

                Jigsaw jigsaw = new Jigsaw(M, N);
                StringBuilder builder = new StringBuilder();
                for (int m = 0; m < M; m++) {
                    for (int n = 0; n < N; n++) {
                        JigsawPoke[] pokes = jigsaw.pieces[m][n].pokes;
                        for (int i = 0; i < pokes.length; i++) {
                            builder.append(pokes[i].rep);
                            if (i != pokes.length - 1) {
                                builder.append(",");
                            }
                        }
                        if (n != N - 1) {
                            builder.append("|");
                        } else {
                            if (m != M - 1) {
                                builder.append("\n");
                            }
                        }
                    }
                }
                writePlainText(exchange, builder.toString(), 200);
            } catch (Exception e) {
                e.printStackTrace();
                writePlainText(exchange, e.getMessage(), 500);
            }
        }
    }

    static Charset getCharset(Headers requestHeaders) {
        List<String> ctHeaders = requestHeaders.get("Content-Type");

        Charset charset = StandardCharsets.UTF_8;
        if (ctHeaders != null) {
            for (String ct : ctHeaders) {
                int csIndex = ct.indexOf("charset=");
                if (csIndex > 0) {
                    String charsetReq = ct.substring(csIndex +
                            "charset=".length());
                    int semiIndex = charsetReq.indexOf(";");
                    if (semiIndex > 0) {
                        charsetReq = charsetReq.substring(semiIndex);
                    }
                    charsetReq = charsetReq.trim();
                    if (Charset.isSupported(charsetReq)) {
                        charset = Charset.forName(charsetReq);
                    }
                }
            }
        }
        return charset;
    }


    static class JigsawHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> query = queryParams(exchange.getRequestURI());

            try {
                int M = Integer.parseInt(query.get("M"));
                int N = Integer.parseInt(query.get("N"));

                if (M < 0 || N < 0) {
                    writePlainText(exchange,
                            "Invalid M or N parameters",
                            400);
                    return;
                }
                if (M > 100 || N > 100) {
                    writePlainText(exchange,
                            "Reached configured limit of M or N",
                            400);
                }

                Charset charset = getCharset(exchange.getRequestHeaders());
                Reader iReader = new InputStreamReader(
                        exchange.getRequestBody(), charset);
                BufferedReader is = new BufferedReader(iReader);

                JigsawPiece[] B = new JigsawPiece[M * N];
                String line;
                int index = 0;
                while (index < B.length && ((line = is.readLine()) != null)) {
                    String[] splits = line.split(",");
                    if (splits.length != Jigsaw.SIDES) {
                        writePlainText(exchange, "Line " + (index + 1) +
                                        ": Unexpected number of pokes: " + splits.length,
                                400);
                        return;
                    }
                    B[index] = new JigsawPiece();
                    for (int j = 0; j < Jigsaw.SIDES; j++) {
                        B[index].pokes[j] = JigsawPoke.from(splits[j].charAt(0));
                    }
                    ++index;
                }
                if (index != B.length) {
                    writePlainText(exchange, "Not enough rows: " + index + " <> " + B.length, 400);
                    return;
                }

                int pokeBalance = 0;
                int flats = 0;
                for (int i = 0; i < B.length; i++) {
                    for (int j = 0; j < Jigsaw.SIDES; j++) {
                        pokeBalance += B[i].pokes[j].val;
                        if (B[i].pokes[j] == JigsawPoke.FLAT) {
                            flats++;
                        }
                    }
                }
                int expectedFlats = 2 * M + 2 * N;
                if (flats != expectedFlats) {
                    writePlainText(exchange, "UNSOLVABLE: Flat mismatch: " + flats + "<>" + expectedFlats,
                            200);
                    return;
                }
                if (pokeBalance != 0) {
                    writePlainText(exchange, "UNSOLVABLE: Pokes imbalance: " + pokeBalance,
                            200);
                    return;
                }

                JigsawSolver solver = new JigsawSolver3(M, N, B);
                GoogleModel model = new GoogleModel();
                solver.formulate(model);
                List<JigsawLocation> solution = solver.solve(model);

                if (solution == null) {
                    writePlainText(exchange, "UNSOLVABLE: So solution found", 200);
                    return;
                }

                String output = solution.stream()
                        .map(JigsawLocation::toString)
                        .collect(Collectors.joining("\n"));
                writePlainText(exchange, output, 200);


            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, 0);
                exchange.getResponseBody().close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Loader.loadNativeLibraries();

        HttpServer httpServer = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 8080)
        , 0);

        httpServer.createContext("/jigsaw.html",
                new FileHandler("public/jigsaw.html",
                        "text/html; charset=utf-8"));
        httpServer.createContext("/jigsaw.js",
                new FileHandler("public/jigsaw.js",
                        "text/javascript; charset=utf-8"));
        httpServer.createContext("/favicon.ico",
                new FileHandler("public/favicon.ico",
                        "image/x-icon"));
        httpServer.createContext("/jigsaw/random.txt",
                new RandomJigsaw());
        httpServer.createContext("/jigsaw/solve",
                new JigsawHandler());

        httpServer.start();
    }
}
