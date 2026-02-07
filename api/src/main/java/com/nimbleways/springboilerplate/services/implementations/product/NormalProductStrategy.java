package com.nimbleways.springboilerplate.services.implementations.product;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import org.springframework.stereotype.Component;

@Component
public class NormalProductStrategy implements IProductProcessing {

    private final ProductRepository productRepository;
    private final ProductService productService;

    public NormalProductStrategy(ProductRepository productRepository,
                                 ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Override
    public void process(Product p) {
        if (p.getAvailable() > 0) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else if (p.getLeadTime() > 0) {
            productService.notifyDelay(p.getLeadTime(), p);
        }
    }

    @Override
    public ProductType getType() {
        return ProductType.NORMAL;
    }
}

