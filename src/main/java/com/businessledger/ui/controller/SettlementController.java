package com.businessledger.ui.controller;

import com.businessledger.dto.SettlementDTO;
import com.businessledger.entity.Order;
import com.businessledger.service.OrderService;
import com.businessledger.service.PricingService;
import com.businessledger.util.Constants;
import com.businessledger.util.DateUtil;
import com.businessledger.util.PriceUtil;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class SettlementController {

    @FXML
    private Label lblTitle;
    @FXML
    private VBox orderInfoCard;
    @FXML
    private Label lblRoomName;
    @FXML
    private Label lblPeopleCount;
    @FXML
    private Label lblStartTime;
    @FXML
    private Label lblEndTime;
    @FXML
    private Label lblDuration;
    @FXML
    private Label lblRoomFee;
    @FXML
    private Label lblOvernightFee;
    @FXML
    private Label lblSubtotal;
    @FXML
    private Label lblDepositReturn;
    @FXML
    private Label lblTotalReceivable;
    @FXML
    private TextField txtActualPayment;
    @FXML
    private TextField txtRefund;
    @FXML
    private CheckBox chkMeituan;
    @FXML
    private Spinner<Integer> spnOvernightCount;
    @FXML
    private Label lblOvernightTotal;
    @FXML
    private Label lblError;

    private final OrderService orderService;
    private final PricingService pricingService;

    private Order currentOrder;

    public SettlementController(OrderService orderService, PricingService pricingService) {
        this.orderService = orderService;
        this.pricingService = pricingService;
    }

    public void setOrder(Order order) {
        this.currentOrder = order;
    }

    @FXML
    public void initialize() {
        spnOvernightCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0));
        spnOvernightCount.valueProperty().addListener((obs, oldVal, newVal) -> updateCalculation());
    }

    public void loadOrderData() {
        if (currentOrder == null) {
            return;
        }

        lblTitle.setText("收款结算 - " + currentOrder.getRoom().getName());
        lblRoomName.setText("包间：" + currentOrder.getRoom().getName());
        lblPeopleCount.setText("人数：" + (currentOrder.getPeopleCount() != null ? currentOrder.getPeopleCount() : 0));
        lblStartTime.setText("开台：" + DateUtil.formatDateTime(currentOrder.getStartTime()));

        LocalDate today = LocalDate.now();
        BigDecimal roomFee = pricingService.calculatePrice(currentOrder.getRoom(), today);
        lblRoomFee.setText("¥ " + roomFee);

        BigDecimal deposit = currentOrder.getDeposit() != null ? currentOrder.getDeposit() : Constants.ZERO;
        lblDepositReturn.setText("-¥ " + deposit);

        updateCalculation();
    }

    private void updateCalculation() {
        if (currentOrder == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        BigDecimal roomFee = pricingService.calculatePrice(currentOrder.getRoom(), today);

        int overnightCount = spnOvernightCount.getValue() != null ? spnOvernightCount.getValue() : 0;
        BigDecimal overnightFee = pricingService.calculateOvernightFee(overnightCount);

        lblOvernightFee.setText("¥ " + overnightFee);
        lblOvernightTotal.setText("¥ " + overnightFee);

        BigDecimal subtotal = PriceUtil.add(roomFee, overnightFee);
        lblSubtotal.setText("¥ " + subtotal);

        BigDecimal deposit = currentOrder.getDeposit() != null ? currentOrder.getDeposit() : Constants.ZERO;
        BigDecimal totalReceivable = PriceUtil.subtract(subtotal, deposit);
        lblTotalReceivable.setText("¥ " + totalReceivable);

        txtActualPayment.setText(totalReceivable.toString());
    }

    public boolean validate() {
        lblError.setText("");

        try {
            String paymentText = txtActualPayment.getText();
            if (paymentText != null && !paymentText.isBlank()) {
                new BigDecimal(paymentText);
            }
        } catch (NumberFormatException e) {
            lblError.setText("实付款金额格式不正确");
            return false;
        }

        try {
            String refundText = txtRefund.getText();
            if (refundText != null && !refundText.isBlank()) {
                new BigDecimal(refundText);
            }
        } catch (NumberFormatException e) {
            lblError.setText("退款金额格式不正确");
            return false;
        }

        return true;
    }

    public SettlementDTO buildDTO() {
        SettlementDTO dto = new SettlementDTO();
        dto.setOrderId(currentOrder.getId());

        try {
            dto.setActualPayment(new BigDecimal(txtActualPayment.getText()));
        } catch (NumberFormatException e) {
            dto.setActualPayment(BigDecimal.ZERO);
        }

        try {
            String refundText = txtRefund.getText();
            dto.setRefund(refundText != null && !refundText.isBlank()
                    ? new BigDecimal(refundText) : BigDecimal.ZERO);
        } catch (NumberFormatException e) {
            dto.setRefund(BigDecimal.ZERO);
        }

        dto.setIsMeituan(chkMeituan.isSelected());
        dto.setOvernightCount(spnOvernightCount.getValue() != null ? spnOvernightCount.getValue() : 0);

        return dto;
    }
}