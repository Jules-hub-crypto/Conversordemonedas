import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

public class ConversorDeMonedas {

    private static final String API_KEY = "b83d40c3156e33b538e1a840";
    private static final String BASE_API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    public static void main(String[] args) {
        HashMap<String, Double> exchangeRates = cargarTasasDeCambio("USD");

        if (exchangeRates == null) {
            System.out.println("No se pudieron cargar las tasas de cambio. Verifique su conexión o clave API.");
            return;
        }

        iniciarInteraccionConUsuario(exchangeRates);
    }

    private static HashMap<String, Double> cargarTasasDeCambio(String baseCurrency) {
        HashMap<String, Double> exchangeRates = new HashMap<>();
        String apiUrl = BASE_API_URL + baseCurrency;

        try {
            System.out.println("Conectando a la API...");
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() != 200) {
                System.out.println("Error al conectar con la API. Código de respuesta: " + connection.getResponseCode());
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject rates = jsonResponse.getJSONObject("conversion_rates");

            for (String key : rates.keySet()) {
                exchangeRates.put(key, rates.getDouble(key));
            }

            System.out.println("Tasas de cambio cargadas correctamente.");
        } catch (Exception e) {
            System.out.println("Ocurrió un error al cargar las tasas de cambio: " + e.getMessage());
            return null;
        }

        return exchangeRates;
    }

    private static void iniciarInteraccionConUsuario(HashMap<String, Double> exchangeRates) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            mostrarMenu();
            int opcion = leerEntero(scanner, "Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    mostrarMonedasDisponibles(exchangeRates);
                    break;
                case 2:
                    filtrarMonedasInteres(exchangeRates, scanner);
                    break;
                case 3:
                    convertirMonedas(exchangeRates, scanner);
                    break;
                case 4:
                    System.out.println("Saliendo del programa. ¡Gracias por usar el conversor de monedas!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\nOpciones:");
        System.out.println("1. Mostrar monedas disponibles");
        System.out.println("2. Filtrar monedas de interés");
        System.out.println("3. Convertir entre monedas");
        System.out.println("4. Salir");
    }

    private static void mostrarMonedasDisponibles(HashMap<String, Double> exchangeRates) {
        System.out.println("Monedas disponibles:");
        for (String moneda : exchangeRates.keySet()) {
            System.out.println("- " + moneda);
        }
    }

    private static void filtrarMonedasInteres(HashMap<String, Double> exchangeRates, Scanner scanner) {
        System.out.print("Ingrese las monedas de interés separadas por comas (por ejemplo: USD,EUR,COP): ");
        String[] monedasInteres = scanner.nextLine().split(",");

        System.out.println("Tasas de cambio para las monedas seleccionadas:");
        for (String moneda : monedasInteres) {
            moneda = moneda.trim().toUpperCase();
            if (exchangeRates.containsKey(moneda)) {
                System.out.println(moneda + ": " + exchangeRates.get(moneda));
            } else {
                System.out.println("Moneda no encontrada: " + moneda);
            }
        }
    }

    private static void convertirMonedas(HashMap<String, Double> exchangeRates, Scanner scanner) {
        System.out.print("Ingrese la moneda de origen (por ejemplo, USD): ");
        String monedaOrigen = scanner.nextLine().trim().toUpperCase();

        System.out.print("Ingrese la moneda de destino (por ejemplo, EUR): ");
        String monedaDestino = scanner.nextLine().trim().toUpperCase();

        if (!exchangeRates.containsKey(monedaOrigen) || !exchangeRates.containsKey(monedaDestino)) {
            System.out.println("Una o ambas monedas no fueron encontradas.");
            return;
        }

        double monto = leerDouble(scanner, "Ingrese el monto a convertir: ");
        double tasaOrigen = exchangeRates.get(monedaOrigen);
        double tasaDestino = exchangeRates.get(monedaDestino);
        double resultado = monto / tasaOrigen * tasaDestino;

        System.out.printf("Resultado: %.2f %s%n", resultado, monedaDestino);
    }

    private static int leerEntero(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número entero.");
            }
        }
    }

    private static double leerDouble(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número válido.");
            }
        }
    }
}
