package com.jeanlima.springrestapi.rest.dto;

import java.math.BigDecimal;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoDTO {
    private String descricao;
    private BigDecimal preco;
    private EstoqueDTO estoqueDTO;
}
