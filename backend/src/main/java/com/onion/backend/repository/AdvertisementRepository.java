package com.onion.backend.repository;


import com.onion.backend.entity.Advertisement;
import com.onion.backend.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    Page<Advertisement> findAllByIsDeletedIsFalse(Pageable pageable);
}
