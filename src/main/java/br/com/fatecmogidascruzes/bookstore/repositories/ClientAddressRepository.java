package br.com.fatecmogidascruzes.bookstore.repositories;

import br.com.fatecmogidascruzes.bookstore.entities.ClientAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientAddressRepository extends JpaRepository<ClientAddress, Long> {
	List<ClientAddress> findByClient_CodeOrderById(String clientCode);
	Optional<ClientAddress> findByIdAndClient_Code(Long id, String clientCode);
	long countByClient_CodeAndType(String clientCode, String type);
}