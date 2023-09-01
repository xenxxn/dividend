package com.example.dividend.persist;

import com.example.dividend.persist.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    boolean existsByTicker(String ticker);

    //optional 을 쓰는 이유는 NullPointerException 을 방지하기 위함
    Optional<CompanyEntity> findByName(String name);

    //Optional<CompanyEntity> findByTicker(String ticker);

}
