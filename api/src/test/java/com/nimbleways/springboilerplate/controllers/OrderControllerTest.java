package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertEquals;


import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Specify the controller class you want to test
// This indicates to spring boot to only load UsersController into the context
// Which allows a better performance and needs to do less mocks
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private NotificationService notificationService;

        @MockBean
        private ProductService productService; // mock pour vérifier les appels

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private ProductRepository productRepository;

        /** Test principal déjà existant */
        @Test
        public void processOrderShouldReturn() throws Exception {
                List<Product> allProducts = createProducts();
                Set<Product> orderItems = new HashSet<>(allProducts);
                Order order = createOrder(orderItems);
                productRepository.saveAll(allProducts);
                order = orderRepository.save(order);

                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                        .andExpect(status().isOk());

                Order resultOrder = orderRepository.findById(order.getId()).get();
                assertEquals(order.getId(), resultOrder.getId());
        }

        /** Cas : produit expiré déclenche handleExpiredProduct */
        @Test
        public void expirableProductExpiredShouldCallHandleExpiredProduct() throws Exception {
                Product expired = new Product(null, 10, 0, "EXPIRABLE", "Old Milk",
                        LocalDate.now().minusDays(1), null, null);
                productRepository.save(expired);

                Order order = createOrder(Set.of(expired));
                order = orderRepository.save(order);

                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                        .andExpect(status().isOk());

                verify(productService).handleExpiredProduct(argThat(p -> expired.getName().equals("Old Milk")));

        }

        /** Cas : produit expirable valide diminue la quantité */
        @Test
        public void expirableProductValidShouldDecreaseAvailable() throws Exception {
                Product valid = new Product(null, 5, 5, "EXPIRABLE", "Fresh Butter",
                        LocalDate.now().plusDays(10), null, null);
                productRepository.save(valid);

                Order order = createOrder(Set.of(valid));
                order = orderRepository.save(order);

                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                        .andExpect(status().isOk());

                Product updated = productRepository.findById(valid.getId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                assertEquals(4, updated.getAvailable().intValue());


        }

        /** Cas : produit saisonnier hors saison déclenche handleSeasonalProduct */
        @Test
        public void seasonalProductOutOfSeasonShouldCallHandleSeasonalProduct() throws Exception {
                Product seasonal = new Product(null, 10, 0, "SEASONAL", "Winter Fruit",
                        null, LocalDate.now().plusDays(10), LocalDate.now().plusDays(20));
                productRepository.save(seasonal);

                Order order = createOrder(Set.of(seasonal));
                order = orderRepository.save(order);

                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                        .andExpect(status().isOk());

                verify(productService).handleSeasonalProduct(argThat(p ->
                        p.getName().equals("Winter Fruit") &&
                                p.getType().equals("SEASONAL")
                ));
        }

        /** Cas : produit saisonnier en saison diminue la quantité */
        @Test
        public void seasonalProductInSeasonShouldDecreaseAvailable() throws Exception {
                Product seasonal = new Product(null, 8, 8, "SEASONAL", "Summer Fruit",
                        null, LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
                productRepository.save(seasonal);

                Order order = createOrder(Set.of(seasonal));
                order = orderRepository.save(order);

                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                        .andExpect(status().isOk());

                Product updated = productRepository.findById(seasonal.getId()).get();
                assertEquals(7, updated.getAvailable().intValue());
        }

        /** Cas : produit normal sans stock mais avec leadTime déclenche notifyDelay */
        @Test
        public void normalProductNoStockWithLeadTimeShouldCallNotifyDelay() throws Exception {
                Product normal = new Product(null, 5, 0, "NORMAL", "USB Cable", null, null, null);
                productRepository.save(normal);

                Order order = createOrder(Set.of(normal));
                order = orderRepository.save(order);

                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                        .andExpect(status().isOk());

                verify(productService).notifyDelay(eq(5), argThat(p -> p.getName().equals("USB Cable")));
        }

        /** Cas : produit normal avec stock > 0 diminue la quantité */
        @Test
        public void normalProductWithStockShouldDecreaseAvailable() throws Exception {
                Product normal = new Product(null, 10, 10, "NORMAL", "USB Cable", null, null, null);
                productRepository.save(normal);

                Order order = createOrder(Set.of(normal));
                order = orderRepository.save(order);

                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                        .andExpect(status().isOk());

                Product updated = productRepository.findById(normal.getId()).get();
                assertEquals(9, updated.getAvailable().intValue());
        }

        // Helper methods
        private static Order createOrder(Set<Product> products) {
                Order order = new Order();
                order.setItems(products);
                return order;
        }

        private static List<Product> createProducts() {
                List<Product> products = new ArrayList<>();
                products.add(new Product(null, 15, 30, "NORMAL", "USB Cable", null, null, null));
                products.add(new Product(null, 10, 0, "NORMAL", "USB Dongle", null, null, null));
                products.add(new Product(null, 15, 30, "EXPIRABLE", "Butter", LocalDate.now().plusDays(26), null, null));
                products.add(new Product(null, 90, 6, "EXPIRABLE", "Milk", LocalDate.now().minusDays(2), null, null));
                products.add(new Product(null, 15, 30, "SEASONAL", "Watermelon", null, LocalDate.now().minusDays(2), LocalDate.now().plusDays(58)));
                products.add(new Product(null, 15, 30, "SEASONAL", "Grapes", null, LocalDate.now().plusDays(180), LocalDate.now().plusDays(240)));
                return products;
        }
}
