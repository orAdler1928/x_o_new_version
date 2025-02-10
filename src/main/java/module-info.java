module com.example.x_o_new_version {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.x_o_new_version to javafx.fxml;
    exports com.example.x_o_new_version;
}