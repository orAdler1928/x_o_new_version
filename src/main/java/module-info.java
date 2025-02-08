module com.example.x_o_new_version {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.x_o_new_version to javafx.fxml;
    exports com.example.x_o_new_version;
}