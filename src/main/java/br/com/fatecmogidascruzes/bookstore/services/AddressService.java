package br.com.fatecmogidascruzes.bookstore.services;

import br.com.fatecmogidascruzes.bookstore.entities.Client;
import br.com.fatecmogidascruzes.bookstore.entities.ClientAddress;
import br.com.fatecmogidascruzes.bookstore.repositories.ClientAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AddressService {
    private static final Set<String> ADDRESS_TYPES = Set.of("Entrega", "Cobrança");
    private static final Set<String> RESIDENCE_TYPES = Set.of("Casa", "Apartamento");
    private static final Set<String> STREET_TYPES = Set.of("Rua", "Avenida", "Alameda", "Travessa", "Rodovia", "Praça");
    private static final Set<String> STATE_CODES = Set.of("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private final ClientAddressRepository addressRepository;
    private final ClientService clientService;

    public AddressService(ClientAddressRepository addressRepository, ClientService clientService) {
        this.addressRepository = addressRepository;
        this.clientService = clientService;
    }

    @Transactional(readOnly = true)
    public List<ClientAddress> findByClient(String clientCode) {
        clientService.findByCode(clientCode);
        return addressRepository.findByClient_CodeOrderById(clientCode);
    }

    @Transactional(readOnly = true)
    public ClientAddress find(String clientCode, Long id) {
        return addressRepository.findByIdAndClient_Code(id, clientCode)
                .orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado."));
    }

    @Transactional
    public ClientAddress create(String clientCode, AddressData data) {
        Client client = clientService.findByCode(clientCode);
        validate(data);
        ClientAddress address = new ClientAddress();
        apply(address, data);
        address.setClient(client);
        return addressRepository.save(address);
    }

    @Transactional
    public ClientAddress update(String clientCode, Long id, AddressData data) {
        ClientAddress address = find(clientCode, id);
        validate(data);
        if (!address.getType().equals(data.type())
                && addressRepository.countByClient_CodeAndType(clientCode, address.getType()) <= 1) {
            throw new IllegalArgumentException("O cliente deve manter ao menos um endereço de " + address.getType() + ".");
        }
        apply(address, data);
        return addressRepository.save(address);
    }

    @Transactional
    public void delete(String clientCode, Long id) {
        ClientAddress address = find(clientCode, id);
        long sameType = addressRepository.countByClient_CodeAndType(clientCode, address.getType());
        if (sameType <= 1) {
            throw new IllegalArgumentException("O cliente deve manter ao menos um endereço de " + address.getType() + ".");
        }
        addressRepository.delete(address);
    }

    private void validate(AddressData data) {
        if (data == null || blank(data.label()) || blank(data.type()) || blank(data.residenceType())
                || blank(data.streetType()) || blank(data.street()) || blank(data.number())
                || blank(data.neighborhood()) || blank(data.cep()) || blank(data.city()) || blank(data.state())) {
            throw new IllegalArgumentException("Preencha todos os campos obrigatórios do endereço.");
        }
        if (!ADDRESS_TYPES.contains(data.type())) {
            throw new IllegalArgumentException("Tipo de endereço inválido.");
        }
        if (!RESIDENCE_TYPES.contains(data.residenceType())) {
            throw new IllegalArgumentException("Tipo de residência inválido.");
        }
        if (!STREET_TYPES.contains(data.streetType())) {
            throw new IllegalArgumentException("Tipo de logradouro inválido.");
        }
        if (!STATE_CODES.contains(data.state().toUpperCase())) {
            throw new IllegalArgumentException("Estado inválido.");
        }
        if (!data.cep().matches("\\d{5}-?\\d{3}")) {
            throw new IllegalArgumentException("CEP inválido.");
        }
    }

    private void apply(ClientAddress address, AddressData data) {
        address.setLabel(data.label().trim());
        address.setType(data.type());
        address.setResidenceType(data.residenceType());
        address.setStreetType(data.streetType());
        address.setStreet(data.street().trim());
        address.setNumber(data.number().trim());
        address.setNeighborhood(data.neighborhood().trim());
        address.setCep(data.cep().trim());
        address.setCity(data.city().trim());
        address.setState(data.state().toUpperCase());
        address.setCountry("Brasil");
        address.setNotes(data.notes() == null ? null : data.notes().trim());
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    public record AddressData(String label, String type, String residenceType, String streetType,
                              String street, String number, String neighborhood, String cep,
                              String city, String state, String notes) {}
}
