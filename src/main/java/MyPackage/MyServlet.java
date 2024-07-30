package MyPackage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/MyPackage/MyServlet")
public class MyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public MyServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String apiKey = "185c85c9463debc7fc6e8c2fd8b07d26";
        String city = request.getParameter("city"); 

        if (city == null || city.isEmpty()) {
            city = "New Delhi"; // default city if input is empty
        }

        // URL-encode the city name to handle spaces and special characters
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + encodedCity + "&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check for HTTP response code before attempting to read the response
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("HTTP error code: " + responseCode);
                request.setAttribute("error", "Unable to fetch weather data. Please try again later.");
                request.getRequestDispatcher("/index.jsp").forward(request, response);
                return;
            }

            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            Scanner scanner = new Scanner(reader);
            StringBuilder responseContent = new StringBuilder();

            while (scanner.hasNext()) {
                responseContent.append(scanner.nextLine());
            }

            scanner.close();
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseContent.toString(), JsonObject.class);

            long dateTimestamp = jsonObject.get("dt").getAsLong() * 1000;
            String date = new Date(dateTimestamp).toString();
            double temperatureKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
            int temperatureCelsius = (int) (temperatureKelvin - 273.15);
            int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();
            double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();
            String weatherCondition = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();
            double feelsLikeTemperature = jsonObject.getAsJsonObject("main").get("feels_like").getAsDouble();
            int realTempInCelcius = (int) (feelsLikeTemperature  - 273.15);
            int visibility = jsonObject.get("visibility").getAsInt();
//            System.out.println("Date: " + date);
//            System.out.println("City: " + city);
//            System.out.println("Temperature: " + temperatureCelsius);
//            System.out.println("Weather Condition: " + weatherCondition);
//            System.out.println("Humidity: " + humidity);
//            System.out.println("Wind Speed: " + windSpeed);

            request.setAttribute("date", date);
            request.setAttribute("city", city);
            request.setAttribute("temperature", temperatureCelsius);
            request.setAttribute("feelsLikeTemperature", realTempInCelcius);
            request.setAttribute("weatherCondition", weatherCondition);
            request.setAttribute("humidity", humidity);
            request.setAttribute("windSpeed", windSpeed);
            request.setAttribute("weatherData", responseContent.toString());
            request.setAttribute("visibility",visibility);

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            request.setAttribute("error", "Error fetching weather data: " + e.getMessage());
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
