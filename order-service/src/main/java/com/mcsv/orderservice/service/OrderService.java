package com.mcsv.orderservice.service;


import com.mcsv.orderservice.dto.InventoryResponse;
import com.mcsv.orderservice.dto.OrderLineItemsDto;
import com.mcsv.orderservice.dto.OrderRequest;
import com.mcsv.orderservice.model.Order;
import com.mcsv.orderservice.model.OrderLineItems;
import com.mcsv.orderservice.repository.OrderRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WebClient.Builder webClientBuilder; //changes here cause we need load balancer, we have mulpiple instances

    @Autowired
    private Tracer tracer;

    //this realize the order
    //@Transactional(readOnly = true)
    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();


        //elements of order
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        order.setNumberOrder(UUID.randomUUID().toString()); //this is the order number
        order.setOrderLineItems(orderLineItems);

        //communicate with another service with ractive programming with webflux, we need webflux dependency
        List<String> codeSku = order.getOrderLineItems().stream()
                        .map(OrderLineItems::getCodeSku)
                        .collect(Collectors.toList());

        System.out.println("codeSku = " + codeSku);

        //here we config zipkin
        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");

        try(Tracer.SpanInScope isLookup = tracer.withSpan(inventoryServiceLookup.start())){
            inventoryServiceLookup.tag("call", "inventory-service");

            InventoryResponse [] inventoryResponseArray = webClientBuilder.build().get()
                    .uri("http://localhost:8082/api/inventory", uriBuilder -> uriBuilder.queryParam("codeSku", codeSku).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                    .allMatch(InventoryResponse::isInStock);
            if(allProductsInStock){
                orderRepository.save(order);
                return "Order placed successfully";
            }else {
                throw new IllegalArgumentException("Some products are not in stock");
            }

        }finally {
            inventoryServiceLookup.end();
        }



        //orderRepository.save(order);

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto){
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setCodeSku(orderLineItemsDto.getCodeSku());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }
}

