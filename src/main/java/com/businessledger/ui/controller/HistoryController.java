package com.businessledger.ui.controller;

import com.businessledger.dto.OrderDTO;
import com.businessledger.dto.OrderQueryDTO;
import com.businessledger.entity.Room;
import com.businessledger.enums.OrderStatus;
import com.businessledger.service.OrderService;
import com.businessledger.service.RoomService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Scope("prototype")
public class HistoryController {

    @FXML
    private DatePicker dpStartDate;
    @FXML
    private DatePicker dpEndDate;
    @FXML
    private ComboBox<String> cmbRoomFilter;
    @FXML
    private ComboBox<String> cmbStatusFilter;
    @FXML
    private Button btnSearch;
    @FXML
    private TableView<OrderDTO> historyTable;
    @FXML
    private TableColumn<OrderDTO, String> colOrderId;
    @FXML
    private TableColumn<OrderDTO, String> colDate;
    @FXML
    private TableColumn<OrderDTO, String> colRoom;
    @FXML
    private TableColumn<OrderDTO, String> colStatus;
    @FXML
    private TableColumn<OrderDTO, Integer> colPeople;
    @FXML
    private TableColumn<OrderDTO, String> colActualPayment;
    @FXML
    private TableColumn<OrderDTO, String> colRefund;
    @FXML
    private TableColumn<OrderDTO, String> colMeituan;
    @FXML
    private TableColumn<OrderDTO, Void> colAction;
    @FXML
    private Label lblTotalOrders;
    @FXML
    private Label lblFinishedOrders;
    @FXML
    private Label lblCancelledOrders;
    @FXML
    private Label lblTotalRevenue;
    @FXML
    private Label lblTotalRefund;
    @FXML
    private Label lblNetRevenue;

    private final OrderService orderService;
    private final RoomService roomService;

    private final ObservableList<OrderDTO> historyList = FXCollections.observableArrayList();

    public HistoryController(OrderService orderService, RoomService roomService) {
        this.orderService = orderService;
        this.roomService = roomService;
    }

    @FXML
    public void initialize() {
        setupTable();
        loadFilters();
        handleSearch();
    }

    private void setupTable() {
        colOrderId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(data.getValue().getId())));
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCreateTime() != null ? data.getValue().getCreateTime().toLocalDate().toString() : ""));
        colRoom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRoomName()));
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus().getDescription()));
        colPeople.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(
                data.getValue().getPeopleCount()));
        colActualPayment.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getActualPayment() != null ? data.getValue().getActualPayment().toString() : ""));
        colRefund.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRefund() != null ? data.getValue().getRefund().toString() : ""));
        colMeituan.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getIsMeituan() != null && data.getValue().getIsMeituan() ? "是" : "否"));
        historyTable.setItems(historyList);
    }

    private void loadFilters() {
        cmbRoomFilter.getItems().add("全部包间");
        List<Room> rooms = roomService.getAllRooms();
        for (Room room : rooms) {
            cmbRoomFilter.getItems().add(room.getName());
        }
        cmbRoomFilter.setValue("全部包间");

        cmbStatusFilter.getItems().add("全部状态");
        for (OrderStatus status : OrderStatus.values()) {
            cmbStatusFilter.getItems().add(status.getDescription());
        }
        cmbStatusFilter.setValue("全部状态");
    }

    @FXML
    private void handleSearch() {
        OrderQueryDTO queryDTO = new OrderQueryDTO();
        queryDTO.setStartDate(dpStartDate.getValue());
        queryDTO.setEndDate(dpEndDate.getValue());

        String roomFilter = cmbRoomFilter.getValue();
        if (roomFilter != null && !"全部包间".equals(roomFilter)) {
            List<Room> rooms = roomService.getAllRooms();
            for (Room room : rooms) {
                if (room.getName().equals(roomFilter)) {
                    queryDTO.setRoomId(room.getId());
                    break;
                }
            }
        }

        String statusFilter = cmbStatusFilter.getValue();
        if (statusFilter != null && !"全部状态".equals(statusFilter)) {
            for (OrderStatus status : OrderStatus.values()) {
                if (status.getDescription().equals(statusFilter)) {
                    queryDTO.setStatus(status.name());
                    break;
                }
            }
        }

        List<OrderDTO> orders = orderService.queryOrders(queryDTO);
        historyList.clear();
        historyList.addAll(orders);

        updateStats(orders);
    }

    private void updateStats(List<OrderDTO> orders) {
        int total = orders.size();
        int finished = 0;
        int cancelled = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalRefund = BigDecimal.ZERO;

        for (OrderDTO order : orders) {
            if (order.getStatus() == OrderStatus.FINISHED) {
                finished++;
            } else if (order.getStatus() == OrderStatus.CANCELLED) {
                cancelled++;
            }
            if (order.getActualPayment() != null) {
                totalRevenue = totalRevenue.add(order.getActualPayment());
            }
            if (order.getRefund() != null) {
                totalRefund = totalRefund.add(order.getRefund());
            }
        }

        lblTotalOrders.setText(String.valueOf(total));
        lblFinishedOrders.setText(String.valueOf(finished));
        lblCancelledOrders.setText(String.valueOf(cancelled));
        lblTotalRevenue.setText("¥ " + totalRevenue);
        lblTotalRefund.setText("¥ " + totalRefund);
        lblNetRevenue.setText("¥ " + totalRevenue.subtract(totalRefund));
    }
}