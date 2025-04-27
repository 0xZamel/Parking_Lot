package com.example.parking.gui;

import com.example.parking.spot.*;
import com.example.parking.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.*;
import org.controlsfx.control.*;
import java.io.*;
import java.net.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.*;
import java.util.*;
import java.util.function.*;

public class AdminPageController {
    enum LastClickedPane {
        ReservationPane,
        SpotsPane
    }

    private Admin admin;
    private VehicleType currentVehicleType;
    private LastClickedPane lastClickedPane;

    private ScrollPane lastScrollPane = new ScrollPane();
    private Pane lastPane = new Pane();
    @FXML
    private Label headerText;

    @FXML
    private Pane homePane, chooseCarPane, ownersPane, reservationPane, feedbackPane, backPane, spotsPane;

    @FXML
    private TabPane SpotTabPane;

    @FXML
    private Rating AvgRatingBar;

    @FXML
    private Label avgFeedback;

    @FXML
    private TableView<Feedback> FeedbackTable;

    @FXML
    private TableView<Owner>OwnersTable;

    @FXML
    private TableView<Reservation> ReservationTable;

    @FXML
    private Label totalAmountLabel, totalSpotsLabel, totalOwnersLabel, totalReservationsLabel;


    public void initialize() {
        admin = new Admin();
        homePane.setVisible(false);
        chooseCarPane.setVisible(false);
        spotsPane.setVisible(false);
        backPane.setVisible(false);
        ownersPane.setVisible(false);
        reservationPane.setVisible(false);
        feedbackPane.setVisible(false);

        switchToPane(homePane);
        homeView();
    }

    private void switchToPane(Pane pane) {
        backPane.setVisible(false);

        lastPane.setVisible(false);
        lastScrollPane.setVisible(false);
        pane.setVisible(true);
        lastPane = pane;
    }
    private void showAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        alert.getDialogPane().getStyleClass().add("dialog-pane");

        URL cssFile = getClass().getResource("/css/adminStyle.css");
        if (cssFile != null) {
            alert.getDialogPane().getStylesheets().add(cssFile.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }

        alert.showAndWait();
    }
    public void handleBackButton() {
        if (lastClickedPane == LastClickedPane.SpotsPane) {
            goToChooseCarPageFromSpots();
        }
        else {
            goToChooseCarPageFromReservations();
        }
    }

    // Left Buttons
    // Home Page
    @FXML
    public void goToHomePage() {
        headerText.setText("Welcome To Admin Dashboard");
        switchToPane(homePane);
        homeView();
    }
    public void homeView() {
        int totalSpots = SystemManager.getTotalSpots();
        int totalOwners = SystemManager.getTotalOwners();
        int totalReservations = SystemManager.getTotalAvailableReservations();


        totalSpotsLabel.setText("Spots\n" + totalSpots);
        totalSpotsLabel.getStyleClass().add("number");

        totalOwnersLabel.setText("Owners\n" + totalOwners);
        totalOwnersLabel.getStyleClass().add("number");

        totalReservationsLabel.setText("Reservations\n" + totalReservations);
        totalReservationsLabel.getStyleClass().add("number");
    }

