package br.com.agendafacilpro.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agendafacilpro.domain.Customer;

public interface CustomerRepo extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEstablishmentIdAndPhoneNormalized(Long establishmentId, String phoneNormalized);
}
