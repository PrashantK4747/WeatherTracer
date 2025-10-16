# WeatherTracer ☁️🌦️

WeatherTracer is a Java-based desktop application that provides real-time weather forecasts based on the user's location. It utilizes the OpenWeatherMap API for fetching current and future weather conditions and Google Maps Static API for location-based visualization.

## 🖥️ System Requirements

<span style="color:red; font-weight:bold">• Operating System: Windows, macOS, or Linux (tested on Windows 10/11)<br>
• Java Development Kit (JDK) 8 or later (recommended: JDK 11+)<br>
• Internet connection (required for weather and geocoding APIs)<br>
• At least 2 GB RAM<br>
• 50 MB free disk space<br>
• Screen resolution: 1280x720 or higher</span>

## 📱 Platform Support

**This application is designed for desktop operating systems only (Windows, macOS, Linux).**
**Android and other mobile platforms are not supported.**

## 📦 Features

- 🌤️ Get real-time weather updates for your location
- 🔎 Search for weather in different cities
- 🧭 Uses IP-based geolocation and manual input
- 🖥️ Simple and interactive Swing-based GUI
- 📡 Integrates with OpenWeatherMap API
- 🧊 Lightweight `.jar` application with no external dependencies other than `json-20210307.jar`

# 📚 Dependencies

- OpenWeatherMap API
- Google Maps
- JSON-java library (json-20210307.jar)

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK 8 or later)
- Internet connection (for API calls)
- External library: `json-20210307.jar` (included)


### Installation (Step-by-Step)

1. **Download & Install Java:**
   - Download and install the latest Java Development Kit (JDK) from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [AdoptOpenJDK](https://adoptium.net/).
   - Verify installation:
     ```bash
     java -version
     ```

2. **Clone the Repository:**
   ```bash
   git clone https://github.com/yourusername/WeatherTracer.git
   cd WeatherTracer
   ```

3. **Add JSON Library:**
   - Ensure `json-20210307.jar` is present in your project directory (usually in `lib/`).
   - If missing, download from [Maven Central](https://search.maven.org/artifact/org.json/json/20210307/jar).

4. **Build the Application:**
   - If using an IDE (Eclipse/IntelliJ):
     - Import the project as a Java project.
     - Add `json-20210307.jar` to your build path.
   - If using command line:
     ```bash
     javac -cp lib/json-20210307.jar src/App.java
     ```

5. **Run the Application:**
   ```bash
   java -cp lib/json-20210307.jar;src App
   ```
   - On Linux/macOS, use `:` instead of `;` for the classpath separator.

6. **API Keys:**
   - The app uses a demo OpenWeatherMap API key. For production, get your own key from [OpenWeatherMap](https://openweathermap.org/appid) and update it in `App.java`.

7. **Troubleshooting:**
   - If you see errors about missing classes, check your classpath and ensure all dependencies are present.
   - For GUI issues, ensure your Java installation supports Swing.

## Contact

For questions or suggestions, open an issue or contact [prashantk4747@gmail.com]