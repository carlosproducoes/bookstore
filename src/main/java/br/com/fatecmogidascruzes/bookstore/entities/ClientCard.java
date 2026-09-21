package br.com.fatecmogidascruzes.bookstore.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "client_cards")
public class ClientCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private CardBrand brand;

    @Column(name = "card_number_hash", nullable = false, length = 100)
    private String cardNumberHash;

    @Column(name = "last_four", nullable = false, length = 4)
    private String lastFour;

    @Column(name = "holder_name", nullable = false, length = 120)
    private String holderName;

    @Column(name = "security_code_hash", nullable = false, length = 100)
    private String securityCodeHash;

    @Column(nullable = false)
    private boolean preferred;

    public Long getId() { return id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public CardBrand getBrand() { return brand; }
    public void setBrand(CardBrand brand) { this.brand = brand; }
    public String getLastFour() { return lastFour; }
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public boolean isPreferred() { return preferred; }
    public void setPreferred(boolean preferred) { this.preferred = preferred; }
    public void setCardNumberHash(String cardNumberHash) { this.cardNumberHash = cardNumberHash; }
    public void setLastFour(String lastFour) { this.lastFour = lastFour; }
    public void setSecurityCodeHash(String securityCodeHash) { this.securityCodeHash = securityCodeHash; }
}
