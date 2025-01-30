package com.techie.microservices.order.service;

import com.techie.microservices.order.client.InventoryClient;
import com.techie.microservices.order.dto.OrderRequest;
import com.techie.microservices.order.event.OrderPlacedEvent;
import com.techie.microservices.order.model.Order;
import com.techie.microservices.order.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String,OrderPlacedEvent> kafkaTemplate;
    public void placeOrder(OrderRequest orderRequest){
        var isProductInStock=inventoryClient.isInStock(orderRequest.skuCode(),orderRequest.quantity());
        if(isProductInStock) {
            Order order = new Order();
            // mapping order request to order object
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price());
            order.setQuantity(orderRequest.quantity());
            order.setSkuCode(orderRequest.skuCode());
            // saving to order repo
            orderRepository.save(order);
            // sending message to kafka topic
            // sending order number and email
            OrderPlacedEvent orderPlacedEvent=new OrderPlacedEvent(order.getOrderNumber(),orderRequest.userDetails().email());
            log.info("Start sending orderPlacedEvent {} to kafka topic order-placed ",orderPlacedEvent);
            kafkaTemplate.send("order placed",orderPlacedEvent);
            log.info("End sending orderPlacedEvent {} to kafka topic order-placed ",orderPlacedEvent);
        }else{
            throw new RuntimeException("Product with skuCode "+orderRequest.skuCode()+" is not in stock");
        }
    }
}
