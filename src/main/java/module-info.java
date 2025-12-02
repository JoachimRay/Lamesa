module main {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;

    opens main to javafx.fxml;
    exports main;
}
