package com.mcsv.orderservice.service;


import com.mcsv.orderservice.config.rabitmq.Producer;
import com.mcsv.orderservice.dto.InventoryResponse;
import com.mcsv.orderservice.dto.OrderLineItemsDto;
import com.mcsv.orderservice.dto.OrderRequest;
import com.mcsv.orderservice.event.OrderPlacedEvent;
import com.mcsv.orderservice.model.Order;
import com.mcsv.orderservice.model.OrderLineItems;
import com.mcsv.orderservice.repository.OrderRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class OrderService {

    @Autowired
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WebClient.Builder webClientBuilder; //To communicate with another service

    @Autowired
    private Tracer tracer;

    @Autowired
    private Producer producer;

    //this realize the order
    //@Transactional(readOnly = true)
    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();


        //fist we pass the orderRequest to orderLineItemsDtoList, then we map to orderLineItems
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        //we add a id random to the order
        order.setNumberOrder(UUID.randomUUID().toString());
        //we add the orderLineItems to the order
        order.setOrderLineItems(orderLineItems);

        //we get the codeSku from the orderLineItems, can be more than one
        List<String> codeSku = order.getOrderLineItems().stream()
                        .map(OrderLineItems::getCodeSku)
                        .collect(Collectors.toList());

        System.out.println("codeSku = " + codeSku);

        //here we config zipkin
        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");

        try(Tracer.SpanInScope isLookup = tracer.withSpan(inventoryServiceLookup.start())){
            inventoryServiceLookup.tag("call", "inventory-service");

            //here we comunicate with inventory-service
            InventoryResponse [] inventoryResponseArray = webClientBuilder.build().get()
                    //we pass codeSku ass param
                    .uri("http://inventory-service/api/inventory", uriBuilder -> uriBuilder.queryParam("codeSku", codeSku).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class) //we receved the response of type InventoryResponse (DTO)
                    .block();

            //we ckeck if the products are in stock
            boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                    .allMatch(InventoryResponse::isInStock);

            //if products are in stock then save the order
            if(allProductsInStock){
                orderRepository.save(order);
                //message with kafka
                kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getNumberOrder()));

                //message with rabbitmq
                sendMessageRabbitMQ("Notification sending");
                return "Order placed successfully";
            }else {
                throw new IllegalArgumentException("Some products are not in stock");
            }

        }finally {
            inventoryServiceLookup.end();
        }



        //orderRepository.save(order);

    }

    //pass info from orderLineItemsDto to orderLineItems
    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto){
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setCodeSku(orderLineItemsDto.getCodeSku());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }

    //rabbitmq
    private void sendMessageRabbitMQ(String message) {
        log.info("Sending message to RabbitMQ: {}", message);
        producer.send(message);
    }
}

