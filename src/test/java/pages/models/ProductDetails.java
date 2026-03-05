package pages.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetails {
    private String name;
    private double rating;
    private double price;
    private String category;

    public ProductDetails(String name, double rating) {
        this.name = name;
        this.rating = rating;
    }

    public ProductDetails(String name, double price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }
}