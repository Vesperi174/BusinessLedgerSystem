package com.businessledger.ui.controller;

import com.businessledger.config.SpringContextHolder;
import com.businessledger.dto.OrderDTO;
import com.businessledger.entity.Room;
import com.businessledger.enums.RoomStatus;
import com.businessledger.event.OrderStatusChangedEvent;
import com.businessledger.event.RoomStatusChangedEvent;
import com.businessledger.service.AuditLogService;
import com.businessledger.service.OrderService;
import com.businessledger.service.RoomService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.TilePane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MainController {

    @FXML
    private Button btnWalkIn;
    @FXML
    private Button btnReservation;
    @FXML
    private Button btnBackup;
    @FXML
    private Button btnHistory;
    @FXML
    private TilePane roomGrid;
    @FXML
    private TableView<OrderDTO> orderTable;
    @FXML
    private TableColumn<OrderDTO, String> colOrderId;
    @FXML
    private TableColumn<OrderDTO, String> colRoom;
    @FXML
    private TableColumn<OrderDTO, String> colStatus;
    @FXML
    private TableColumn<OrderDTO, Integer> colPeople;
    @FXML
    private TableColumn<OrderDTO, String> colAmount;
    @FXML
    private TableColumn<OrderDTO, Void> colAction;
    @FXML
    private Label lblTodayRevenue;
    @FXML
    private Label lblOrderCount;
    @FXML
    private Label lblFinishedCount;
    @FXML
    private Label lblDbStatus;
    @FXML
    private Label lblLastBackup;
    @FXML
    private Label lblLogCount;
    @FXML
    private Label lblNotification;
    @FXML
    private SplitPane mainSplitPane;

    private final RoomService roomService;
    private final OrderService orderService;
    private final AuditLogService auditLogService;
    private Stage stage;

    private final ObservableList<OrderDTO> orderList = FXCollections.observableArrayList();

    public MainController(RoomService roomService, OrderService orderService, AuditLogService auditLogService) {
        this.roomService = roomService;
        this.orderService = orderService;
        this.auditLogService = auditLogService;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        setupKeyboardShortcuts();
        setupOrderTable();
        refreshRoomCards();
        refreshOrderTable();
        refreshStatusBar();
    }

    private void setupKeyboardShortcuts() {
        Scene scene = roomGrid.getScene();
        if (scene != null) {
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::handleCreateReservation);
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN), this::handleWalkIn);
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN), this::handleHistory);
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN), this::handleBackup);
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F5), this::handleRefresh);
        }
    }

    private void setupOrderTable() {
        colOrderId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(data.getValue().getId())));
        colRoom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRoomName()));
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus().getDescription()));
        colPeople.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(
                data.getValue().getPeopleCount()));
        colAmount.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getActualPayment() != null ? data.getValue().getActualPayment().toString() : ""));
        orderTable.setItems(orderList);
    }

    private void refreshRoomCards() {
        roomGrid.getChildren().clear();
        List<Room> rooms = roomService.getAllRooms();

        for (Room room : rooms) {
            VBox card = createRoomCard(room);
            roomGrid.getChildren().add(card);
        }
    }

    private VBox createRoomCard(Room room) {
        VBox card = new VBox(8);
        card.setPrefWidth(220);
        card.setPrefHeight(160);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new javafx.geometry.Insets(12));

        String statusClass = switch (room.getStatus()) {
            case IDLE -> "room-card-idle";
            case RESERVED -> "room-card-reserved";
            case IN_USE -> "room-card-inuse";
        };
        card.getStyleClass().addAll("room-card", statusClass);

        Label statusIndicator = new Label(room.getStatus().getDescription());
        statusIndicator.getStyleClass().add("room-card-status");

        Label nameLabel = new Label(room.getName());
        nameLabel.getStyleClass().add("room-card-name");

        Label priceLabel = new Label("¥" + room.getPrice() + " / 一口价");
        priceLabel.getStyleClass().add("room-card-price");

        card.getChildren().addAll(statusIndicator, nameLabel, priceLabel);

        if (room.getStatus() == RoomStatus.IDLE) {
            Button openBtn = new Button("直接开台");
            openBtn.getStyleClass().add("room-card-btn");
            openBtn.setOnAction(e -> openWalkInDialog(room));
            card.getChildren().add(openBtn);
        }

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                if (room.getStatus() == RoomStatus.IDLE) {
                    openWalkInDialog(room);
                }
            }
        });

        return card;
    }

    private void refreshOrderTable() {
        orderList.clear();
        List<OrderDTO> orders = orderService.queryOrders(new com.businessledger.dto.OrderQueryDTO());
        orderList.addAll(orders);
    }

    private void refreshStatusBar() {
        lblDbStatus.setText("正常");
        lblLastBackup.setText("-");
        lblLogCount.setText(auditLogService.count() + " 条");
    }

    @FXML
    private void handleWalkIn() {
        List<Room> available = roomService.getAvailableRooms();
        if (available.isEmpty()) {
            showNotification("暂无可用的空闲包间");
            return;
        }
        openWalkInDialog(available.get(0));
    }

    private void openWalkInDialog(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReservationDialog.fxml"));
            loader.setControllerFactory(SpringContextHolder::getBean);
            Parent root = loader.load();

            ReservationController controller = loader.getController();
            controller.setRoom(room);
            controller.setWalkInMode(true);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(stage);
            dialogStage.setTitle("直接开台 - " + room.getName());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            refreshAll();
        } catch (Exception e) {
            showNotification("打开开台窗口失败: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateReservation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReservationDialog.fxml"));
            loader.setControllerFactory(SpringContextHolder::getBean);
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(stage);
            dialogStage.setTitle("创建预约");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            refreshAll();
        } catch (Exception e) {
            showNotification("打开预约窗口失败: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackup() {
        showNotification("备份功能将在后续版本中实现");
    }

    @FXML
    private void handleHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HistoryDialog.fxml"));
            loader.setControllerFactory(SpringContextHolder::getBean);
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(stage);
            dialogStage.setTitle("历史查询");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (Exception e) {
            showNotification("打开历史查询窗口失败: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        refreshAll();
    }

    private void refreshAll() {
        Platform.runLater(() -> {
            refreshRoomCards();
            refreshOrderTable();
            refreshStatusBar();
        });
    }

    private void showNotification(String message) {
        lblNotification.setText(message);
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        Platform.runLater(() -> {
            refreshRoomCards();
            refreshOrderTable();
        });
    }

    @EventListener
    public void onRoomStatusChanged(RoomStatusChangedEvent event) {
        Platform.runLater(this::refreshRoomCards);
    }
}