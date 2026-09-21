package br.com.fatecmogidascruzes.bookstore.repositories;

import br.com.fatecmogidascruzes.bookstore.entities.ClientCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientCardRepository extends JpaRepository<ClientCard, Long> {
    List<ClientCard> findByClient_CodeOrderByPreferredDescId(String clientCode);
    Optional<ClientCard> findByIdAndClient_Code(Long id, String clientCode);
    long countByClient_Code(String clientCode);
    long countByClient_CodeAndPreferredTrue(String clientCode);
}
