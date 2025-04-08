package com.fawry.order_api.mapper;

import com.fawry.order_api.domain.event.OrderCreatedEventDTO;
import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.domain.model.OrderItem;
import com.fawry.order_api.domain.model.Outbox;
import com.fawry.order_api.dto.dtos.*;
import com.fawry.order_api.dto.enums.OrderSagaStatus;
import com.fawry.order_api.dto.enums.SagaEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OutboxMapper {


    public Outbox mapToOutbox(OrderRequest request, Order order, OrderSagaStatus status, String customerEmail, Outbox outbox) {

            outbox.setOrderId(order.getOrderId());
            outbox.setStatus(OrderSagaStatus.CREATED);
            outbox.setCustomerName(request.customerName());
            outbox.setCustomerContact(request.customerContact());
            outbox.setCustomerEmail(customerEmail);
            outbox.setSagaEventType(SagaEventType.ORDER_CREATED);
            outbox.setPaymentAmount(order.getPaymentAmount().getAmount());
            outbox.setNumber(request.paymentMethod().details().getNumber());
            outbox.setCvv(request.paymentMethod().details().getCvv());
            outbox.setExpiry(request.paymentMethod().details().getExpiry());
            outbox.setGovernorate(request.addressDetails().getGovernorate());
            outbox.setCity(request.addressDetails().getCity());
            outbox.setAddress(request.addressDetails().getAddress());
            outbox.setProcessed(false);
            outbox.setUserId(order.getUserId());
            outbox.setCity(request.addressDetails().getCity());
            return outbox;
    }

    public OrderCreatedEventDTO mapToOrderCreatedEventDTO(Outbox outbox, Order order) {
            PaymentDetails details = new PaymentDetails();
            details.setNumber(outbox.getNumber());
            details.setCvv(outbox.getCvv());
            details.setExpiry(outbox.getExpiry());
            PaymentMethod paymentMethod = new PaymentMethod(details);

            AddressDetails addressDetails = AddressDetails.builder()
                    .governorate(outbox.getGovernorate())
                    .city(outbox.getCity())
                    .address(outbox.getAddress())
                    .build();

            List<OrderItem> orderItems = order.getOrderItems().stream().toList();
            List<OrderItemDTO> orderItemDTOList = new ArrayList<>();
            for (OrderItem orderItem : orderItems) {
                    OrderItemDTO orderItemDTO = new OrderItemDTO(orderItem.getStoreId(), orderItem.getProductId(), orderItem.getQuantity(), orderItem.getPrice().getAmount());
                    orderItemDTOList.add(orderItemDTO);
            }

            OrderCreatedEventDTO orderCreatedEventDTO = OrderCreatedEventDTO.newInstance(outbox.getOrderId(), order.getUserId(), outbox.getSagaEventType().name(), outbox.getStatus().name(), outbox.getCustomerEmail(), outbox.getCustomerName(), outbox.getCustomerContact(), addressDetails, outbox.getPaymentAmount(), orderItemDTOList, paymentMethod);
            log.info("Mapped outbox to orderCreatedEventDTO: {}", orderCreatedEventDTO);
            return orderCreatedEventDTO;
    }
}
