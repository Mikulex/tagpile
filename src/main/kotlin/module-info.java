module com.mikulex.tagpile {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires org.xerial.sqlitejdbc;
    requires org.slf4j;


    opens com.mikulex.tagpile to javafx.fxml;
    exports com.mikulex.tagpile;
}