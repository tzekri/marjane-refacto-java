package com.nimbleways.springboilerplate.services.implementations.order;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.implementations.product.ProductRuleFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessingService {

    private final OrderRepository orderRepository;
    private final ProductRuleFactory productRuleFactory;

    public OrderProcessingService(OrderRepository orderRepository,
                                  ProductRuleFactory productRuleFactory) {
        this.orderRepository = orderRepository;
        this.productRuleFactory = productRuleFactory;
    }

    public ProcessOrderResponse processOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        for (Product p : order.getItems()) {

            ProductType type = ProductType.from(p.getType());

            productRuleFactory
                    .getStrategy(type)
                    .process(p);
        }

        return new ProcessOrderResponse(order.getId());
    }
}
