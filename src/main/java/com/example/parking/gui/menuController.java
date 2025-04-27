package com.example.parking.gui;

import com.example.parking.*;
import javafx.animation.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.io.*;
import java.net.*;
import javafx.util.*;
import java.util.*;

public class menuController {
    private static Owner currentOwner;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label login_msg;
    @FXML
    private Pane buttonHolder;
    @FXML
    private AnchorPane loginPage, signUpPage;

    @FXML
    private TextField usernameField2, licenseNumberField, licensePlateField, balanceField;
    @FXML
    private PasswordField passwordField2;
    @FXML
    private ComboBox<String> vehicleTypeComboBox;
    @FXML
    private ListView<String> vehicleListView;
    @FXML
    private Label signup_msg;

    // ObservableList To Store Vehicles To Print Them
    private ObservableList<String> vehiclesString = FXCollections.observableArrayList();
    // To Store Vehicles Into The Owner
    private ArrayList<Vehicle> vehicles = new ArrayList<>();

    @FXML
    private CheckBox showPasswordBox;
    @FXML
    private TextField PasswordVisible;

    private Pane currentPane;
    private AnchorPane currentAnchorPane;
    @FXML
    void initialize() {
        currentPane = new Pane();
        currentAnchorPane = new AnchorPane();

        buttonHolder.setVisible(true);
        loginPage.setVisible(false);
        signUpPage.setVisible(false);
        PasswordVisible.setVisible(false);
        vehicleListView.setItems(vehiclesString);

        PasswordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        showPasswordBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                PasswordVisible.setVisible(true);
                passwordField.setVisible(false);
            } else {
                PasswordVisible.setVisible(false);
                passwordField.setVisible(true);
            }
        });
    }

    private void switchToPane(Pane pane) {
        currentPane.setVisible(false);
        currentAnchorPane.setVisible(false);

        pane.setVisible(true);
        currentPane = pane;
    }
    private void switchToPane(AnchorPane pane) {
        currentPane.setVisible(false);
        currentAnchorPane.setVisible(false);

        pane.setVisible(true);
        currentAnchorPane = pane;
    }
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
    private void resetFields() {
        // Clear text fields
        usernameField.clear();
        passwordField.clear();
        usernameField2.clear();
        passwordField2.clear();
        licenseNumberField.clear();
        licensePlateField.clear();
        balanceField.clear();

        // Reset combo boxes
        vehicleTypeComboBox.setValue(null);
        // Clear list view
        vehicleListView.getItems().clear();
        // Reset labels
        login_msg.setText("");
        signup_msg.setText("");

        showPasswordBox.setSelected(false);
        PasswordVisible.clear();
    }
    // Go To Menu
    public void goToMenu() {
        resetFields();
        switchToPane(buttonHolder);
    }

    // Login Page
    public void goToLoginPage() {
        switchToPane(loginPage);
    }
    public void loginButton(ActionEvent event) throws IOException {
        // check the User_name And Password ->
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation: Check if username or password are empty
        if (username.isEmpty() || password.isEmpty()) {
            displayTemporaryMessage(login_msg, "fill in both username and password");
            return;
        }

        // Go to Admin Page
        if (username.equals("admin") && password.equals("admin")) {
            showAlert("Message", "Logged Successfully","Welcome Admin!");
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/parking/AdminPageFXML.fxml")));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ( ((Node) event.getSource()).getScene().getWindow());
            stage.setScene(scene);
            stage.show();
            return;
        }

        // User Page
        if (Owner.isOwnerExist(username, password)) {
            // Set The Current Owner
            Owner owner = Owner.getOwnerByUsername(username);
            setCurrentOwner(owner);
            showAlert("Message", "Logged Successfully","Welcome " + owner.getUserName() + "!");
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/parking/UserPageFXML.fxml")));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ( ((Node) event.getSource()).getScene().getWindow());
            stage.setScene(scene);
            stage.show();
            return;
        }

        // Not Found
        displayTemporaryMessage(login_msg, "Wrong Username/Password");
    }

    // Sign Up Page
    public void goToSignUpPage() {
        switchToPane(signUpPage);
    }
    @FXML
    public void handleAddVehicle() {
        // Get input values
        String vehicleType = vehicleTypeComboBox.getValue();
        String licensePlate = licensePlateField.getText().trim();

        // Validate inputs
        if (vehicleType == null || vehicleType.isEmpty()) {
            displayTemporaryMessage(signup_msg, "Please select a vehicle type");
            return;
        }
        if (licensePlate.isEmpty()) {
            displayTemporaryMessage(signup_msg, "Please enter a license plate.");
            return;
        }

        if (vehiclesString.size() >= 3) {
            displayTemporaryMessage(signup_msg, "You can only add up to 3 vehicles.");
            return;
        }

        if (!Vehicle.isValidLicensePlate(licensePlate)) {
            displayTemporaryMessage(signup_msg, "Please enter a valid license plate.");
            return;
        }

        // Add the vehicle to the list
        String vehicleInfo = "Vehicle: " + vehicleType + ", Plate: " + licensePlate;

        Vehicle vehicle = new Vehicle(VehicleType.valueOf(vehicleType), licensePlate);

        vehicles.add(vehicle);
        vehiclesString.add(vehicleInfo);

        // Clear the input fields
        licensePlateField.clear();
        vehicleTypeComboBox.setValue(null);
    }
    @FXML
    public void handleRemoveVehicle() {
        // Get the selected vehicle
        int selectedIndex = vehicleListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1) {
            displayTemporaryMessage(signup_msg, "Select A Vehicle To Remove");
            return;
        }

        // Remove the selected vehicle
        vehiclesString.remove(selectedIndex);
        vehicles.remove(selectedIndex);
    }
    // Sign Up Button
    public void signUpButton(ActionEvent event) {
        // Get input values
        String userName = usernameField2.getText().trim();
        String password = passwordField2.getText().trim();
        String licenseNumber = licenseNumberField.getText().trim();
        String balanceText = balanceField.getText().trim();

        if (userName.isEmpty() || password.isEmpty() || licenseNumber.isEmpty() || balanceText.isEmpty()) {
            displayTemporaryMessage(signup_msg, "All fields must be filled");
            return;
        }

        // if the Username Taken Error
        if (!Owner.userNameValid(userName)) {
            displayTemporaryMessage(signup_msg, "This Username Is Taken");
            return;
        }

        if(!Owner.passwordValid(password)) {
            displayTemporaryMessage(signup_msg, "Password must be at least 6 characters long!");
            return;
        }

        if (!Owner.balanceValid(balanceText)) {
            displayTemporaryMessage(signup_msg, "Enter A Valid Balance");
            return;
        }

        double balanceDouble = Double.parseDouble(balanceText);

        // Register Successfully
        showAlert("Message","SignUp Successfully!", "Welcome to our garage <3");
        Owner.addOwner(userName, password, licenseNumber, vehicles, balanceDouble);

        // Go LoginPage
        goToMenu();
    }

    static public void setCurrentOwner(Owner owner) {
        currentOwner = owner;
    }
    static public Owner getCurrentOwner() {
        return currentOwner;
    }

    private void showAlert(String title,String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        alert.getDialogPane().getStyleClass().add("login-alert");

        URL cssFile = getClass().getResource("/css/style.css");
        if (cssFile != null) {
            alert.getDialogPane().getStylesheets().add(cssFile.toExternalForm());
        } else {
            System.out.println("CSS file not found.");
        }

        alert.showAndWait();
    }
}
