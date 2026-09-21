package br.com.fatecmogidascruzes.bookstore.services;

import br.com.fatecmogidascruzes.bookstore.entities.CardBrand;
import br.com.fatecmogidascruzes.bookstore.entities.Client;
import br.com.fatecmogidascruzes.bookstore.entities.ClientCard;
import br.com.fatecmogidascruzes.bookstore.repositories.CardBrandRepository;
import br.com.fatecmogidascruzes.bookstore.repositories.ClientCardRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class CardService {
    private static final Set<String> DEFAULT_BRANDS = Set.of("American Express", "Elo", "Hipercard", "Mastercard", "Visa");
    private final ClientCardRepository cardRepository;
    private final CardBrandRepository brandRepository;
    private final ClientService clientService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public CardService(ClientCardRepository cardRepository, CardBrandRepository brandRepository, ClientService clientService) {
        this.cardRepository = cardRepository;
        this.brandRepository = brandRepository;
        this.clientService = clientService;
        seedBrands();
    }

    @Transactional
    public void seedBrands() {
        DEFAULT_BRANDS.forEach(name -> brandRepository.findByNameIgnoreCase(name).orElseGet(() -> brandRepository.save(new CardBrand(name))));
    }

    @Transactional(readOnly = true)
    public List<ClientCard> findByClient(String clientCode) {
        clientService.findByCode(clientCode);
        return cardRepository.findByClient_CodeOrderByPreferredDescId(clientCode);
    }

    @Transactional(readOnly = true)
    public List<CardBrand> findBrands() { return brandRepository.findAllByOrderByName(); }

    @Transactional(readOnly = true)
    public ClientCard find(String clientCode, Long id) {
        return cardRepository.findByIdAndClient_Code(id, clientCode)
                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado."));
    }

    @Transactional
    public ClientCard create(String clientCode, CardData data) {
        Client client = clientService.findByCode(clientCode);
        validate(data, true);
        ClientCard card = new ClientCard();
        apply(card, data);
        card.setClient(client);
        if (cardRepository.countByClient_Code(clientCode) == 0 || data.preferred()) {
            setPreferredForClient(clientCode, card);
        }
        return cardRepository.save(card);
    }

    @Transactional
    public ClientCard update(String clientCode, Long id, CardData data) {
        ClientCard card = find(clientCode, id);
        validate(data, false);
        apply(card, data);
        if (data.preferred()) setPreferredForClient(clientCode, card);
        return cardRepository.save(card);
    }

    @Transactional
    public void setPreferred(String clientCode, Long id) {
        ClientCard card = find(clientCode, id);
        setPreferredForClient(clientCode, card);
        cardRepository.save(card);
    }

    @Transactional
    public void delete(String clientCode, Long id) {
        ClientCard card = find(clientCode, id);
        long total = cardRepository.countByClient_Code(clientCode);
        if (card.isPreferred() && total > 1) {
            cardRepository.delete(card);
            ClientCard replacement = cardRepository.findByClient_CodeOrderByPreferredDescId(clientCode).get(0);
            replacement.setPreferred(true);
            cardRepository.save(replacement);
            return;
        }
        cardRepository.delete(card);
    }

    private void setPreferredForClient(String clientCode, ClientCard selected) {
        cardRepository.findByClient_CodeOrderByPreferredDescId(clientCode).forEach(card -> card.setPreferred(card == selected));
        selected.setPreferred(true);
    }

    private void validate(CardData data, boolean requireSensitiveData) {
        if (data == null || blank(data.holderName()) || blank(data.brand())
                || (requireSensitiveData && (blank(data.number()) || blank(data.securityCode())))) {
            throw new IllegalArgumentException("Preencha todos os campos obrigatórios do cartão.");
        }
        if (!blank(data.number())) {
            String number = digits(data.number());
            if (number.length() < 13 || number.length() > 19 || !validLuhn(number)) {
                throw new IllegalArgumentException("Número de cartão inválido.");
            }
        }
        if (!blank(data.securityCode()) && !data.securityCode().matches("\\d{3,4}")) {
            throw new IllegalArgumentException("Código de segurança inválido.");
        }
        if (brandRepository.findByNameIgnoreCase(data.brand()).isEmpty()) {
            throw new IllegalArgumentException("Bandeira não cadastrada.");
        }
    }

    private void apply(ClientCard card, CardData data) {
        if (!blank(data.number())) {
            String number = digits(data.number());
            card.setCardNumberHash(encoder.encode(number));
            card.setLastFour(number.substring(number.length() - 4));
        }
        card.setHolderName(data.holderName().trim().toUpperCase());
        if (!blank(data.securityCode())) card.setSecurityCodeHash(encoder.encode(data.securityCode()));
        card.setBrand(brandRepository.findByNameIgnoreCase(data.brand()).orElseThrow());
        card.setPreferred(data.preferred());
    }

    private boolean validLuhn(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int index = number.length() - 1; index >= 0; index--) {
            int value = number.charAt(index) - '0';
            if (alternate && (value *= 2) > 9) value -= 9;
            sum += value;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    private String digits(String value) { return value.replaceAll("\\D", ""); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    public record CardData(String number, String holderName, String brand, String securityCode, boolean preferred) {}
}
