package br.com.fatecmogidascruzes.bookstore.services;

import br.com.fatecmogidascruzes.bookstore.entities.Client;
import br.com.fatecmogidascruzes.bookstore.entities.ClientAddress;
import br.com.fatecmogidascruzes.bookstore.repositories.ClientRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Locale;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public List<Client> findAll(String code, String name, String cpf, String email, String status) {
        return clientRepository.findByCodeContainingIgnoreCaseAndNameContainingIgnoreCaseAndCpfContainingAndEmailContainingIgnoreCaseAndStatusContainingIgnoreCase(
                normalize(code), normalize(name), normalize(cpf), normalize(email), normalize(status));
    }

    @Transactional(readOnly = true)
    public Client findByCode(String code) {
        return clientRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + code));
    }

    @Transactional
    public void inactivate(String code) {
        Client client = findByCode(code);
        client.setStatus("INATIVO");
        clientRepository.save(client);
    }

    @Transactional
    public void delete(String code) {
        Client client = findByCode(code);
        clientRepository.delete(client);
    }

    @Transactional
    public void changePassword(String code, String password, String confirmation) {
        if (!password.equals(confirmation) || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}$")) {
            throw new IllegalArgumentException("A senha deve ter 8 caracteres, maiúscula, minúscula, especial e confirmação igual.");
        }
        Client client = findByCode(code);
        client.setPasswordHash(passwordEncoder.encode(password));
        clientRepository.save(client);
    }

    @Transactional
    public void update(String code, String name, String gender, java.time.LocalDate birth,
                       String cpf, String email, String phoneType, String ddd, String phoneNumber) {
        Client client = findByCode(code);
        if (clientRepository.existsByCpfAndCodeNot(cpf, code) || clientRepository.existsByEmailAndCodeNot(email, code)) {
            throw new IllegalArgumentException("CPF ou e-mail já cadastrado.");
        }
        client.setName(name);
        client.setGender(gender);
        client.setBirth(birth);
        client.setCpf(cpf);
        client.setEmail(email);
        client.setPhoneType(phoneType);
        client.setDdd(ddd);
        client.setPhoneNumber(phoneNumber);
        client.setPhone("(" + ddd + ") " + phoneNumber);
        clientRepository.save(client);
    }

    @Transactional
    public Client create(String name, String gender, java.time.LocalDate birth, String cpf, String email,
                         String phoneType, String ddd, String phoneNumber, String password,
                         String passwordConfirmation, AddressData delivery, AddressData billing) {
                validateAddress(delivery);
                validateAddress(billing);
        if (!password.equals(passwordConfirmation)) {
            throw new IllegalArgumentException("As senhas não conferem.");
        }
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}$")) {
            throw new IllegalArgumentException("A senha deve ter 8 caracteres, maiúscula, minúscula e especial.");
        }
        if (clientRepository.existsByCpf(cpf) || clientRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("CPF ou e-mail já cadastrado.");
        }
        Client client = new Client();
        client.setCode("C-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        client.setName(name);
        client.setGender(gender);
        client.setBirth(birth);
        client.setCpf(cpf);
        client.setEmail(email);
        client.setPhoneType(phoneType);
        client.setDdd(ddd);
        client.setPhoneNumber(phoneNumber);
        client.setPhone("(" + ddd + ") " + phoneNumber);
        client.setPasswordHash(passwordEncoder.encode(password));
        client.addAddress(toEntity(delivery, "Entrega", client));
        client.addAddress(toEntity(billing, "Cobrança", client));

        return clientRepository.save(client);
    }

    private String normalize(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }

    private ClientAddress toEntity(AddressData data, String type, Client client) {
        ClientAddress address = new ClientAddress();
        address.setClient(client);
        address.setLabel(data.label());
        address.setType(type);
        address.setResidenceType(data.residenceType());
        address.setStreetType(data.streetType());
        address.setStreet(data.street());
        address.setNumber(data.number());
        address.setNeighborhood(data.neighborhood());
        address.setCep(data.cep());
        address.setCity(data.city());
        address.setState(data.state());
        address.setCountry("Brasil");
        address.setNotes(data.notes());
        return address;
    }

    private void validateAddress(AddressData address) {
        if (address == null || isBlank(address.label()) || isBlank(address.residenceType())
                || isBlank(address.streetType()) || isBlank(address.street()) || isBlank(address.number())
                || isBlank(address.neighborhood()) || isBlank(address.cep()) || isBlank(address.city())
                || isBlank(address.state())) {
            throw new IllegalArgumentException("Preencha todos os campos obrigatórios do endereço.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record AddressData(String label, String residenceType, String streetType, String street,
                              String number, String neighborhood, String cep, String city,
                              String state, String notes) {}
}
