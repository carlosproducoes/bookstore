package br.com.fatecmogidascruzes.bookstore.repositories;

import br.com.fatecmogidascruzes.bookstore.entities.CardBrand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardBrandRepository extends JpaRepository<CardBrand, Long> {
    Optional<CardBrand> findByNameIgnoreCase(String name);
    List<CardBrand> findAllByOrderByName();
}
