# FocusFlow Installation & Running Guide

## Prerequisites

- **Java JDK 21** (or compatible with JavaFX 21)
  - [Download JDK](https://adoptium.net/temurin/releases/?version=21)
- **JavaFX SDK 21.0.7**
  - [Download JavaFX SDK](https://gluonhq.com/products/javafx/)
- **Maven** (for building the project)
  - [Download Maven](https://maven.apache.org/download.cgi)

## Setup

### 1. Install Java JDK
- Download and install JDK 21 from the link above.
- Add the JDK `bin` directory to your system `PATH`.

### 2. Install JavaFX SDK
- Download JavaFX SDK 21.0.7 from the link above.
- Extract the downloaded archive to a directory of your choice (e.g., `C:\javafx-sdk-21.0.7`).
- Ensure you remember the path where you extracted the JavaFX SDK, as you will need it to run the application.

### 3. Install Maven
- Download and extract Maven.
- Add the Maven `bin` directory to your system `PATH`.

### 4. Clone the Repository
```sh
git clone https://github.com/milesbaack/FocusFlow.git
cd FocusFlow
```

## Building the Project

1. Open a terminal in the project root directory.
2. Run:
   ```sh
   mvn clean package -DskipTests
   ```
3. The executable JAR will be created at:
   - `target/focusflow-1.0-SNAPSHOT-shaded.jar`

## Running the Application

### If JavaFX is NOT on your system path:
Run the following command (replace the path if your JavaFX SDK is elsewhere):

```sh
java --module-path "[path to JavaFX sdk]" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media -jar target/focusflow-1.0-SNAPSHOT-shaded.jar
```

### If JavaFX is on your system path (advanced users):
You may be able to run:
```sh
java -jar target/focusflow-1.0-SNAPSHOT-shaded.jar
```

## Troubleshooting
- If you see an error about missing JavaFX runtime components, ensure the `--module-path` and `--add-modules` arguments are correct and point to your JavaFX SDK `lib` directory.
- Make sure you are using the correct JDK version (21 or compatible with your JavaFX SDK).

## Dependencies
- JavaFX (controls, fxml, graphics, base, media)
- All other dependencies are bundled in the shaded JAR via Maven.

---

For further help, see the [JavaFX documentation](https://openjfx.io/) or contact the project maintainers. 