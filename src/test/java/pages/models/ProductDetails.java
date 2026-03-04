package pages.models;

public class ProductDetails {
    private final String name;
    private final double rating;

    public ProductDetails(String name, double rating) {
        this.name = name;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return "ProductDetails{name='" + name + "', rating=" + rating + '}';
    }
}
