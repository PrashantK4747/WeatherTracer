/*
 * Project Title: "-----Weather Tracer-----"
 * Weather Tracer is a Java Swing desktop application that displays the current and 5-day weather forecast
 * for any location. It uses OpenWeatherMap and OpenStreetMap APIs to fetch weather data and geocode locations,
 * providing a modern, user-friendly interface for weather tracking.
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;

public class App {
    private static final String WEATHER_API_KEY = "4088c7dc90c51913dfc2f2df80ed1a1e";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1";
    private static final String IP_GEO_URL = "http://ip-api.com/json";

    // Weather icon mapping
    private static final Map<String, String> WEATHER_ICONS = new HashMap<>();
    static {
        WEATHER_ICONS.put("clear sky", "☀️");
        WEATHER_ICONS.put("few clouds", "🌤️");
        WEATHER_ICONS.put("scattered clouds", "⛅");
        WEATHER_ICONS.put("broken clouds", "☁️");
        WEATHER_ICONS.put("overcast clouds", "☁️");
        WEATHER_ICONS.put("shower rain", "🌦️");
        WEATHER_ICONS.put("rain", "🌧️");
        WEATHER_ICONS.put("thunderstorm", "⛈️");
        WEATHER_ICONS.put("snow", "❄️");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // Create the main application window (JFrame) with the title "Weather Tracer"
        JFrame frame = new JFrame("Weather Tracer");
        // Set the default close operation to exit the application when the window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set the size of the window to 1200x700 pixels
        frame.setSize(1200, 700);
        // Center the window on the screen
        frame.setLocationRelativeTo(null);

        // Create the main panel with BorderLayout to hold all UI components
        JPanel mainPanel = new JPanel(new BorderLayout());
        // Set the background color of the main panel to a light blue shade
        mainPanel.setBackground(new Color(240, 248, 255));

        // Create the title panel (top of the window)
        JPanel titlePanel = createTitlePanel();
        
        // Create the search panel (below the title)
        JPanel searchPanel = createSearchPanel();
        
        // Create the forecast panel (center area for weather cards)
        JPanel forecastPanel = createForecastPanel();

        // Combine the title and search panels into a single north panel
        JPanel northPanel = new JPanel();
        // Use vertical BoxLayout to stack title and search panels
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        // Set the background color of the north panel to match the main panel
        northPanel.setBackground(new Color(240, 248, 255));
        // Add the title panel to the north panel
        northPanel.add(titlePanel);
        // Add the search panel below the title panel
        northPanel.add(searchPanel);

        // Add the north panel to the top (NORTH) of the main panel
        mainPanel.add(northPanel, BorderLayout.NORTH);
        // Add the forecast panel to the center of the main panel
        mainPanel.add(forecastPanel, BorderLayout.CENTER);

        // Get the location text field from the search panel (component index 1)
        JTextField locationField = (JTextField) searchPanel.getComponent(1);
        // Get the search button from the search panel (component index 2)
        JButton searchButton = (JButton) searchPanel.getComponent(2);
        
        // Define a runnable to update the weather display when searching
        Runnable updateWeather = () -> updateWeatherDisplay(locationField, forecastPanel, frame);
        
        // Add an action listener to the search button to trigger weather update in a new thread
        searchButton.addActionListener(e -> new Thread(updateWeather).start());
        // Add an action listener to the location field (Enter key) to trigger weather update
        locationField.addActionListener(e -> new Thread(updateWeather).start());

        // Set the main panel as the content pane of the frame
        frame.setContentPane(mainPanel);
        // Make the frame visible
        frame.setVisible(true);

        // Load the initial weather data for the user's location in a new thread
        new Thread(() -> loadInitialWeather(locationField, forecastPanel)).start();
    }

    private static JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.setBackground(new Color(240, 248, 255));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        // Add horizontal glue before and after to center the label
        titlePanel.add(Box.createHorizontalGlue());
        JLabel titleLabel = new JLabel("Weather Tracer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(new Color(41, 128, 185));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createHorizontalGlue());
        return titlePanel;
    }

    private static JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        searchPanel.setBackground(new Color(240, 248, 255));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JLabel locationLabel = new JLabel("Location:");
        locationLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        locationLabel.setForeground(new Color(52, 73, 94));

        JTextField locationField = new JTextField(25);
        locationField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        locationField.setBackground(Color.WHITE);
        locationField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JButton searchButton = createStyledButton("Search", new Color(41, 128, 185));

        searchPanel.add(locationLabel);
        searchPanel.add(locationField);
        searchPanel.add(searchButton);

        return searchPanel;
    }

    private static JPanel createForecastPanel() {
        JPanel forecastPanel = new JPanel(new BorderLayout());
        forecastPanel.setBackground(new Color(240, 248, 255));
        forecastPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        // Forecast cards container (no header here now)
        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBackground(new Color(240, 248, 255));

        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(240, 248, 255));

        forecastPanel.add(scrollPane, BorderLayout.CENTER);

        return forecastPanel;
    }

    private static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private static void updateWeatherDisplay(JTextField locationField, JPanel forecastPanel, JFrame frame) {
        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a location name.");
            return;
        }

        try {
            double[] latlon = geocodeLocation(location);
            if (latlon == null) {
                JOptionPane.showMessageDialog(frame, "Location not found.");
                return;
            }

            String weatherJson = fetchWeather(String.valueOf(latlon[0]), String.valueOf(latlon[1]));
            updateForecastDisplay(forecastPanel, weatherJson);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error loading weather data: " + ex.getMessage());
        }
    }

    private static void updateForecastDisplay(JPanel forecastPanel, String weatherJson) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Get the scrollpane and its viewport
                JScrollPane scrollPane = (JScrollPane) forecastPanel.getComponent(0);
                JPanel cardsContainer = (JPanel) scrollPane.getViewport().getView();
                cardsContainer.removeAll();

                JSONObject obj = new JSONObject(weatherJson);
                JSONArray list = obj.getJSONArray("list");

                // Group forecast by date
                Map<String, java.util.List<JSONObject>> dailyForecasts = new LinkedHashMap<>();
                
                for (int i = 0; i < list.length(); i++) {
                    JSONObject entry = list.getJSONObject(i);
                    String dt_txt = entry.getString("dt_txt");
                    String date = dt_txt.split(" ")[0];
                    
                    dailyForecasts.computeIfAbsent(date, k -> new ArrayList<>()).add(entry);
                }

                // Create forecast cards
                boolean isFirstDay = true;
                for (Map.Entry<String, java.util.List<JSONObject>> dayEntry : dailyForecasts.entrySet()) {
                    // Add "Today's Weather" header before the first day
                    if (isFirstDay) {
                        JLabel todayHeaderLabel = new JLabel("Today's Weather", SwingConstants.CENTER);
                        todayHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                        todayHeaderLabel.setForeground(new Color(52, 73, 94)); // Changed to dark blue-gray
                        todayHeaderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        todayHeaderLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
                        cardsContainer.add(todayHeaderLabel);
                    }
                    
                    JPanel dayCard = createDayCard(dayEntry.getKey(), dayEntry.getValue());
                    cardsContainer.add(dayCard);
                    cardsContainer.add(Box.createVerticalStrut(10));
                    
                    // Add the "5-Day Weather Forecast" header after the first day (today's forecast)
                    if (isFirstDay) {
                        JLabel headerLabel = new JLabel("5-Day Weather Forecast", SwingConstants.CENTER);
                        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                        headerLabel.setForeground(new Color(52, 73, 94)); // Changed to dark blue-gray
                        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
                        cardsContainer.add(headerLabel);
                        cardsContainer.add(Box.createVerticalStrut(10));
                        isFirstDay = false;
                    }
                }

                cardsContainer.revalidate();
                cardsContainer.repaint();
            } catch (Exception e) {
                JLabel errorLabel = new JLabel("Error loading forecast data", SwingConstants.CENTER);
                errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                errorLabel.setForeground(Color.RED);
                
                JScrollPane scrollPane = (JScrollPane) forecastPanel.getComponent(0);
                JPanel cardsContainer = (JPanel) scrollPane.getViewport().getView();
                cardsContainer.removeAll();
                cardsContainer.add(errorLabel);
                cardsContainer.revalidate();
            }
        });
    }

    private static JPanel createDayCard(String date, java.util.List<JSONObject> forecasts) {
        JPanel dayCard = new JPanel(new BorderLayout());
        dayCard.setBackground(Color.WHITE);
        dayCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Date header
        String formattedDate = formatDate(date);
        JLabel dateLabel = new JLabel(formattedDate);
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dateLabel.setForeground(new Color(52, 73, 94));

        // Forecast entries panel
        JPanel forecastsPanel = new JPanel();
        forecastsPanel.setLayout(new BoxLayout(forecastsPanel, BoxLayout.Y_AXIS));
        forecastsPanel.setBackground(Color.WHITE);

        for (JSONObject forecast : forecasts) {
            JPanel entryPanel = createForecastEntry(forecast);
            forecastsPanel.add(entryPanel);
            if (forecasts.indexOf(forecast) < forecasts.size() - 1) {
                forecastsPanel.add(Box.createVerticalStrut(8));
            }
        }

        dayCard.add(dateLabel, BorderLayout.NORTH);
        dayCard.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        dayCard.add(forecastsPanel, BorderLayout.SOUTH);

        return dayCard;
    }

    private static JPanel createForecastEntry(JSONObject forecast) {
        JPanel entryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        entryPanel.setBackground(new Color(248, 249, 250));
        entryPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        try {
            String dt_txt = forecast.getString("dt_txt");
            String time = dt_txt.split(" ")[1].substring(0, 5);
            
            JSONObject main = forecast.getJSONObject("main");
            double temp = main.getDouble("temp");
            double feelsLike = main.getDouble("feels_like");
            int humidity = main.getInt("humidity");
            
            JSONArray weatherArr = forecast.getJSONArray("weather");
            JSONObject weather = weatherArr.getJSONObject(0);
            String description = weather.getString("description");
            
            String icon = WEATHER_ICONS.getOrDefault(description.toLowerCase(), "🌤️");

            // Time label
            JLabel timeLabel = new JLabel(time);
            timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            timeLabel.setForeground(new Color(52, 73, 94));
            timeLabel.setPreferredSize(new Dimension(60, 20));

            // Weather icon
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            iconLabel.setPreferredSize(new Dimension(40, 20));

            // Temperature
            JLabel tempLabel = new JLabel(String.format("%.1f°C", temp));
            tempLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            tempLabel.setForeground(new Color(231, 76, 60));
            tempLabel.setPreferredSize(new Dimension(70, 20));

            // Description
            JLabel descLabel = new JLabel(capitalizeFirst(description));
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            descLabel.setForeground(new Color(127, 140, 141));
            descLabel.setPreferredSize(new Dimension(150, 20));

            // Additional info
            JLabel infoLabel = new JLabel(String.format("Feels %.1f°C • %d%% humidity", feelsLike, humidity));
            infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            infoLabel.setForeground(new Color(149, 165, 166));

            entryPanel.add(timeLabel);
            entryPanel.add(Box.createHorizontalStrut(10));
            entryPanel.add(iconLabel);
            entryPanel.add(Box.createHorizontalStrut(10));
            entryPanel.add(tempLabel);
            entryPanel.add(Box.createHorizontalStrut(15));
            entryPanel.add(descLabel);
            entryPanel.add(Box.createHorizontalStrut(15));
            entryPanel.add(infoLabel);

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error parsing forecast entry");
            errorLabel.setForeground(Color.RED);
            entryPanel.add(errorLabel);
        }

        return entryPanel;
    }

    private static String formatDate(String date) {
        try {
            String[] parts = date.split("-");
            String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            return String.format("%s %d, %s", months[month], day, parts[0]);
        } catch (Exception e) {
            return date;
        }
    }

    private static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static void loadInitialWeather(JTextField locationField, JPanel forecastPanel) {
        try {
            double[] latlon = getUserLocationByIP();
            if (latlon != null) {
                String locationName = reverseGeocode(String.valueOf(latlon[0]), String.valueOf(latlon[1]));
                SwingUtilities.invokeLater(() -> {
                    if (locationName != null) {
                        locationField.setText(locationName);
                    } else {
                        locationField.setText(latlon[0] + ", " + latlon[1]);
                    }
                });

                String weatherJson = fetchWeather(String.valueOf(latlon[0]), String.valueOf(latlon[1]));
                updateForecastDisplay(forecastPanel, weatherJson);
            }
        } catch (Exception e) {
            // Ignore startup errors
        }
    }

    // Existing utility methods remain the same
    private static double[] geocodeLocation(String location) throws IOException, JSONException {
        String urlStr = String.format(NOMINATIM_URL, URLEncoder.encode(location, "UTF-8"));
        HttpURLConnection conn = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();
        JSONArray arr = new JSONArray(content.toString());
        if (arr.length() == 0) return null;
        JSONObject obj = arr.getJSONObject(0);
        double lat = obj.getDouble("lat");
        double lon = obj.getDouble("lon");
        return new double[]{lat, lon};
    }

    private static double[] getUserLocationByIP() throws IOException, JSONException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(IP_GEO_URL).toURL().openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();
        JSONObject obj = new JSONObject(content.toString());
        if (!"success".equals(obj.optString("status"))) return null;
        double lat = obj.getDouble("lat");
        double lon = obj.getDouble("lon");
        return new double[]{lat, lon};
    }

    private static String reverseGeocode(String lat, String lon) {
        try {
            String urlStr = "https://nominatim.openstreetmap.org/reverse?lat=" + lat + "&lon=" + lon + "&format=json";
            HttpURLConnection conn = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();
            JSONObject obj = new JSONObject(content.toString());
            JSONObject address = obj.optJSONObject("address");
            if (address != null) {
                String city = address.optString("city", address.optString("town", address.optString("village", "")));
                String state = address.optString("state", "");
                String country = address.optString("country", "");
                StringBuilder sb = new StringBuilder();
                if (!city.isEmpty()) sb.append(city);
                if (!state.isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(state);
                }
                if (!country.isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(country);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private static String fetchWeather(String lat, String lon) throws IOException {
        String urlStr = WEATHER_API_URL + "?lat=" + lat + "&lon=" + lon + "&appid=" + WEATHER_API_KEY + "&units=metric";
        URL url = URI.create(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();
        return content.toString();
    }
}