package br.com.agendafacilpro.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agendafacilpro.domain.Customer;

public interface CustomerRepo extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEstablishmentIdAndPhoneNormalized(Long establishmentId, String phoneNormalized);

    List<Customer> findByEstablishmentIdOrderByUpdatedAtDesc(Long establishmentId);

    @Query("""
    select c from Customer c
    where c.establishment.id=:establishmentId
      and (lower(c.name) like concat('%', :query, '%')
           or c.phoneNormalized like concat('%', :digits, '%'))
    order by c.updatedAt desc
  """)
    List<Customer> searchByEstablishment(@Param("establishmentId") Long establishmentId, @Param("query") String query, @Param("digits") String digits);
}
