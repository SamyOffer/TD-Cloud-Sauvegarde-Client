module com.example.sauvegerdejavafx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.sauvegerdejavafx to javafx.fxml;
    exports com.example.sauvegerdejavafx;
}