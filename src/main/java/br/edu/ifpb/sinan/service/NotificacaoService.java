package br.edu.ifpb.sinan.service;

import br.edu.ifpb.sinan.dto.NotificacaoRequest;
import br.edu.ifpb.sinan.model.Notificacao;
import br.edu.ifpb.sinan.repository.NotificacaoRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificacaoService {
    private final NotificacaoRepository repository;

    public NotificacaoService(NotificacaoRepository repository) {
        this.repository = repository;
    }

    public Notificacao criar(NotificacaoRequest request) {
        validarRegras(request);
        Notificacao notificacao = new Notificacao();
        copiar(request, notificacao);
        return repository.save(notificacao);
    }

    public Notificacao atualizar(Long id, NotificacaoRequest request) {
        validarRegras(request);
        Notificacao notificacao = buscarPorId(id);
        copiar(request, notificacao);
        return repository.save(notificacao);
    }

    public Notificacao buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada."));
    }

    public void excluir(Long id) {
        Notificacao notificacao = buscarPorId(id);
        repository.delete(notificacao);
    }

    public Page<Notificacao> consultar(String agravo, String paciente, String uf,
                                       LocalDate dataInicial, LocalDate dataFinal,
                                       boolean duplicadas, int pagina, int tamanho,
                                       String ordenarPor, String ordem) {
        if (pagina < 1 || tamanho < 1 || tamanho > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "pagina deve ser >= 1 e tamanho deve estar entre 1 e 100.");
        }
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "dataInicial não pode ser posterior a dataFinal.");
        }

        Set<String> camposPermitidos = Set.of("id", "agravo", "dataNotificacao", "nomePaciente", "dataNascimento");
        if (!camposPermitidos.contains(ordenarPor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo de ordenação inválido.");
        }
        if (!ordem.equalsIgnoreCase("ASC") && !ordem.equalsIgnoreCase("DESC")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ordem deve ser ASC ou DESC.");
        }

        List<Notificacao> todos = repository.findAll();
        Set<Long> idsDuplicados = encontrarDuplicadas(todos);

        List<Notificacao> filtrados = todos.stream()
                .filter(n -> vazio(agravo) || normalizar(n.getAgravo()).contains(normalizar(agravo)))
                .filter(n -> vazio(paciente) || normalizar(n.getNomePaciente()).contains(normalizar(paciente)))
                .filter(n -> vazio(uf) || igual(n.getUfResidencia(), uf))
                .filter(n -> dataInicial == null || !n.getDataNotificacao().isBefore(dataInicial))
                .filter(n -> dataFinal == null || !n.getDataNotificacao().isAfter(dataFinal))
                .filter(n -> !duplicadas || idsDuplicados.contains(n.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        Comparator<Notificacao> comparator = comparator(ordenarPor);
        if (ordem.equalsIgnoreCase("DESC")) comparator = comparator.reversed();
        filtrados.sort(comparator);

        int inicio = Math.min((pagina - 1) * tamanho, filtrados.size());
        int fim = Math.min(inicio + tamanho, filtrados.size());
        return new PageImpl<>(filtrados.subList(inicio, fim),
                PageRequest.of(pagina - 1, tamanho), filtrados.size());
    }

    private Set<Long> encontrarDuplicadas(List<Notificacao> lista) {
        Set<Long> duplicados = new HashSet<>();
        for (int i = 0; i < lista.size(); i++) {
            Notificacao a = lista.get(i);
            if (!camposDuplicidadePreenchidos(a)) continue;
            for (int j = i + 1; j < lista.size(); j++) {
                Notificacao b = lista.get(j);
                if (!camposDuplicidadePreenchidos(b)) continue;
                boolean mesmosDados =
                        normalizar(a.getAgravo()).equals(normalizar(b.getAgravo())) &&
                        normalizar(a.getNomePaciente()).equals(normalizar(b.getNomePaciente())) &&
                        Objects.equals(a.getDataNascimento(), b.getDataNascimento()) &&
                        normalizar(a.getNomeMae()).equals(normalizar(b.getNomeMae()));
                long dias = Math.abs(ChronoUnit.DAYS.between(a.getDataNotificacao(), b.getDataNotificacao()));
                if (mesmosDados && dias <= 3) {
                    duplicados.add(a.getId());
                    duplicados.add(b.getId());
                }
            }
        }
        return duplicados;
    }

    private boolean camposDuplicidadePreenchidos(Notificacao n) {
        return !vazio(n.getAgravo()) && !vazio(n.getNomePaciente()) &&
                n.getDataNascimento() != null && !vazio(n.getNomeMae()) &&
                n.getDataNotificacao() != null;
    }

    private Comparator<Notificacao> comparator(String campo) {
        return switch (campo) {
            case "id" -> Comparator.comparing(Notificacao::getId);
            case "agravo" -> Comparator.comparing(Notificacao::getAgravo, String.CASE_INSENSITIVE_ORDER);
            case "nomePaciente" -> Comparator.comparing(Notificacao::getNomePaciente, String.CASE_INSENSITIVE_ORDER);
            case "dataNascimento" -> Comparator.comparing(Notificacao::getDataNascimento,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Notificacao::getDataNotificacao);
        };
    }

    private void validarRegras(NotificacaoRequest r) {
        boolean temNascimento = r.dataNascimento() != null;
        boolean informouValorIdade = r.idadeValor() != null;
        boolean informouUnidadeIdade = !vazio(r.idadeUnidade());
        boolean temIdadeCompleta = informouValorIdade && informouUnidadeIdade;

        // Campo 10 do SINAN: idade só é informada quando a data de nascimento é desconhecida.
        if (!temNascimento && !temIdadeCompleta) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe a data de nascimento ou, quando ela for desconhecida, a idade com sua unidade.");
        }
        if (temNascimento && (informouValorIdade || informouUnidadeIdade)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não informe idade quando a data de nascimento estiver preenchida.");
        }
        if (informouValorIdade != informouUnidadeIdade) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Idade e unidade da idade devem ser informadas juntas.");
        }
        if (!Set.of("M", "F", "I").contains(r.sexo().trim().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sexo deve ser M, F ou I.");
        }
        if (informouUnidadeIdade && !Set.of("HORA", "DIA", "MES", "ANO").contains(r.idadeUnidade().trim().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unidade da idade inválida.");
        }

        String gestante = Objects.toString(r.gestante(), "").trim().toUpperCase();
        Set<String> valoresGestante = Set.of("1_TRI", "2_TRI", "3_TRI", "IG_IGNORADA", "NAO", "NAO_SE_APLICA", "IGNORADO");
        if (vazio(gestante) || !valoresGestante.contains(gestante)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Gestante é obrigatório e deve possuir um valor válido.");
        }

        if ("M".equalsIgnoreCase(r.sexo().trim()) && !"NAO_SE_APLICA".equals(gestante)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Para paciente do sexo masculino, gestante deve ser NAO_SE_APLICA.");
        }
        if ("F".equalsIgnoreCase(r.sexo().trim()) && "NAO_SE_APLICA".equals(gestante)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Para paciente do sexo feminino, informe a situação gestacional ou IGNORADO.");
        }
        if ("I".equalsIgnoreCase(r.sexo().trim()) && !Set.of("NAO_SE_APLICA", "IGNORADO").contains(gestante)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Para sexo ignorado, gestante deve ser NAO_SE_APLICA ou IGNORADO.");
        }

        // RN03: resideBrasil e usado pelo formulario. Para manter compatibilidade com
        // clientes antigos, quando o campo nao vier no JSON inferimos pelo pais:
        // Brasil/vazio = residente no Brasil; outro pais = residente no exterior.
        boolean resideBrasil = resolverResidenciaBrasil(r);
        if (resideBrasil) {
            if (vazio(r.ufResidencia())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "UF de residência é obrigatória para residente no Brasil.");
            }
            if (vazio(r.municipioResidencia())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Município de residência é obrigatório quando a UF é informada.");
            }
            if (!vazio(r.paisResidencia()) && !"brasil".equals(normalizar(r.paisResidencia()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Para residente no Brasil, o país deve ser Brasil ou ficar em branco.");
            }
        } else {
            if (vazio(r.paisResidencia()) || "brasil".equals(normalizar(r.paisResidencia()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "País de residência é obrigatório e deve ser diferente de Brasil para residente no exterior.");
            }
            if (!vazio(r.ufResidencia()) || !vazio(r.municipioResidencia())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "UF e município de residência devem ficar em branco para residente no exterior.");
            }
        }
    }

    private void copiar(NotificacaoRequest r, Notificacao n) {
        n.setAgravo(r.agravo().trim());
        n.setCid10(r.cid10());
        n.setDataNotificacao(r.dataNotificacao());
        n.setDataPrimeirosSintomas(r.dataPrimeirosSintomas());
        n.setNomePaciente(r.nomePaciente().trim());
        n.setDataNascimento(r.dataNascimento());
        n.setIdadeValor(r.idadeValor());
        n.setIdadeUnidade(vazio(r.idadeUnidade()) ? null : r.idadeUnidade().trim().toUpperCase());
        n.setSexo(r.sexo().trim().toUpperCase());
        n.setGestante(vazio(r.gestante()) ? null : r.gestante().trim().toUpperCase());
        n.setNomeMae(vazio(r.nomeMae()) ? null : r.nomeMae().trim());
        boolean resideBrasil = resolverResidenciaBrasil(r);
        n.setResideBrasil(resideBrasil);
        n.setUfResidencia(vazio(r.ufResidencia()) ? null : r.ufResidencia().trim().toUpperCase());
        n.setMunicipioResidencia(vazio(r.municipioResidencia()) ? null : r.municipioResidencia().trim());
        n.setPaisResidencia(resideBrasil
                ? "Brasil"
                : r.paisResidencia().trim());
        n.setObservacoes(r.observacoes());
    }

    private boolean resolverResidenciaBrasil(NotificacaoRequest r) {
        if (r.resideBrasil() != null) return r.resideBrasil();
        return vazio(r.paisResidencia()) || "brasil".equals(normalizar(r.paisResidencia()));
    }

    private boolean vazio(String s) { return s == null || s.trim().isEmpty(); }
    private boolean igual(String a, String b) { return normalizar(a).equals(normalizar(b)); }
    private String normalizar(String s) {
        if (s == null) return "";
        String semAcentos = Normalizer.normalize(s.trim().replaceAll("\\s+", " "), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT);
    }
}
