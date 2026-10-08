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
        // ---- Menu bar: File and Help ----
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> Platform.exit());               // closes the app
        Menu fileMenu = new Menu("_File", null, exitItem);

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION,
                "Lodge Billing Lab\nBills are kept only until you close the app.").showAndWait());
        Menu helpMenu = new Menu("_Help", null, aboutItem);

        MenuBar menuBar = new MenuBar(fileMenu, helpMenu);

        // ---- Toolbar: New guest | View receipt | Delete selected bill ----
        Button newGuestBtn = new Button("New guest");
        Button viewReceiptBtn = new Button("View receipt");
        Button deleteBtn = new Button("Delete selected bill");
        ToolBar toolBar = new ToolBar(newGuestBtn, viewReceiptBtn, deleteBtn);

        // These two buttons are greyed out until a row is selected in the table.
        viewReceiptBtn.disableProperty().bind(
                table.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.disableProperty().bind(
                table.getSelectionModel().selectedItemProperty().isNull());

        // Each button calls one of the action methods written in piece 5g.
        newGuestBtn.setOnAction(e -> onNewGuest());
        viewReceiptBtn.setOnAction(e -> onViewReceipt());
        deleteBtn.setOnAction(e -> onDelete());
        // ---- Page heading ----
        Label title = new Label("Lodge Billing Lab");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        Label subtitle = new Label("ICT261 Lecture 3  |  Mr E Nyirenda  |  Mulungushi University");

        // ---- Form settings ----
        currencyBox.setValue(CurrencyType.ZMW);                    // start with kwacha selected
        currencyBox.setOnAction(e -> updateCurrencyLabels());      // when changed, refresh labels and prices
        currencyBox.setMaxWidth(Double.MAX_VALUE);                 // let the drop-downs stretch full width
        roomBox.setMaxWidth(Double.MAX_VALUE);
        roomBox.setPromptText("Select room type");
        notesArea.setPrefRowCount(2);
        notesArea.setWrapText(true);

        // These two labels are kept in fields because their text ("in K" / "in $") changes.
        extrasLabel = labelFor("_Extras in K:", extrasField);
        paidLabel = labelFor("Amount _paid in K:", paidField);

        // ---- Form layout: labels in column 0, inputs in column 1 ----
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        ColumnConstraints labelColumn = new ColumnConstraints(140);   // fixed width for labels
        ColumnConstraints inputColumn = new ColumnConstraints();
        inputColumn.setHgrow(Priority.ALWAYS);                        // inputs take the remaining width
        form.getColumnConstraints().addAll(labelColumn, inputColumn);

        form.addRow(0, labelFor("C_urrency:", currencyBox), currencyBox);
        form.addRow(1, labelFor("_Guest name:", nameField), nameField);
        form.addRow(2, labelFor("_Room type:", roomBox), roomBox);
        form.addRow(3, labelFor("N_ights:", nightsField), nightsField);
        form.addRow(4, extrasLabel, extrasField);
        form.addRow(5, paidLabel, paidField);
        form.addRow(6, labelFor("No_tes:", notesArea), notesArea);

        // ---- Calculate and Save buttons ----
        Button calculateBtn = new Button("_Calculate");
        Button saveBtn = new Button("_Save bill");
        saveBtn.setDefaultButton(true);                            // Enter key presses Save
        calculateBtn.setOnAction(e -> onCalculate());
        saveBtn.setOnAction(e -> onSave());
        HBox actionButtons = new HBox(10, calculateBtn, saveBtn);

        // ---- Right-hand panel: prices and rules ----
        Label priceTitle = new Label("Practice room prices");
        priceTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label ruleDiscount = new Label(String.format("%d nights or more: %.0f%% off the room charge.",
                Bill.DISCOUNT_MIN_NIGHTS, Bill.DISCOUNT_RATE * 100));
        Label ruleExtras = new Label("Extras get no discount. No tax in this lab.");
        Label ruleSession = new Label("Saved bills last only until you close the app.");
        ruleDiscount.setWrapText(true);                            // long text wraps onto new lines
        ruleExtras.setWrapText(true);
        ruleSession.setWrapText(true);

        VBox pricePanel = new VBox(10, priceTitle, priceList, new Separator(),
                ruleDiscount, ruleExtras, ruleSession);
        pricePanel.setPadding(new Insets(15));
        pricePanel.setPrefWidth(260);
        pricePanel.setStyle("-fx-background-color: #e8f0fb; -fx-background-radius: 8;");

        // Put the form on the left and the price panel on the right.
        HBox formRow = new HBox(20, form, pricePanel);
        HBox.setHgrow(form, Priority.ALWAYS);
        // ---- Saved bills table ----
        // column(...) is a helper written in piece 5g. The second part says what text to show.
        table.getColumns().add(column("Bill", Bill::getId));
        table.getColumns().add(column("Guest", Bill::getGuest));
        table.getColumns().add(column("Room", b -> b.getRoom().toString()));
        table.getColumns().add(column("Nights", b -> String.valueOf(b.getNights())));
        table.getColumns().add(column("Total", b -> b.getCurrency().format(b.getTotal())));
        table.getColumns().add(column("Paid", b -> b.getCurrency().format(b.getPaid())));
        table.getColumns().add(column("Balance", b -> b.getCurrency().format(b.getBalance())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No bills saved yet."));    // shown while the table is empty
        table.setPrefHeight(150);

        Label savedLabel = new Label("Saved bills");
        savedLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        // Fill in the "in K" labels and the price list for the starting currency.
        updateCurrencyLabels();

        // ---- Put everything in the window ----
        VBox content = new VBox(10, title, subtitle, formRow, actionButtons,
                statusLabel, savedLabel, table);
        content.setPadding(new Insets(15));
        VBox.setVgrow(table, Priority.ALWAYS);                     // the table takes spare height

        BorderPane root = new BorderPane();
        root.setTop(new VBox(menuBar, toolBar));                   // menu bar above toolbar
        root.setCenter(content);

        stage.setTitle("Lodge Billing Lab");
        stage.setScene(new Scene(root, 900, 700));
        stage.show();
    }   // <- end of the start() method