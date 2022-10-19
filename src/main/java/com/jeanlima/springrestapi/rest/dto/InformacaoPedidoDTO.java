package com.jeanlima.springrestapi.rest.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InformacaoPedidoDTO {
    private Integer id;
    private BigDecimal total;
    private List<InformacaoItemPedidoDTO> itens;
}