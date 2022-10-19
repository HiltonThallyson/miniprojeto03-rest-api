package com.jeanlima.springrestapi.rest.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InformacoesClienteDTO {
    private String nome;
    private String cpf;
    private Set<InformacaoPedidoDTO> pedidos;
}
