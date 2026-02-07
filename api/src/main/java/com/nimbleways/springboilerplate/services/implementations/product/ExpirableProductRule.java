package com.nimbleways.springboilerplate.services.implementations.product;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ExpirableProductRule implements IProductProcessing {

    private final ProductRepository productRepository;
    private final ProductService productService;

    public ExpirableProductRule(ProductRepository productRepository,
                                    ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Override
    public void process(Product p) {

        if (p.getAvailable() > 0 &&
                p.getExpiryDate().isAfter(LocalDate.now())) {

            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);

        } else {
            productService.handleExpiredProduct(p);
        }
    }

    @Override
    public ProductType getType() {
        return ProductType.EXPIRABLE;
    }
}
