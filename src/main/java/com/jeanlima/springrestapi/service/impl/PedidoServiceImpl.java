package com.jeanlima.springrestapi.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.jeanlima.springrestapi.enums.StatusPedido;
import com.jeanlima.springrestapi.exception.PedidoNaoEncontradoException;
import com.jeanlima.springrestapi.exception.RegraNegocioException;
import com.jeanlima.springrestapi.model.Cliente;
import com.jeanlima.springrestapi.model.Estoque;
import com.jeanlima.springrestapi.model.ItemPedido;
import com.jeanlima.springrestapi.model.Pedido;
import com.jeanlima.springrestapi.model.Produto;
import com.jeanlima.springrestapi.repository.ClienteRepository;
import com.jeanlima.springrestapi.repository.EstoqueRepository;
import com.jeanlima.springrestapi.repository.ItemPedidoRepository;
import com.jeanlima.springrestapi.repository.PedidoRepository;
import com.jeanlima.springrestapi.repository.ProdutoRepository;
import com.jeanlima.springrestapi.rest.controllers.EstoqueController;
import com.jeanlima.springrestapi.rest.dto.ItemPedidoDTO;
import com.jeanlima.springrestapi.rest.dto.PedidoDTO;
import com.jeanlima.springrestapi.service.PedidoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;
    private final ClienteRepository clientesRepository;
    private final ProdutoRepository produtosRepository;
    private final ItemPedidoRepository itemsPedidoRepository;
    private final EstoqueRepository estoqueRepository;

    private final EstoqueController estoqueController;

    @Override
    @Transactional
    public Pedido salvar(PedidoDTO dto) {
        Integer idCliente = dto.getCliente();
        Cliente cliente = clientesRepository
                .findById(idCliente)
                .orElseThrow(() -> new RegraNegocioException("C??digo de cliente inv??lido."));

        Pedido pedido = new Pedido();

        pedido.setDataPedido(LocalDate.now());
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.REALIZADO);

        List<ItemPedido> itemsPedido = converterItems(pedido, dto.getItems());
        List<ItemPedido> confirmedItemsPedido = new ArrayList<ItemPedido>();
        for (ItemPedido itemPedido : itemsPedido) {
            Integer quantityOfProductInEstoque = 0;
            for (Estoque estoque : estoqueRepository.findAll()) {
                if (estoque.getProduto().getId() == itemPedido.getProduto().getId()) {
                    quantityOfProductInEstoque = estoque.getQuantidade();
                    if (itemPedido.getQuantidade().compareTo(quantityOfProductInEstoque) > 0) {
                        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                                "Quantidade do produto acima do estoque");
                    }
                    estoque.setQuantidade(quantityOfProductInEstoque - itemPedido.getQuantidade());
                    estoqueController.updateEstoqueByPatch(estoque.getId(), estoque);
                }
            }
            confirmedItemsPedido.add(itemPedido);
        }
        pedido.setItens(confirmedItemsPedido);
        pedido.setTotal();
        repository.save(pedido);
        itemsPedidoRepository.saveAll(itemsPedido);
        pedido.setItens(itemsPedido);
        return pedido;
    }

    private List<ItemPedido> converterItems(Pedido pedido, List<ItemPedidoDTO> items) {
        if (items.isEmpty()) {
            throw new RegraNegocioException("N??o ?? poss??vel realizar um pedido sem items.");
        }

        return items
                .stream()
                .map(dto -> {
                    Integer idProduto = dto.getProduto();
                    Produto produto = produtosRepository
                            .findById(idProduto)
                            .orElseThrow(
                                    () -> new RegraNegocioException(
                                            "C??digo de produto inv??lido: " + idProduto));

                    ItemPedido itemPedido = new ItemPedido();
                    itemPedido.setQuantidade(dto.getQuantidade());
                    itemPedido.setPedido(pedido);
                    itemPedido.setProduto(produto);
                    return itemPedido;
                }).collect(Collectors.toList());

    }

    @Override
    public Optional<Pedido> obterPedidoCompleto(Integer id) {

        return repository.findByIdFetchItens(id);
    }

    @Override
    public void atualizaStatus(Integer id, StatusPedido statusPedido) {
        repository
                .findById(id)
                .map(pedido -> {
                    pedido.setStatus(statusPedido);
                    return repository.save(pedido);
                }).orElseThrow(() -> new PedidoNaoEncontradoException());

    }

    @Override
    public void deletarPedidoById(Integer id) {
        repository.findById(id).map(p -> {
            repository.deleteById(id);
            return p;
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Override
    public void atualizarPedido(Integer id, Pedido pedido) {
        repository.findById(id).map(pedidoExistente -> {
            pedido.setId(pedidoExistente.getId());
            if(pedido.getCliente() == null) {
                pedido.setCliente(pedidoExistente.getCliente());
            }else {
                Cliente novoCliente = clientesRepository.findById(pedido.getCliente().getId()).map(cliente -> {
                    return cliente;
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente n??o encontrado"));
                Cliente clienteExistente = clientesRepository.findById(pedidoExistente.getCliente().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                clienteExistente.getPedidos().remove(pedido);
                clientesRepository.save(clienteExistente);
                pedido.setCliente(novoCliente);
            }
            if(pedido.getItens() == null) pedido.setItens(pedidoExistente.getItens());
            if(pedido.getDataPedido() == null) pedido.setDataPedido(pedidoExistente.getDataPedido());
            if(pedido.getStatus() == null) {
                pedido.setStatus(pedidoExistente.getStatus());
            }else {
                atualizaStatus(id, pedido.getStatus());
            }
            if(pedido.getTotal() == null) pedido.setTotal();
            repository.save(pedido);
            return pedidoExistente;
            
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido n??o encontrado"));
        
    }

    

}
