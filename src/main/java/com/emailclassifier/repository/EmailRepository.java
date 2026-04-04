package com.emailclassifier.repository;

import com.emailclassifier.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {

    //custom query- To filter by classification label
    List<Email> findByClassificationOrderByCreatedAtDesc(String classification);

    //@Query JPQL - When method naming gets too complex — more readable
    @Query("SELECT e FROM Email e ORDER BY e.createdAt DESC")
    List<Email> findAllOrderByCreatedAtDesc();
}