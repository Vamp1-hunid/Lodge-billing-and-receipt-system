package com.example.lodgebilling;

// Imports: each line pulls in a class used below.
import javafx.application.Application;               // base class of every JavaFX program
import javafx.application.Platform;                  // lets us close the app (File > Exit)
import javafx.beans.property.SimpleStringProperty;   // a watchable text value for table cells
import javafx.collections.FXCollections;             // creates observable lists
import javafx.collections.ObservableList;            // a list the table watches
import javafx.geometry.Insets;                       // padding
import javafx.scene.Scene;                           // the content of a window
import javafx.scene.control.*;                       // Button, Label, TextField, ComboBox, TableView, Alert...
import javafx.scene.layout.*;                        // GridPane, HBox, VBox, BorderPane, Priority
import javafx.stage.Stage;                           // the window itself

import java.util.function.Function;                  // used by the column() helper method

public class App extends Application {

    // ---- Data ----
    // Every saved bill lives here. The table is connected to it, so adding or removing
    // a bill updates the screen automatically. It is cleared when the app closes.
    private final ObservableList<Bill> bills = FXCollections.observableArrayList();
    private int nextBillNumber = 1;      // used to make the ids B001, B002, B003...

    // ---- Controls ----
    // These are fields (not local variables) because several methods below need to read them.
    private final ComboBox<CurrencyType> currencyBox =
            new ComboBox<>(FXCollections.observableArrayList(CurrencyType.values()));
    private final TextField nameField = new TextField();
    private final ComboBox<RoomType> roomBox =
            new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
    private final TextField nightsField = new TextField();
    private final TextField extrasField = new TextField();
    private final TextField paidField = new TextField();
    private final TextArea notesArea = new TextArea();
    private final Label statusLabel = new Label();            // green/red message under the buttons
    private final VBox priceList = new VBox(4);               // holds the room price lines
    private final TableView<Bill> table = new TableView<>(bills);
    private Label extrasLabel;                                // their text changes with the currency
    private Label paidLabel;

    // JavaFX calls start() when the program opens. "stage" is the main window.
    // Pieces 5b to 5e all go inside this method.
    @Override
    public void start(Stage stage) {