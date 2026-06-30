package com.businessledger.ui.controller;

import com.businessledger.dto.OrderCreateDTO;
import com.businessledger.entity.Room;
import com.businessledger.service.OrderService;
import com.businessledger.service.PricingService;
import com.businessledger.service.RoomService;
import com.businessledger.util.Constants;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@Scope("prototype")
public class ReservationController {

    @FXML
    private ComboBox<Room> cmbRoom;
    @FXML
    private DatePicker dpReserveDate;
    @FXML
    private TextField txtReserveTime;
    @FXML
    private Spinner<Integer> spnPeopleCount;
    @FXML
    private TextField txtDeposit;
    @FXML
    private Label lblFeeEstimate;
    @FXML
    private Label lblError;

    private final RoomService roomService;
    private final OrderService orderService;
    private final PricingService pricingService;

    private Room preSelectedRoom;
    private boolean walkInMode;

    public ReservationController(RoomService roomService, OrderService orderService, PricingService pricingService) {
        this.roomService = roomService;
        this.orderService = orderService;
        this.pricingService = pricingService;
    }

    public void setRoom(Room room) {
        this.preSelectedRoom = room;
    }

    public void setWalkInMode(boolean walkInMode) {
        this.walkInMode = walkInMode;
    }

    @FXML
    public void initialize() {
        spnPeopleCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));

        loadRooms();

        cmbRoom.setOnAction(e -> updateFeeEstimate());
        dpReserveDate.setOnAction(e -> updateFeeEstimate());

        if (walkInMode && preSelectedRoom != null) {
            cmbRoom.setValue(preSelectedRoom);
            cmbRoom.setDisable(true);
            dpReserveDate.setValue(LocalDate.now());
            dpReserveDate.setDisable(true);
            txtReserveTime.setDisable(true);
            txtDeposit.setText("0.00");
            txtDeposit.setDisable(true);
        }
    }

    private void loadRooms() {
        List<Room> availableRooms = roomService.getAvailableRooms();
        cmbRoom.getItems().clear();
        cmbRoom.getItems().addAll(availableRooms);
    }

    private void updateFeeEstimate() {
        Room selectedRoom = cmbRoom.getValue();
        if (selectedRoom != null) {
            LocalDate date = dpReserveDate.getValue() != null ? dpReserveDate.getValue() : LocalDate.now();
            BigDecimal price = pricingService.calculatePrice(selectedRoom, date);
            lblFeeEstimate.setText("¥" + price);
        }
    }

    public boolean validate() {
        lblError.setText("");

        if (cmbRoom.getValue() == null) {
            lblError.setText("请选择包间");
            return false;
        }

        Integer peopleCount = spnPeopleCount.getValue();
        if (peopleCount == null || peopleCount < 1) {
            lblError.setText("人数至少为1");
            return false;
        }

        try {
            String depositText = txtDeposit.getText();
            if (depositText != null && !depositText.isBlank()) {
                new BigDecimal(depositText);
            }
        } catch (NumberFormatException e) {
            lblError.setText("定金格式不正确");
            return false;
        }

        return true;
    }

    public OrderCreateDTO buildDTO() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setRoomId(cmbRoom.getValue().getId());

        if (dpReserveDate.getValue() != null) {
            LocalDate date = dpReserveDate.getValue();
            LocalTime time = LocalTime.of(0, 0);
            String timeText = txtReserveTime.getText();
            if (timeText != null && !timeText.isBlank()) {
                try {
                    time = LocalTime.parse(timeText);
                } catch (Exception ignored) {
                }
            }
            dto.setReserveTime(LocalDateTime.of(date, time));
        }

        dto.setPeopleCount(spnPeopleCount.getValue());

        try {
            String depositText = txtDeposit.getText();
            dto.setDeposit(depositText != null && !depositText.isBlank()
                    ? new BigDecimal(depositText) : Constants.DEFAULT_DEPOSIT);
        } catch (NumberFormatException e) {
            dto.setDeposit(Constants.DEFAULT_DEPOSIT);
        }

        return dto;
    }

    public boolean isWalkInMode() {
        return walkInMode;
    }
}