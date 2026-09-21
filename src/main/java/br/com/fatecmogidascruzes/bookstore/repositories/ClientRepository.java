package br.com.fatecmogidascruzes.bookstore.repositories;

import br.com.fatecmogidascruzes.bookstore.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByCode(String code);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    boolean existsByCpfAndCodeNot(String cpf, String code);
    boolean existsByEmailAndCodeNot(String email, String code);
    List<Client> findByCodeContainingIgnoreCaseAndNameContainingIgnoreCaseAndCpfContainingAndEmailContainingIgnoreCaseAndStatusContainingIgnoreCase(
            String code, String name, String cpf, String email, String status);
}
