package com.nimbleways.springboilerplate.services.implementations;


import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository pr;

    @Mock
    private NotificationService ns;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ============================
    // notifyDelay tests
    // ============================
    @Test
    void notifyDelayShouldSaveProductAndSendNotification() {
        Product p = new Product();
        p.setName("USB Cable");

        productService.notifyDelay(5, p);

        // Vérifie que le leadTime est mis à jour
        assert(p.getLeadTime() == 5);

        // Vérifie que ProductRepository.save est appelé
        verify(pr).save(p);

        // Vérifie que NotificationService.sendDelayNotification est appelé
        verify(ns).sendDelayNotification(5, "USB Cable");
    }

    // ============================
    // handleSeasonalProduct tests
    // ============================
    @Test
    void handleSeasonalProductAfterSeasonEndShouldSendOutOfStock() {
        Product p = new Product();
        p.setName("Seasonal Product");
        p.setLeadTime(5);
        p.setSeasonEndDate(LocalDate.now().minusDays(1));
        p.setSeasonStartDate(LocalDate.now().minusDays(10));

        productService.handleSeasonalProduct(p);

        // Produit mis à 0 et notification envoyée
        assert(p.getAvailable() == 0);
        verify(ns).sendOutOfStockNotification("Seasonal Product");
        verify(pr).save(p);
    }

    @Test
    void handleSeasonalProductBeforeSeasonStartShouldSendOutOfStock() {
        Product p = new Product();
        p.setName("Seasonal Product");
        p.setLeadTime(5);
        p.setSeasonStartDate(LocalDate.now().plusDays(2));
        p.setSeasonEndDate(LocalDate.now().plusDays(10));

        productService.handleSeasonalProduct(p);

        verify(ns).sendOutOfStockNotification("Seasonal Product");
        verify(pr).save(p);
    }

    @Test
    void handleSeasonalProductDuringSeasonShouldNotifyDelay() {
        Product p = new Product();
        p.setName("Seasonal Product");
        p.setLeadTime(5);
        p.setSeasonStartDate(LocalDate.now().minusDays(2));
        p.setSeasonEndDate(LocalDate.now().plusDays(5));

        productService.handleSeasonalProduct(p);

        verify(ns).sendDelayNotification(5, "Seasonal Product");
        verify(pr).save(p);
    }

    // ============================
    // handleExpiredProduct tests
    // ============================
    @Test
    void handleExpiredProductAvailableAndNotExpiredShouldDecrementAvailable() {
        Product p = new Product();
        p.setName("Expiring Product");
        p.setAvailable(3);
        p.setExpiryDate(LocalDate.now().plusDays(2));

        productService.handleExpiredProduct(p);

        assert(p.getAvailable() == 2);
        verify(pr).save(p);
        verify(ns, never()).sendExpirationNotification(any(), any());
    }

    @Test
    void handleExpiredProductExpiredOrUnavailableShouldSendExpiration() {
        Product p = new Product();
        p.setName("Expired Product");
        p.setAvailable(0);
        p.setExpiryDate(LocalDate.now().minusDays(1));

        productService.handleExpiredProduct(p);

        assert(p.getAvailable() == 0);
        verify(ns).sendExpirationNotification("Expired Product", p.getExpiryDate());
        verify(pr).save(p);
    }
}