    // Owners Page
    public void goToOwnersPage() {
        headerText.setText("Owners");
        switchToPane(ownersPane);
        OwnersView();
    }
    // Define the custom cell renderer
    private TableColumn<Owner, String> createMultiRowColumn(String columnName, Function<Owner, List<String>> dataExtractor) {
        TableColumn<Owner, String> column = new TableColumn<>(columnName);

        column.setCellFactory(param -> new TableCell<Owner, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Get the data for this cell
                    Owner owner = getTableRow().getItem();
                    List<String> data = dataExtractor.apply(owner);

                    // Create a VBox to hold the data as multiple rows
                    VBox vbox = new VBox();
                    for (String detail : data) {
                        Label label = new Label(detail);
                        vbox.getChildren().add(label);
                    }

                    setGraphic(vbox);
                    setText(null); // Clear text to ensure only the graphic is displayed
                }
            }
        });

        return column;
    }
    public void removeOwner() {
        // Example: Prompt user to select a spot to remove
        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, getOwnersIds());
        dialog.setTitle("Remove Owner");
        dialog.setHeaderText("Select a Owner ID to remove:");
        dialog.setContentText("Owner ID:");

        dialog.getDialogPane().getStyleClass().add("owner-alert");

        URL cssFile = getClass().getResource("/css/adminStyle.css");
        if (cssFile != null) {
            dialog.getDialogPane().getStylesheets().add(cssFile.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }

        dialog.showAndWait().ifPresent(ownerID -> {
            admin.removeOwner(Integer.parseInt(ownerID));
            OwnersView();
        });
    }
    public ArrayList<String> getOwnersIds() {
        ArrayList<String> ids = new ArrayList<>();
        ArrayList<Owner> owners = SystemManager.getOwners();

        for (Owner owner : owners) {
            ids.add(String.valueOf(owner.getOwnerID()));
        }
        return ids;
    }
    public void OwnersView() {
        OwnersTable.setEditable(true);
        // To prevent duplication
        OwnersTable.getColumns().clear();
        OwnersTable.getItems().clear();

        OwnersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Fetch the owners' data
        ArrayList<Owner> owners = SystemManager.getOwners();

        // Define main columns
        TableColumn<Owner, String> ownerIdCol = new TableColumn<>("Owner ID");
        ownerIdCol.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(param.getValue().getOwnerID())));


        TableColumn<Owner, String> userNameCol = new TableColumn<>("Username");
        userNameCol.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getUserName()));

        TableColumn<Owner, String> passwordCol = new TableColumn<>("Password");
        passwordCol.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getPassword()));

        TableColumn<Owner, String> licenseCol = new TableColumn<>("License Number");
        licenseCol.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getLicenseNumber()));

        // Custom multi-row column for Vehicles
        TableColumn<Owner, String> vehiclesCol = createMultiRowColumn("Vehicles", owner -> {
            ArrayList<Vehicle> vehicles = owner.getVehicles();
            return vehicles.stream()
                    .map(vehicle -> vehicle.getType() + " (" + vehicle.getLicensePlate() + ")")
                    .toList();
        });

        // Define main columns
        TableColumn<Owner, String> balanceCol = new TableColumn<>("Balance");
        balanceCol.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(param.getValue().getPayment().getBalance())));

        // Define main columns
        TableColumn<Owner, String> penaltyCol = new TableColumn<>("Penalty");
        penaltyCol.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(param.getValue().getPayment().getPenalty())));

        // Add columns to the main table
        OwnersTable.getColumns().addAll(ownerIdCol, userNameCol, passwordCol, licenseCol, vehiclesCol, balanceCol, penaltyCol);
        OwnersTable.getItems().addAll(owners);
    }

    // Spots Page
    public void goToChooseCarPageFromSpots() {
        headerText.setText("Choose Vehicle Spots");
        lastClickedPane = LastClickedPane.SpotsPane;
        switchToPane(chooseCarPane);
    }
    public void goToSpotsPage() {
        switchToPane(spotsPane);
        backPane.setVisible(true);
        refreshSpotView();
    }
    public void addSpot() {
        // Show a confirmation dialog
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Add New Spot");
        confirmationAlert.setHeaderText("Are you sure you want to add a new spot?");
        confirmationAlert.setContentText("This will create a new spot with ID: " + SystemManager.nextSpotID);

        confirmationAlert.getDialogPane().getStyleClass().add("dialog-content");

        URL cssFile = getClass().getResource("/css/adminStyle.css");
        if (cssFile != null) {
            confirmationAlert.getDialogPane().getStylesheets().add(cssFile.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }

        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Give The Spot Type
                admin.addSpot(currentVehicleType); // Add new spot with the generated ID
                refreshSpotView(); // Refresh the UI
            }
        });
    }
    public void removeSpot() {
        // Example: Prompt user to select a spot to remove
        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, getSpotIds());
        dialog.setTitle("Remove Spot");
        dialog.setHeaderText("Select a Spot ID to remove:");
        dialog.setContentText("Spot ID:");

        dialog.getDialogPane().getStyleClass().add("owner-alert");

        URL cssFile = getClass().getResource("/css/adminStyle.css");
        if (cssFile != null) {
            dialog.getDialogPane().getStylesheets().add(cssFile.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }
        dialog.showAndWait().ifPresent(spotIdString -> {
            int spotId = Integer.parseInt(spotIdString);
            admin.removeSpot(spotId);
            refreshSpotView();
        });
    }
    public List<String> getSpotIds() {
        ArrayList<String> ids = new ArrayList<>();
        ArrayList<Spot> spots = SystemManager.getSpotsByType(currentVehicleType);

        for (Spot spot : spots) {
            ids.add(String.valueOf(spot.getSpotID()));
        }
        return ids;
    }
    public void addSlot() {
        Tab selectedTab = SpotTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null)
            return;

        String selectedTabText = selectedTab.getText(); // Get the tab text (e.g., "Spot ID: 1")
        int currentSpotID = Integer.parseInt(selectedTabText.replace("Spot ID: ", ""));

        ArrayList<Slot> slots = SystemManager.getSlotsBySpotID(currentSpotID);
        // Step 2: Show custom dialog to input start and end LocalDateTime
        Dialog<Pair<LocalDateTime, LocalDateTime>> dateTimeDialog = createDateTimeInputDialog(slots);
        dateTimeDialog.showAndWait().ifPresent(dateTimes -> {
            // Step 3: Add the new slot
            LocalDateTime startDateTime = dateTimes.getKey();
            LocalDateTime endDateTime = dateTimes.getValue();

            admin.addSlot(currentSpotID, startDateTime, endDateTime); // Add the slot
            refreshSpotView(); // Refresh the view
        });
    }

    private Dialog<Pair<LocalDateTime, LocalDateTime>> createDateTimeInputDialog(ArrayList<Slot> existingSlots) {
        Dialog<Pair<LocalDateTime, LocalDateTime>> dialog = new Dialog<>();
        dialog.setTitle("Input Slot Date/Time");
        dialog.setHeaderText("Enter the start and end date/time for the new slot:");

        // Add buttons
        ButtonType addButtonType = new ButtonType("Add Slot", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create input fields
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        TextField startTimeField = new TextField();
        startTimeField.setPromptText("Start Time (HH:mm)");

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        TextField endTimeField = new TextField();
        endTimeField.setPromptText("End Time (HH:mm)");

        // Apply style to TextFields
        startTimeField.getStyleClass().add("text-fieldd");
        endTimeField.getStyleClass().add("text-fieldd");

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Start Date:"), 0, 0);
        grid.add(startDatePicker, 1, 0);
        grid.add(new Label("Start Time:"), 0, 1);
        grid.add(startTimeField, 1, 1);
        grid.add(new Label("End Date:"), 0, 2);
        grid.add(endDatePicker, 1, 2);
        grid.add(new Label("End Time:"), 0, 3);
        grid.add(endTimeField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Validate input fields before closing the dialog
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                LocalDate startDate = startDatePicker.getValue();
                LocalTime startTime = LocalTime.parse(startTimeField.getText());
                LocalDate endDate = endDatePicker.getValue();
                LocalTime endTime = LocalTime.parse(endTimeField.getText());

                LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
                LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

                if (startDate == null || endDate == null || startTime == null || endTime == null) {
                    showAlert("Message","Error", "All fields must be filled.");
                    event.consume(); // Prevent dialog from closing
                } else if (startDate.isAfter(endDate) ||
                        (startDate.isEqual(endDate) && startTime.isAfter(endTime))) {
                    showAlert("Message","Error", "Start date/time must be before end date/time.");
                    event.consume(); // Prevent dialog from closing
                } else if (startDate.isBefore(LocalDate.now())) {
                    showAlert("Message","Error", "Start date/time must be in the future.");
                    event.consume(); // Prevent dialog from closing
                }
                else if ((double) Duration.between(startDateTime, endDateTime).toHours() < 1) {
                    showAlert("Message","Error", "Start date/time must be at least one hour.");
                    event.consume(); // Prevent dialog from closing
                }
                else {
                    // Check for overlaps with existing slots
                    LocalDateTime newStart = LocalDateTime.of(startDate, startTime);
                    LocalDateTime newEnd = LocalDateTime.of(endDate, endTime);
                    boolean overlap = false;

                    for (Slot existingSlot : existingSlots) {
                        LocalDateTime existingStart = existingSlot.getStartDate();
                        LocalDateTime existingEnd = existingSlot.getEndDate();

                        // If the new slot's start time is before an existing slot's end time,
                        // and the new slot's end time is after the existing slot's start time, it's an overlap

                        if ((newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart))) {
                            overlap = true;
                            break;
                        }
                    }

                    if (overlap) {
                        showAlert("Message","Error", "The selected time slot overlaps with an existing slot.");
                        event.consume(); // Prevent dialog from closing
                    }
                }
            } catch (Exception e) {
                showAlert("Message","Error", "Invalid time format. Use HH:mm.");
                event.consume(); // Prevent dialog from closing
            }
        });

        // Convert the result to a Pair of LocalDateTime objects
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                LocalDate startDate = startDatePicker.getValue();
                LocalTime startTime = LocalTime.parse(startTimeField.getText());
                LocalDate endDate = endDatePicker.getValue();
                LocalTime endTime = LocalTime.parse(endTimeField.getText());

                return new Pair<>(
                        LocalDateTime.of(startDate, startTime),
                        LocalDateTime.of(endDate, endTime)
                );
            }
            return null;
        });

        URL cssFile = getClass().getResource("/css/adminStyle.css");
        if (cssFile != null) {
            dialog.getDialogPane().getStylesheets().add(cssFile.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }
        return dialog;
    }
    public void removeSlot() {
        Tab selectedTab = SpotTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null)
            return;

        String selectedTabText = selectedTab.getText(); // Get the tab text (e.g., "Spot ID: 1")
        int selectedSpotId = Integer.parseInt(selectedTabText.replace("Spot ID: ", ""));

        // Step 2: Get the Slot IDs for the selected Spot
        List<String> slotIds = getSlotIdsForSpot(selectedSpotId);
        // Step 3: Prompt the user to select a Slot ID
        ChoiceDialog<String> slotDialog = new ChoiceDialog<>(null, slotIds);
        slotDialog.setTitle("Remove Slot");
        slotDialog.setHeaderText("Select a Slot ID to remove:");
        slotDialog.setContentText("Slot ID:");

        slotDialog.getDialogPane().getStyleClass().add("owner-alert");
        URL cssFilee = getClass().getResource("/css/adminStyle.css");
        if (cssFilee != null) {
            slotDialog.getDialogPane().getStylesheets().add(cssFilee.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }

        slotDialog.showAndWait().ifPresent(slotId -> {
            // Step 4: Remove the selected Slot
            admin.removeSlot(Integer.parseInt(slotId));
            refreshSpotView(); // Refresh the view of slots for the spot
        });
    }
    // Helper method to get the Slot IDs for a given Spot ID
    public List<String> getSlotIdsForSpot(int spotId) {
        ArrayList<String> slotIds = new ArrayList<>();
        ArrayList<Slot> slots = SystemManager.getSlotsBySpotID(spotId);
        for (Slot slot : slots) { // Assuming Spot has a method getSlots()
            slotIds.add(String.valueOf(slot.getSlotID()));
        }
        return slotIds;
    }

    public void refreshSpotView() {
        SpotTabPane.getTabs().clear(); // Clear the container
        SpotTabPane.getStyleClass().add("tab-pane");

        URL cssFile = getClass().getResource("/css/adminStyle.css");
        if (cssFile != null) {
            SpotTabPane.getStylesheets().add(cssFile.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }

        ArrayList<Spot> spots = SystemManager.getSpotsByType(currentVehicleType);
        for (Spot spot : spots) {
            // Create a new Tab for each spot
            Tab tab = new Tab();
            tab.setText("Spot ID: " + spot.getSpotID());
            tab.getStyleClass().add("tab");

            // Create the TableView for slots
            TableView<Slot> slotTable = new TableView<>();
            slotTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            slotTable.getStyleClass().add("AdminStacksTables");

            // Define columns
            TableColumn<Slot, String> slotIdCol = new TableColumn<>("Slot ID");
            slotIdCol.setCellValueFactory(param ->
                    new SimpleStringProperty(String.valueOf(param.getValue().getSlotID())));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            TableColumn<Slot, String> startDateCol = new TableColumn<>("Start Date");
            startDateCol.setCellValueFactory(param ->
                    new SimpleStringProperty(param.getValue().getStartDate().format(formatter))
            );

            TableColumn<Slot, String> endDateCol = new TableColumn<>("End Date");
            endDateCol.setCellValueFactory(param ->
                    new SimpleStringProperty(param.getValue().getEndDate().format(formatter))
            );

            TableColumn<Slot, Boolean> availabilityCol = new TableColumn<>("Is Available");
            availabilityCol.setCellValueFactory(param ->
                    new SimpleBooleanProperty(param.getValue().isAvailable())
            );

            // Add columns to the TableView
            slotTable.getColumns().addAll(slotIdCol, startDateCol, endDateCol, availabilityCol);

            // Populate the TableView with slots
            slotTable.getItems().addAll(spot.getSlots());

            // Set the TableView as the content of the Tab
            tab.setContent(slotTable);

            // Add the Tab to the TabPane
            SpotTabPane.getTabs().add(tab);
        }
    }

    public void carSelection() {
        currentVehicleType = VehicleType.Car;
        if (lastClickedPane == LastClickedPane.SpotsPane) {
            headerText.setText("Car Spots");
            goToSpotsPage();
        }
        else {
            headerText.setText("Car Reservations");
            goToReservationPage();
        }
    }
    public void bikeSelection() {
        currentVehicleType = VehicleType.Bike;
        if (lastClickedPane == LastClickedPane.SpotsPane) {
            headerText.setText("Bike Spots");
            goToSpotsPage();
        }
        else {
            headerText.setText("Bike Reservations");
            goToReservationPage();
        }
    }
    public void truckSelection() {
        currentVehicleType = VehicleType.FourByFour;
        if (lastClickedPane == LastClickedPane.SpotsPane) {
            headerText.setText("Four By Four Spots");
            goToSpotsPage();
        }
        else {
            headerText.setText("Four By Four Reservations");
            goToReservationPage();
        }
    }

    // Reservations Page
    public void goToChooseCarPageFromReservations() {
        headerText.setText("Choose Vehicle Reservations");
        lastClickedPane = LastClickedPane.ReservationPane;
        switchToPane(chooseCarPane);
    }
    public void goToReservationPage() {
        switchToPane(reservationPane);
        backPane.setVisible(true);
        reservationView();
    }
    public void cancelReservation() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, getReservationIds());
        dialog.setTitle("Cancel Reservation");
        dialog.setHeaderText("Select a Reservation ID to cancel:");
        dialog.setContentText("Reservation ID:");
        dialog.showAndWait().ifPresent(reservationIDString -> {
            if (reservationIDString.isEmpty())
                return;
            int reservationID = Integer.parseInt(reservationIDString);
            admin.cancelReservation(reservationID);
            reservationView();
        });
    }
    public ArrayList<String> getReservationIds() {
        ArrayList<String> reservationIds = new ArrayList<>();
        ArrayList<Reservation> reservations = SystemManager.getReservationsByType(currentVehicleType);

        for (Reservation reservation : reservations) {
            if (reservation.isActive())
                reservationIds.add(String.valueOf(reservation.getReservationID()));
        }

        return reservationIds;
    }

    public void reservationView() {
        // to prevent duplication
        ReservationTable.getColumns().clear();
        ReservationTable.getItems().clear();
        ReservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ArrayList<Reservation> reservations = SystemManager.getReservationsByType(currentVehicleType);

        double totalAmount = admin.calculateTotalAmountByType(currentVehicleType);
        String formattedTotalAmount = String.format("%.2f", totalAmount);
        totalAmountLabel.setText(formattedTotalAmount);

        TableColumn<Reservation, String> ownerIdCol = new TableColumn<>("Owner ID");
        ownerIdCol.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(param.getValue().getOwnerID())));

        TableColumn<Reservation, String> reservationIdCol = new TableColumn<>("Reservation ID");
        reservationIdCol.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(param.getValue().getReservationID())));

        TableColumn<Reservation, String> slotIdCol = new TableColumn<>("Slot ID");
        slotIdCol.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(param.getValue().getSlot().getSlotID())));

        TableColumn<Reservation, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(param -> {
            double amount = param.getValue().getAmount();
            String formattedAmount = String.format("%.2f", amount);
            return new SimpleStringProperty(formattedAmount);
        });

        // To Show The Start Date , End Date
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

        ReservationTable.getColumns().addAll(ownerIdCol, reservationIdCol, slotIdCol, amountCol, dateCol, statusCol);
        ReservationTable.getItems().addAll(reservations);
    }

    // Feedback Page
    public void goToFeedbackPage() {
        headerText.setText("Feedbacks");
        switchToPane(feedbackPane);
        FeedbackView();
    }
    public void FeedbackView() {
        ArrayList<Feedback> feedbacks = SystemManager.getFeedbacks();
        FeedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        double avgFeedbackNum = 0;
        for (Feedback feedback : feedbacks) {
            avgFeedbackNum += feedback.getRate();
        }
        if (!feedbacks.isEmpty()) {
            avgFeedbackNum /= feedbacks.size();
        }
        avgFeedback.setText(String.format("%.1f", avgFeedbackNum));
        AvgRatingBar.setRating(avgFeedbackNum);
        AvgRatingBar.setDisable(true);

        // to prevent duplication
        FeedbackTable.getColumns().clear();
        FeedbackTable.getItems().clear();


        TableColumn<Feedback, String> ownerIdCol = new TableColumn<>("Owner ID");
        ownerIdCol.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(param.getValue().getOwnerID())));

        TableColumn<Feedback, String> ResIdCol = new TableColumn<>("Reservation ID");
        ResIdCol.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(param.getValue().getReservationID())));

        TableColumn<Feedback, String> RatingCol = new TableColumn<>("Rate");
        RatingCol.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(param.getValue().getRate())));

        TableColumn<Feedback, String> CommentCol = new TableColumn<>("Feedback Message");
        CommentCol.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getFeedbackMessage()));

        FeedbackTable.getColumns().addAll(ownerIdCol, ResIdCol, RatingCol, CommentCol);
        FeedbackTable.getItems().addAll(feedbacks);
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