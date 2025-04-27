package com.example.parking.gui;

import com.example.parking.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.fxml.*;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;

import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;
import org.controlsfx.control.*;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class UserPageController {
    private enum LastClickedPane {
        chooseReservations,
        reservationHistory
    }
    private Owner owner;

    private ScrollPane lastScrollPane = new ScrollPane();
    private Pane lastPane = new Pane();

    // Home
    @FXML
    private Label headerText;
    @FXML
    private Pane homePane, reservationPane, historyReservationPane, depositPane, updateDataPane, backPane;
    @FXML
    private AnchorPane feedbackPane;
    @FXML
    private ScrollPane makeReservationPane;

    LastClickedPane lastClickedPane;
    // Feedback
    @FXML
    private Label submit_msg;
    @FXML
    private TextField ReservationID_field;
    @FXML
    private Rating RatingBar;
    @FXML
    private TextArea feedbackMessage;

    // Deposit
    @FXML
    private TextField depositAmountField;
    @FXML
    private Label  balanceLabel;
    @FXML
    private Button depositButton;

    // Reservations
    @FXML
    private TableView<Reservation> ReservationTable;
    @FXML
    private GridPane gridPaneSlots;

    public void initialize() {
        owner = menuController.getCurrentOwner();
        homePane.setVisible(false);
        reservationPane.setVisible(false);
        depositPane.setVisible(false);
        feedbackPane.setVisible(false);
        updateDataPane.setVisible(false);
        backPane.setVisible(false);
        makeReservationPane.setVisible(false);
        historyReservationPane.setVisible(false);

        headerText.setText("Welcome " + owner.getUserName());
        switchToPane(homePane);
    }

    private void switchToPane(Pane pane) {
        backPane.setVisible(false);

        lastPane.setVisible(false);
        lastScrollPane.setVisible(false);
        pane.setVisible(true);
        lastPane = pane;
    }
    private  void switchToPane(ScrollPane pane) {
        backPane.setVisible(false);

        lastPane.setVisible(false);
        lastScrollPane.setVisible(false);
        pane.setVisible(true);
        lastScrollPane = pane;
    }

    private void showAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        alert.getDialogPane().getStyleClass().add("user-alert");

        URL cssFile = getClass().getResource("/css/adminStyle.css");
        if (cssFile != null) {
            alert.getDialogPane().getStylesheets().add(cssFile.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }

        alert.showAndWait();
    }

    public void handleBackButton() {
        if (lastClickedPane == LastClickedPane.chooseReservations)
            chooseReservations();
        else
            goToReservationHistory();
    }

    // Left Buttons
    // Home Page
    @FXML
    public void goToHomePage() {
        headerText.setText("Welcome " + owner.getUserName());
        switchToPane(homePane);
    }

    // Reservations Page
    public void chooseReservations() {
        headerText.setText("Reservation Management");
        switchToPane(reservationPane);
    }

    // Make Reservation
    public void goToMakeReservation(){
        switchToPane(makeReservationPane);
        headerText.setText("Make a New Reservation");
        backPane.setVisible(true);
        lastClickedPane = LastClickedPane.chooseReservations;
        makeReservationView();
    }
    private ArrayList<Slot> selectedSlots;
    public void makeReservationView() {
        gridPaneSlots.getChildren().clear(); // Clear all previous content
        selectedSlots = new ArrayList<>();

        ArrayList<Slot> slots = SystemManager.getAvailableSlots(owner);
        int columns = 3; // Slots per row
        int rows = (int) Math.ceil(slots.size() / (double) columns); // Calculate total rows dynamically

        // Create a label to display the Total Amount
        Label totalAmountLabel = new Label("Total Amount: $0.0");
        totalAmountLabel.getStyleClass().add("make_reservation_label");

        Label penaltyLabel = new Label("Penalty: $" + String.format("%.2f", owner.getPayment().getPenalty()));
        penaltyLabel.getStyleClass().add("make_reservation_label");

        gridPaneSlots.add(totalAmountLabel, 0, 0, columns, 1); // Add the total amount label above the slots
        gridPaneSlots.add(penaltyLabel, 2, 0, columns, 1); // Add the penalty label above the slots

        for (int i = 0; i < slots.size(); ++i) {
            Slot slot = slots.get(i);
            VBox slotBox = createSlotBox(slot, totalAmountLabel);

            slotBox.setAlignment(Pos.CENTER); // Centers content within the VBox
            slotBox.setSpacing(10);
            slotBox.getStyleClass().add("slot-box");

            int row = i / columns;
            int col = i % columns;
            gridPaneSlots.add(slotBox, col, row + 1); // Offset by 1 to leave space for the total amount label
        }

        gridPaneSlots.setHgap(12); // Horizontal gap between columns
        gridPaneSlots.setVgap(15); // Vertical gap between rows

        // Create and configure the Make Reservation button
        Button makeReservationButton = new Button("Make Reservation");
        makeReservationButton.getStyleClass().add("make-reservation-btn");
        makeReservationButton.setOnAction(event -> {
            if (selectedSlots.isEmpty()) {
                showAlert("Error", "No Slots Selected", "Select Slots To Reserve");
                return;
            }
            makeReservationConfirm(totalAmountLabel.getText());
        });

        // Add the button to the GridPane in the last row
        HBox buttonContainer = new HBox(10, makeReservationButton);
        buttonContainer.setAlignment(Pos.CENTER);
        gridPaneSlots.add(buttonContainer, 0, rows + 2, columns, 1); // Add below the grid in the last row
    }

    private VBox createSlotBox(Slot slot, Label totalAmountLabel) {
        String   spotType = slot.getVehicleType() + " Spot";
        int spotID = slot.getSpotID();
        LocalDateTime startDate = slot.getStartDate();
        LocalDateTime endDate = slot.getEndDate();
        double hours = slot.getHours();
        double amount = slot.getAmount();

        // Header (Spot ID)
        Label labelSpotId = new Label(spotType);
        labelSpotId.setMaxWidth(Double.MAX_VALUE);
        labelSpotId.setAlignment(Pos.CENTER);
        labelSpotId.getStyleClass().add("slot-header");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // Adjust format as needed
        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);

        // Body Rows
        HBox slotRow = createDetailRow("Spot:", String.valueOf(spotID));
        HBox startDateRow = createDetailRow("Start:", formattedStartDate);
        HBox endDateRow = createDetailRow("End:", formattedEndDate);
        HBox hoursRow = createDetailRow("Hours:", String.format("%.2f", hours));

        VBox body = new VBox(5, slotRow, startDateRow, endDateRow, hoursRow);

        // Footer (Amount on the left, Checkbox on the right)
        Label amountLabel = new Label("Amount: $" + String.format("%.2f", amount));
        amountLabel.getStyleClass().add("amount-label");

        CheckBox checkBox = new CheckBox();
        checkBox.getStyleClass().add("slot-checkbox");
        checkBox.setOnAction(e -> {
            if (checkBox.isSelected()) {
                selectedSlots.add(slot);
            }
            else {
                selectedSlots.remove(slot);
            }
            double currentTotalAmount = Payment.totalAmount(owner.getPayment(), selectedSlots);
            totalAmountLabel.setText("Total Amount: $" + String.format("%.2f", currentTotalAmount));
        });

        // Spacer to separate amount and checkbox
        Region spacer = new Region();
        spacer.setMinWidth(1); // Adjust spacing as needed
        HBox.setHgrow(spacer, Priority.ALWAYS); // Expands to push elements apart

        HBox footer = new HBox(1, amountLabel, spacer, checkBox);
        footer.getStyleClass().add("slot-footer");

        // Slot Box Layout
        VBox slotBox = new VBox(labelSpotId, body, footer);
        slotBox.getStyleClass().add("slot-box");

        return slotBox;
    }
    private HBox createDetailRow(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("slot-label");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("slot-value");

        HBox row = new HBox(10, labelNode, valueNode);
        row.getStyleClass().add("slot-row");

        return row;
    }
    private void makeReservationConfirm(String totalAmountText) {
        double penalty = owner.getPayment().getPenalty();
        double amount = Payment.totalAmount(owner.getPayment(), selectedSlots);
        double balance = owner.getPayment().getBalance();

        double totalAmount = amount + penalty;
        // Proceed with reservation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reservation Confirmation");
        alert.setHeaderText("Reservation Details");
        alert.setContentText(String.format("Amount: $%.2f\nPenalty: $%.2f\nFinal Amount: $%.2f\n",
                amount, penalty, totalAmount));

        // Apply the dialog's custom CSS
        alert.getDialogPane().getStyleClass().add("dialog-content");
        URL cssFile = getClass().getResource("/css/adminStyle.css");
        if (cssFile != null) {
            alert.getDialogPane().getStylesheets().add(cssFile.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }
        ButtonType okButton = ButtonType.OK;
        ButtonType cancelButton = ButtonType.CANCEL;
        alert.getButtonTypes().setAll(okButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == okButton) {
                if (balance < totalAmount) {
                    // Handle insufficient balance
                    Alert newAlert = new Alert(Alert.AlertType.ERROR, "Insufficient balance. Please add more funds");
                    newAlert.getDialogPane().getStyleClass().add("dialog-content"); // Apply dialog content CSS
                    newAlert.showAndWait();
                    return;
                }
                for (Slot slot : selectedSlots) {
                    owner.makeReservation(slot);
                }
                Alert newAlert = new Alert(Alert.AlertType.INFORMATION, "Reservations Made Successfully");
                newAlert.getDialogPane().getStyleClass().add("dialog-content"); // Apply dialog content CSS
                newAlert.showAndWait();
                makeReservationView();
            }
        });
    }

    // Reservation History
    public void goToReservationHistory() {
        switchToPane(historyReservationPane);
        headerText.setText("Reservations History");
        backPane.setVisible(true);
        lastClickedPane = LastClickedPane.chooseReservations;
        reservationHistoryView();
    }
    public void reservationHistoryView() {
        // to prevent duplication
        ReservationTable.getColumns().clear();
        ReservationTable.getItems().clear();
        ReservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ArrayList<Reservation> reservations = owner.getReservations();

        TableColumn<Reservation, String> reservationIdCol = new TableColumn<>("Reservation ID");
        reservationIdCol.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(param.getValue().getReservationID())));

        TableColumn<Reservation, String> vehicleTypeCol = new TableColumn<>("Vehicle Type");
        vehicleTypeCol.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(param.getValue().getVehicleType())));

        TableColumn<Reservation, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(String.format("%.2f", param.getValue().getAmount()))));

        // To Show The Start Date , End Datefad
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // Start Date Column
        TableColumn<Reservation, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getReservationDate().format(formatter))
        );

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(param ->
                new SimpleStringProperty((param.getValue().isActive() ? "Confirmed" : "Canceled"))
        );

        ReservationTable.getColumns().addAll(reservationIdCol, vehicleTypeCol, amountCol, dateCol, statusCol);
        ReservationTable.getItems().addAll(reservations);
    }
    // Cancel Reservations
    public void cancelReservation() {
        // Example: Prompt user to select a spot to remove
        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, getReservationIds());
        dialog.setTitle("Remove Reservation");
        dialog.setHeaderText("Select a Reservation ID to remove:");
        dialog.setContentText("Reservation ID:");

        dialog.getDialogPane().getStyleClass().add("owner-alert");
        URL cssFile = getClass().getResource("/css/adminStyle.css");
        if (cssFile != null) {
            dialog.getDialogPane().getStylesheets().add(cssFile.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }

        dialog.showAndWait().ifPresent(reservationIDString -> {
            if (reservationIDString.isEmpty())
                return;
            int reservationID = Integer.parseInt(reservationIDString);
            owner.cancelReservation(reservationID);
            reservationHistoryView();
        });
    }
    public ArrayList<String> getReservationIds() {
        ArrayList<String> ids = new ArrayList<>();
        for (Reservation reservation : owner.getReservations()) {
            if (reservation.isActive())
                ids.add(String.valueOf(reservation.getReservationID()));
        }
        return ids;
    }
    // Update Reservation
    Reservation selectedReservation;
    public void updateReservationButton() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, getReservationIds());
        dialog.setTitle("Update Reservation");
        dialog.setHeaderText("Select a Reservation ID to Update:");
        dialog.setContentText("Reservation ID:");

        dialog.getDialogPane().getStyleClass().add("owner-alert");
        URL cssFile = getClass().getResource("/css/adminStyle.css");
        if (cssFile != null) {
            dialog.getDialogPane().getStylesheets().add(cssFile.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }

        dialog.showAndWait().ifPresent(reservationIDString -> {
            if (reservationIDString.isEmpty())
                return;
            int reservationID = Integer.parseInt(reservationIDString);
            selectedReservation = SystemManager.getReservation(reservationID);
            goToUpdateReservation();
        });
    }
    public void goToUpdateReservation() {
        switchToPane(makeReservationPane);
        headerText.setText("Update Reservation");
        backPane.setVisible(true);
        lastClickedPane = LastClickedPane.reservationHistory;
        updateReservationView();
    }
    public void updateReservationView() {
        gridPaneSlots.getChildren().clear(); // Clear all previous content
        selectedSlots = new ArrayList<>();

        ArrayList<Slot> slots = SystemManager.getSlotsByType(selectedReservation.getVehicleType());
        int columns = 3; // Slots per row

        // Create a label to display the Total Amount
        Label totalAmountLabel = new Label("Total Amount: $0.0");
        totalAmountLabel.getStyleClass().add("make_reservation_label");

        Label oldReservationAmount = new Label("Old Res Amount: $" + String.format("%.2f", selectedReservation.getAmount()));
        oldReservationAmount.getStyleClass().add("make_reservation_label");

        gridPaneSlots.add(totalAmountLabel, 0, 0, columns, 1); // Add the total amount label above the slots
        gridPaneSlots.add(oldReservationAmount, 2, 0, columns, 1); // Add the total amount label above the slots


        for (int i = 0; i < slots.size(); ++i) {
            Slot slot = slots.get(i);
            VBox slotBox = createSlotBox(slot, totalAmountLabel);

            slotBox.setAlignment(Pos.CENTER); // Centers content within the VBox
            slotBox.setSpacing(10);
            slotBox.getStyleClass().add("slot-box");

            int row = i / columns;
            int col = i % columns;
            gridPaneSlots.add(slotBox, col, row + 1); // Offset by 1 to leave space for the total amount label
        }

        // Create a button to confirm reservation
        Button makeReservationButton = new Button("Update Reservation");
        makeReservationButton.getStyleClass().add("make-reservation-btn");
        makeReservationButton.setOnAction(event -> {
            if (selectedSlots.isEmpty()) {
                showAlert("Error", "No Slots Selected", "Select Slots To Reserve");
                return;
            }
            updateReservationConfirm(totalAmountLabel.getText());
        });

        gridPaneSlots.setHgap(18); // Horizontal gap between columns
        gridPaneSlots.setVgap(15); // Vertical gap between rows

        // Add the button below the ScrollPane
        HBox buttonContainer = new HBox(10, makeReservationButton);
        buttonContainer.setAlignment(Pos.CENTER);
        gridPaneSlots.add(buttonContainer, 0, slots.size() / 4 + 2, 4, 1); // Add below grid
    }
    private void updateReservationConfirm(String totalAmountText)   {
        double amount = Payment.totalAmount(owner.getPayment(), selectedSlots);
        double oldReservationAmount = selectedReservation.getAmount();
        double balance = owner.getPayment().getBalance();

        double totalAmount = amount - oldReservationAmount;
        // Proceed with reservation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reservation Confirmation");
        alert.setHeaderText("Reservation Details");
        alert.setContentText(String.format("Amount: $%.2f\nOld Reservation Amount: $%.2f\nFinal Amount: $%.2f\n",
                amount, oldReservationAmount, totalAmount));

        alert.getDialogPane().getStyleClass().add("dialog-content");
        URL cssFile = getClass().getResource("/css/adminStyle.css");
        if (cssFile != null) {
            alert.getDialogPane().getStylesheets().add(cssFile.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }

        ButtonType okButton = ButtonType.OK;
        ButtonType cancelButton = ButtonType.CANCEL;
        alert.getButtonTypes().setAll(okButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == okButton) {
                if (balance < totalAmount) {
                    // Handle insufficient balance
                    Alert newAlert = new Alert(Alert.AlertType.ERROR, "Insufficient balance. Please add more funds");
                    newAlert.getDialogPane().getStyleClass().add("dialog-content"); // Apply dialog content CSS
                    newAlert.showAndWait();
                    return;
                }
                owner.updateReservation(selectedReservation, selectedSlots);
                Alert newAlert = new Alert(Alert.AlertType.INFORMATION, "Reservations Made Successfully");
                newAlert.getDialogPane().getStyleClass().add("dialog-content"); // Apply dialog content CSS
                newAlert.showAndWait();
                goToReservationHistory();
            }
        });
    }

    // Deposit Page
    public void goToDepositPage() {
        switchToPane(depositPane);
        headerText.setText("Deposit Management");
        depositView();
    }
    public void depositView() {
        try {
            double balance = owner.getPayment().getBalance();
            balanceLabel.setText("Current Balance: $" + String.format("%.2f", balance));

            depositAmountField.clear();
            depositButton.setOnAction(event -> {
                String depositAmountText = depositAmountField.getText().trim();
                if (depositAmountText.isEmpty()) {
                    showAlert("Message","Error", "Deposit amount cannot be empty.");
                    return;
                }
                try {
                    double depositAmount = Double.parseDouble(depositAmountText);
                    if (depositAmount <= 0) {
                        showAlert("Message","Error", "Deposit amount must be greater than zero.");
                        return;
                    }
                    owner.getPayment().deposit(depositAmount);
                    balanceLabel.setText("Current Balance: $" + String.format("%.2f", owner.getPayment().getBalance()));
                    showAlert("Message","Success", "Funds added successfully.");
                    depositAmountField.clear();
                } catch (NumberFormatException e) {
                    showAlert("Message","Error", "Invalid amount entered. Please enter a valid number.");
                }
            });
        } catch (Exception e) {
            showAlert("Message","Error", "An error occurred while loading the deposit view.");
        }
    }

    // Feedback Page
    private int messageCounter = 0; // Track the latest message
    private void displayTemporaryMessage(Label msg, String message) {
        messageCounter++;
        int currentMessageId = messageCounter;

        msg.setText(message);
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(3.5),
                event -> {
                    // Only clear if this timeline is the most recent one
                    if (currentMessageId == messageCounter) {
                        msg.setText("");
                    }
                }
        ));
        timeline.setCycleCount(1);
        timeline.play();
    }
    public void goToFeedbackPage() {
        headerText.setText("Make Feedback");
        ReservationID_field.clear();;
        feedbackMessage.clear();;
        RatingBar.setRating(2);

        switchToPane(feedbackPane);
    }
    public void feedbackSubmitButton() {
        try {
            String reservationIdText = ReservationID_field.getText();
            String message = feedbackMessage.getText();
            double rate = RatingBar.getRating();

            if (reservationIdText.isEmpty()) {
                displayTemporaryMessage(submit_msg, "Reservation ID cannot be empty!");
                return;
            }
            if (message.isEmpty()) {
                displayTemporaryMessage(submit_msg, "Feedback message cannot be empty!");
                return;
            }

            int reservationID = Integer.parseInt(reservationIdText);
            if (!owner.isReservationExist(reservationID)) {
                displayTemporaryMessage(submit_msg, "There Is No reservation with that ID");
                return;
            }

            owner.makeFeedback(owner.getOwnerID(), reservationID, rate, message);
            showAlert("Message", "Success", "Feedback has been submitted successfully!");
            goToFeedbackPage();
        }  catch (Exception e) {
            displayTemporaryMessage(submit_msg, "Reservation ID Must be a number");
        }
    }

    // Update Owner Data Page
    public void goToUpdateDataPage(){
        switchToPane(updateDataPane);
        headerText.setText("Update Data");
        UpdateDataView();
    }
    public void UpdateDataView() {
        // Prevent duplication
        updateDataPane.getChildren().clear();

        // Username
        Label userNameLabel = new Label("Username:");
        userNameLabel.getStyleClass().add("form-label");

        TextField userNameField = new TextField(owner.getUserName());
        userNameField.setPromptText("Enter new username");
        userNameField.getStyleClass().add("text-fieldd");

        // My Password
        Label myPasswordLabel = new Label("Password:");
        myPasswordLabel.getStyleClass().add("form-label");

        PasswordField myPasswordField = new PasswordField();
        myPasswordField.setPromptText("Enter your Password");
        myPasswordField.getStyleClass().add("text-fieldd");

        // Red Star Label
        Label redStar = new Label("*");
        redStar.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-font-weight: bold;");

        // HBox to combine PasswordField and Red Star
        HBox myPasswordFieldBox = new HBox();
        myPasswordFieldBox.getChildren().addAll(myPasswordField, redStar);
        myPasswordFieldBox.setAlignment(Pos.CENTER_LEFT);
        myPasswordFieldBox.setSpacing(3); // Adjust spacing between the field and star

        // Confirm Password
        Label newPasswordLabel = new Label("New Password :");
        newPasswordLabel.getStyleClass().add("form-label");

        // New Password
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");
        newPasswordField.getStyleClass().add("text-fieldd");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        confirmPasswordField.getStyleClass().add("text-fieldd");

        // License Number
        Label licenseLabel = new Label("License Number:");
        licenseLabel.getStyleClass().add("form-label");

        TextField licenseField = new TextField(owner.getLicenseNumber());
        licenseField.setPromptText("Enter license number");
        licenseField.getStyleClass().add("text-fieldd");

        // Update Profile Button
        Button updateButton = new Button("Update Profile");
        updateButton.setOnAction(event -> handleUpdateProfile(
                userNameField, myPasswordField, newPasswordField, confirmPasswordField, licenseField));
        updateButton.getStyleClass().add("make-reservation-btn");

        // Form Layout
        VBox updateForm = new VBox(15,
                userNameLabel, userNameField,
                myPasswordLabel, myPasswordFieldBox, newPasswordLabel,
                newPasswordField, confirmPasswordField,
                licenseLabel, licenseField,
                updateButton);
        updateForm.setAlignment(Pos.CENTER);
        updateForm.setPadding(new Insets(20));
        updateForm.getStyleClass().add("form-container");

        // Adjust positioning
        updateForm.setTranslateX(updateDataPane.getWidth() / 2 - updateForm.getWidth() / 2 - 125);
        updateForm.setTranslateY(20);

        // Center Pane
        StackPane centerPane = new StackPane(updateForm);
        centerPane.setAlignment(Pos.TOP_CENTER);
        updateDataPane.getChildren().add(centerPane);
    }
    private void handleUpdateProfile(TextField userNameField, PasswordField myPasswordField,
                                     PasswordField passwordField, PasswordField confirmPasswordField, TextField licenseField) {
        String newUserName = userNameField.getText().trim();
        String myPassword = myPasswordField.getText().trim();
        String newPassword = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String newLicenseNumber = licenseField.getText().trim();

        // Username Check
        // Empty
        if (newUserName.isEmpty()) {
            showAlert("Message", "Error", "Username Can't Be Empty!");
            return;
        }
        // Check If It's Taken
        if (!newUserName.equals(owner.getUserName()) && Owner.isOwnerExist(newUserName)) {
            showAlert("Message", "Error", "Username Already Exists.!");
            return;
        }
        // He Has To Enter His Old Password
        if (myPassword.isEmpty()) {
            showAlert("Message", "Error", "Please Enter Your Password To Update!");
            return;
        }
        if (!myPassword.equals(owner.getPassword())) {
            showAlert("Message", "Error", "Wrong Password!");
            return;
        }
        // Check If He Want To Update His Password
        if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                showAlert("Message", "Error", "Passwords do not match.");
                return;
            }
            if (newPassword.length() < 6) {
                showAlert("Message", "Error", "Password must be at least 6 characters long!");
                return;
            }
        }
        // Check That License Number Is Not Empty
        if (newLicenseNumber.isEmpty()) {
            showAlert("Message", "Error", "License Number Can't Be Empty!");
            return;
        }
        owner.setUserName(newUserName);
        if (!newPassword.isEmpty())
            owner.setPassword(newPassword);
        owner.setLicenseNumber(newLicenseNumber);
        showAlert("Message", "Success", "Profile updated successfully.");
        UpdateDataView();
    }

    // Logout
    public void logoutButton(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/parking/menuFXML.fxml")));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Message","Error", "Failed to load the login page xfml.");
        }
    }
}