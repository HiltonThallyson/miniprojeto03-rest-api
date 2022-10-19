package com.jeanlima.springrestapi.rest.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jeanlima.springrestapi.model.Cliente;
import com.jeanlima.springrestapi.model.ItemPedido;
import com.jeanlima.springrestapi.model.Pedido;
import com.jeanlima.springrestapi.repository.ClienteRepository;
import com.jeanlima.springrestapi.rest.dto.InformacaoItemPedidoDTO;
import com.jeanlima.springrestapi.rest.dto.InformacaoPedidoDTO;
import com.jeanlima.springrestapi.rest.dto.InformacoesClienteDTO;

@RequestMapping("/api/clientes")
@RestController //anotação especializadas de controller - todos já anotados com response body!
public class ClienteController {

    @Autowired
    private ClienteRepository clientes;

    @GetMapping("{id}")
    public Cliente getClienteById( @PathVariable Integer id ){
        return clientes
                .findById(id)
                .orElseThrow(() -> //se nao achar lança o erro!
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Cliente não encontrado"));
    }

    @GetMapping("info/{id}")
    public InformacoesClienteDTO getClienteInformation(@PathVariable Integer id) {
        return clientes.findById(id).map(c -> {
           return InformacoesClienteDTO.builder().cpf(c.getCpf()).nome(c.getNome()).pedidos(converter(c.getPedidos())).build();
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Set<InformacaoPedidoDTO> converter(Set<Pedido> pedidos) {
        if(pedidos.isEmpty()){
            return Collections.emptySet();
        }
        return pedidos.stream().map(p -> 
            InformacaoPedidoDTO.builder().id(p.getId()).total(p.getTotal()).itens(converter(p.getItens())).build()
        ).collect(Collectors.toSet());
    }

    private List<InformacaoItemPedidoDTO> converter(List<ItemPedido> itens) {
        if(itens.isEmpty()){
            return Collections.emptyList();
        }
        return itens.stream().map(item -> 
            InformacaoItemPedidoDTO.builder().descricaoProduto(item.getProduto().getDescricao()).quantidade(item.getQuantidade()).precoUnitario(item.getProduto().getPreco()).build()
        ).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente save( @RequestBody Cliente cliente ){
        return clientes.save(cliente);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete( @PathVariable Integer id ){
        clientes.findById(id)
                .map( cliente -> {
                    clientes.delete(cliente );
                    return cliente;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cliente não encontrado") );

    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update( @PathVariable Integer id,
                        @RequestBody Cliente cliente ){
        clientes
                .findById(id)
                .map( clienteExistente -> {
                    cliente.setId(clienteExistente.getId());
                    clientes.save(cliente);
                    return clienteExistente;
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Cliente não encontrado") );
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateByPatch( @PathVariable Integer id,
                        @RequestBody Cliente cliente ){
        clientes
                .findById(id)
                .map( clienteExistente -> {
                    cliente.setId(clienteExistente.getId());
                    if(cliente.getCpf() == null) cliente.setCpf(clienteExistente.getCpf());
                    if(cliente.getNome() == null) cliente.setNome(clienteExistente.getNome());
                    clientes.save(cliente);
                    return clienteExistente;
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Cliente não encontrado") );
    }

    @GetMapping
    public List<Cliente> find( Cliente filtro ){
        ExampleMatcher matcher = ExampleMatcher
                                    .matching()
                                    .withIgnoreCase()
                                    .withStringMatcher(
                                            ExampleMatcher.StringMatcher.CONTAINING );

        Example example = Example.of(filtro, matcher);
        return clientes.findAll(example);
    }

}
