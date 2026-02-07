package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.implementations.order.OrderProcessingService;
import com.nimbleways.springboilerplate.services.implementations.product.NormalProductStrategy;
import com.nimbleways.springboilerplate.services.implementations.product.ProductRuleFactory;
import com.nimbleways.springboilerplate.services.implementations.product.SeasonalProductStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

class OrderProcessingServiceTest {

    private OrderRepository orderRepository;
    private ProductRuleFactory productRuleFactory;
    private OrderProcessingService orderProcessingService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRuleFactory = mock(ProductRuleFactory.class);
        orderProcessingService = new OrderProcessingService(orderRepository, productRuleFactory);
    }

    @Test
    void processOrder_shouldProcessEachProductAndReturnResponse() {
        // Arrange
        Product product1 = new Product();
        product1.setType("NORMAL"); // matches enum

        Product product2 = new Product();
        product2.setType("SEASONAL"); // matches enum

        Order order = new Order();
        order.setId(1L);
        order.setItems(Set.of(product1, product2));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Mock strategies
        NormalProductStrategy strategyNormal = mock(NormalProductStrategy.class);
        SeasonalProductStrategy strategySeasonal = mock(SeasonalProductStrategy.class);

        when(productRuleFactory.getStrategy(ProductType.NORMAL)).thenReturn(strategyNormal);
        when(productRuleFactory.getStrategy(ProductType.SEASONAL)).thenReturn(strategySeasonal);

        // Act
        ProcessOrderResponse response = orderProcessingService.processOrder(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());

        // Verify that each strategy was called with correct product
        verify(strategyNormal, times(1)).process(product1);
        verify(strategySeasonal, times(1)).process(product2);
    }

    @Test
    void processOrder_whenOrderNotFound_shouldThrowException() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderProcessingService.processOrder(99L);
        });

        assertEquals("Order not found", exception.getMessage());
    }
}
