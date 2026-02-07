package com.nimbleways.springboilerplate.services.implementations.product;

import com.nimbleways.springboilerplate.enums.ProductType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductRuleFactory {

    private final Map<ProductType, IProductProcessing> strategyMap;

    public ProductRuleFactory(List<IProductProcessing> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        IProductProcessing::getType,
                        s -> s
                ));
    }

    public IProductProcessing getStrategy(ProductType type) {
        IProductProcessing strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy for type " + type);
        }
        return strategy;
    }
}
