package eci.arep;

import eci.arep.services.ReflexCalculator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HTTPServer {


    public static void main(String[] args) throws IOException {
        ReflexCalculator calculator = new ReflexCalculator();
        ServerSocket serverSocket = new ServerSocket(36000);
        boolean flag = true;
        while (flag){
            Socket clientSocket = serverSocket.accept();

            OutputStream out = clientSocket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            try {
                Map<String, String> data = getDataEndpoint(in);

                switch (data.get("endpoint")){
                    case "/calculadora":
                        writeOutput(out, getHtml(), 200, "OK", "text/html");
                        break;
                    case "/computar":
                        Double[] params = extractDoubleParams(data.get("comando"));
                        String op =data.get("comando").split("\\(")[0];
                        if (op.equals("qck")) writeOutput(out, String.format("{\"respuesta\": \"%s\"}", Arrays.toString(calculator.quicksort(params))), 200, "OK", "application/json");
                        else writeOutput(out, String.format("{\"respuesta\": \"%s\"}", calculator.calculate(op, params).toString()), 200, "OK", "application/json");
                        break;
                    case "/finish":
                        flag = false;
                        break;
                }


                out.close();
                in.close();
                clientSocket.close();

            } catch (Exception e) {
                writeOutput(out, "<h1>Ocurrio un error</h1>", 500, "INTERNAL SERVER ERROR", "application/json");
                e.printStackTrace();
            }
        }
        serverSocket.close();
    }


    public static void writeOutput(OutputStream out, String content, int statusCode, String msg, String contentType){
        try {
            String rta= String.format("HTTP/1.1 %d %s\r\n", statusCode, msg)
                    + String.format("Content-Type: %s\r\n\r\n", contentType)
                    + content;
            out.write(rta.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> getDataEndpoint(BufferedReader in){

        String[] aux;
        Map<String, String> rta = new HashMap<>();
        try{
            String inputLine = in.readLine();
            aux = inputLine.split(" ");
            rta.put("method", aux[0]);
            rta.put("endpoint", aux[1].split("\\?")[0]);

            if (aux[1].contains("?")) rta.put("comando", aux[1].split("=")[1]);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return rta;

    }

    public static String getHtml(){
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Form Example</title>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>Calculadora</h1>\n" +
                "<form action=\"/hello\">\n" +
                "    <label for=\"name\">Operacion:</label><br>\n" +
                "    <input type=\"text\" id=\"op\" name=\"op\" value=\"cos(0.5)\"><br><br>\n" +
                "    <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
                "</form>\n" +
                "<div id=\"getrespmsg\"></div>\n" +
                "\n" +
                "<script>\n" +
                "            function loadGetMsg() {\n" +
                "                let nameVar = document.getElementById(\"op\").value;\n" +
                "                let url = \"/computar?comando=\" + nameVar;\n" +
                "\n" +
                "                fetch (url, {method: 'POST'})\n" +
                "                    .then(x => x.text())\n" +
                "                    .then(y => document.getElementById(\"getrespmsg\").innerHTML = y);\n" +
                "            }\n" +
                "        </script>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

    private static Double[] extractDoubleParams(String param){

        String sParams = param.split("\\(")[1].replace(")", "");
        List<Double> aux = Arrays.stream(sParams.split(",")).map(x -> Double.parseDouble(x)).collect(Collectors.toList());
        Double[] params = new Double[aux.size()];
        return aux.toArray(params);

    }
}
