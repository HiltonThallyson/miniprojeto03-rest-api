package com.jeanlima.springrestapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jeanlima.springrestapi.model.Estoque;

public interface EstoqueRepository extends JpaRepository<Estoque, Integer>{
    
}
