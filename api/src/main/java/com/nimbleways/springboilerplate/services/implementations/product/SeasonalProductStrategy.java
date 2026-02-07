package com.nimbleways.springboilerplate.services.implementations.product;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SeasonalProductStrategy implements IProductProcessing {

    private final ProductRepository productRepository;
    private final ProductService productService;

    public SeasonalProductStrategy(ProductRepository productRepository,
                                   ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Override
    public void process(Product p) {

        boolean inSeason =
                LocalDate.now().isAfter(p.getSeasonStartDate()) &&
                        LocalDate.now().isBefore(p.getSeasonEndDate());

        if (inSeason && p.getAvailable() > 0) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else {
            productService.handleSeasonalProduct(p);
        }
    }

    @Override
    public ProductType getType() {
        return ProductType.SEASONAL;
    }
}
