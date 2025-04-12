module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires com.fasterxml.jackson.databind;

    opens org.example.demo to javafx.fxml, com.fasterxml.jackson.databind;
    exports org.example.demo;
    exports org.example.demo.wardrobe;
    opens org.example.demo.wardrobe to com.fasterxml.jackson.databind, javafx.fxml;
    exports org.example.demo.feelings;
    opens org.example.demo.feelings to com.fasterxml.jackson.databind, javafx.fxml;
    exports org.example.demo.weather;
    opens org.example.demo.weather to com.fasterxml.jackson.databind, javafx.fxml;
    exports org.example.demo.category;
    opens org.example.demo.category to com.fasterxml.jackson.databind, javafx.fxml;
}