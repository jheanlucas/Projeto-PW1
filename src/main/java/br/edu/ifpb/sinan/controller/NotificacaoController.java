package br.edu.ifpb.sinan.controller;

import br.edu.ifpb.sinan.dto.NotificacaoRequest;
import br.edu.ifpb.sinan.model.Notificacao;
import br.edu.ifpb.sinan.service.NotificacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/notificacao")
public class NotificacaoController {
    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Notificacao> consultar(
            @RequestParam(required = false) String agravo,
            @RequestParam(required = false) String paciente,
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) LocalDate dataInicial,
            @RequestParam(required = false) LocalDate dataFinal,
            @RequestParam(defaultValue = "false") boolean duplicadas,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            @RequestParam(defaultValue = "dataNotificacao") String ordenarPor,
            @RequestParam(defaultValue = "ASC") String ordem) {
        return service.consultar(agravo, paciente, uf, dataInicial, dataFinal,
                duplicadas, pagina, tamanho, ordenarPor, ordem);
    }

    @GetMapping("/{id}")
    public Notificacao buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Notificacao criar(@Valid @RequestBody NotificacaoRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public Notificacao atualizar(@PathVariable Long id, @Valid @RequestBody NotificacaoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
