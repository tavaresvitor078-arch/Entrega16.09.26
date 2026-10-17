package com.umc.pediupatrao.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @GetMapping
    public String listarPedidos(Model model) {
        model.addAttribute("pedidos", List.of());
        model.addAttribute("content", "pedidos/lista :: content");
        return "layout";
    }
}