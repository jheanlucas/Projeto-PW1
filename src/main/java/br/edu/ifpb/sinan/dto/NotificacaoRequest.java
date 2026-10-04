package br.edu.ifpb.sinan.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record NotificacaoRequest(
        @NotBlank @Size(max = 120) String agravo,
        @Size(max = 12) String cid10,
        @NotNull @PastOrPresent LocalDate dataNotificacao,
        @PastOrPresent LocalDate dataPrimeirosSintomas,
        @NotBlank @Size(max = 160) String nomePaciente,
        @PastOrPresent LocalDate dataNascimento,
        @PositiveOrZero Integer idadeValor,
        String idadeUnidade,
        @NotBlank String sexo,
        String gestante,
        @Size(max = 160) String nomeMae,
        Boolean resideBrasil,
        String ufResidencia,
        String municipioResidencia,
        String paisResidencia,
        @Size(max = 1000) String observacoes
) {}
