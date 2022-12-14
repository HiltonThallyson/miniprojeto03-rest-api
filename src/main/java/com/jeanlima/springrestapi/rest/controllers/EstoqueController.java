package com.jeanlima.springrestapi.rest.controllers;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jeanlima.springrestapi.model.Estoque;
import com.jeanlima.springrestapi.model.Produto;
import com.jeanlima.springrestapi.repository.EstoqueRepository;
import com.jeanlima.springrestapi.repository.ProdutoRepository;
import com.jeanlima.springrestapi.rest.dto.EstoqueDTO;

@RestController
@RequestMapping("api/estoque")
public class EstoqueController {
    
    @Autowired
    private EstoqueRepository eRepository;
    

    // @Autowired
    // private ProdutoRepository produtoRepository;

    // @PostMapping
    // public Estoque save(@RequestBody EstoqueDTO estoqueDTO) {
    //     Estoque estoque = new Estoque();
    //     Optional<Produto> produto = produtoRepository.findById(estoqueDTO.getProduto_id());

    //     if(produto.isPresent()){
    //         estoque.setQuantidade(estoqueDTO.getQuantidade());
    //         estoque.setProduto(produto.get());

    //     }
    //     return eRepository.save(estoque);
    // }

    @GetMapping
    public List<Estoque> find(Estoque filtro ){
        ExampleMatcher matcher = ExampleMatcher
                                    .matching()
                                    .withIgnoreCase()
                                    .withStringMatcher(
                                            ExampleMatcher.StringMatcher.CONTAINING );

        Example<Estoque> example = Example.of(filtro, matcher);
        return eRepository.findAll(example);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEstoque(@PathVariable Integer id, @RequestBody Estoque estoque) {
        eRepository.findById(id).map(estoqueExistente -> {
            estoque.setId(estoqueExistente.getId());
            eRepository.save(estoque);
            return estoqueExistente;
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque n??o encontrado"));
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEstoqueByPatch(@PathVariable Integer id, @RequestBody Estoque estoque) {
        eRepository.findById(id).map(estoqueExistente -> {
            estoque.setId(estoqueExistente.getId());
            if(estoque.getProduto() == null) estoque.setProduto(estoqueExistente.getProduto());
            if(estoque.getQuantidade() == null) estoque.setQuantidade(estoqueExistente.getQuantidade());
            eRepository.save(estoque);
            return estoqueExistente;
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque n??o encontrado"));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Integer id) {
        eRepository.findById(id)
        .map(estoque -> {
            eRepository.delete(estoque);
            return eRepository;
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque n??o encontrado"));
    }
}
