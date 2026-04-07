module com.smartroom.smartroomapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;


    opens com.smartroom.smartroomapp to javafx.fxml;
    exports com.smartroom.smartroomapp;
}