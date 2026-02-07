package com.nimbleways.springboilerplate.services.implementations.product;


import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;

public interface IProductProcessing {

    void process(Product product);

    ProductType getType();
}
