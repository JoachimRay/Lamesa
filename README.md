# Lamesa
A restaurant manager built using Java and JavaFX, it is a desktop application to provide a management system for Lamesa.

---

# Features 

### Meals

- Add, update, and delete meals
- Assign meal categories and types
- search meals quickly

### Category Management 

 - Create and manage categories 
 - Automatically reflected in the meals panel

### Sales Tracking 

- Record customer orders
- Track quantity sold
- View top-selling meals

### SQLite Database Integration 

- Local file-based storage
- Minimal setup required

### JavaFX UI 

- Clean and responsive interface
- Multi-page navigation

----

# Tech Stack 

| Component            | Technology                                                  |
| -------------------- | ----------------------------------------------------------- |
| Programming Language | **Java** (JDK 17+)                                          |
| UI Framework         | **JavaFX**                                                  |
| Database             | **SQLite**                                                  |
| Build Tool           | **Maven** or manual JAR packaging (depending on your setup) |


---

# Installation & Setup 

### Clone Repository

```bash
git clone https://github.com/yourusername/lamesa.git
cd lamesa
```

### Libraries using Maven 

ensure your pom.xml includes the required dependencies. If using a local setup (intelliJ), make sure your javaFX SDK is linked. 

### SQlite Database 

Lamesa will automatically create the database file if it doesn't exist.

```bash
database/lamesa.db
```

--- 

# How To Run 

If using Maven:

- a script is also available in the root folder called watch.bat

```bash
mvn clean install
mvn javafx:run
```

if running manually: 

```bash
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar Lamesa.jar
```

--- 

# License

For academic and personal use only. 
Feel free to modify and improve it.












