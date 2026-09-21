package br.com.fatecmogidascruzes.bookstore.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "card_brands")
public class CardBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String name;

    protected CardBrand() {}

    public CardBrand(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
