# Sistema de Notificações de Agravos – SINAN (atividade prática)

Projeto acadêmico de API REST com Spring Boot para cadastro, consulta, atualização e exclusão de notificações.

## Integrante
- Jhean Lucas Soares Albuquerque

## Tecnologias
- Java 21
- Spring Boot 3.5.6
- Spring Web, Spring Data JPA e Spring Validation
- H2 (banco em arquivo, sem instalação adicional)
- HTML, CSS e JavaScript puro

## Como executar
1. Instale o JDK 21 e o Maven.
2. Abra a pasta do projeto no IntelliJ IDEA ou VS Code.
3. Execute a classe `SinanApiApplication` ou rode no terminal:
   ```bash
   mvn spring-boot:run
   ```
4. Acesse `http://localhost:8080`.

O banco H2 é salvo na pasta `data/`. Para visualizar o console do banco, acesse `http://localhost:8080/h2-console` e use:
- JDBC URL: `jdbc:h2:file:./data/sinan`
- User: `sa`
- Password: (em branco)

## Endpoints

| Método | URI | Ação |
|---|---|---|
| GET | `/notificacao` | Listar e filtrar |
| GET | `/notificacao/{id}` | Consultar por ID |
| POST | `/notificacao` | Cadastrar |
| PUT | `/notificacao/{id}` | Atualizar |
| DELETE | `/notificacao/{id}` | Excluir |

### Filtros e paginação
Exemplo:
```http
GET /notificacao?agravo=dengue&paciente=maria&duplicadas=true&pagina=1&tamanho=10&ordenarPor=dataNotificacao&ordem=DESC
```

Filtros disponíveis: `agravo`, `paciente`, `uf`, `dataInicial`, `dataFinal`, `duplicadas`.
Paginação: `pagina` começa em 1; `tamanho` aceita de 1 a 100.
Ordenação: `id`, `agravo`, `nomePaciente`, `dataNascimento`, `dataNotificacao`; `ordem` aceita `ASC` ou `DESC`.

### Exemplo de JSON para cadastro
```json
{
  "agravo": "Dengue",
  "cid10": "A90",
  "dataNotificacao": "2026-03-12",
  "dataPrimeirosSintomas": "2026-03-10",
  "nomePaciente": "Maria da Silva",
  "dataNascimento": "1990-05-10",
  "sexo": "F",
  "gestante": "NAO",
  "nomeMae": "Ana da Silva",
  "resideBrasil": true,
  "ufResidencia": "PB",
  "municipioResidencia": "Cajazeiras",
  "paisResidencia": "Brasil"
}
```

O campo `idadeValor`/`idadeUnidade` (`HORA`, `DIA`, `MES` ou `ANO`) só deve ser enviado quando a data de nascimento for desconhecida. O campo `gestante` é obrigatório: para sexo feminino use `1_TRI`, `2_TRI`, `3_TRI`, `IG_IGNORADA`, `NAO` ou `IGNORADO`; para sexo masculino use `NAO_SE_APLICA`. Para residência, envie `resideBrasil=true` com UF e município; para residente no exterior, envie `resideBrasil=false`, deixe UF/município vazios e informe `paisResidencia`.

## Regra de duplicidade
O filtro `duplicadas=true` retorna notificações que possuem ao menos outra notificação com agravo, paciente, nascimento e nome da mãe iguais (texto normalizado sem diferenciar maiúsculas/minúsculas, acentos ou espaços extras), com diferença de até 3 dias entre as datas de notificação. Registros com algum campo-chave vazio não entram na comparação.

## Observações importantes
- A implementação é uma base acadêmica para a atividade e não um sistema oficial do Ministério da Saúde.
- A RN01 é calculada em memória após carregar os registros. Para grandes volumes, essa regra pode ser otimizada no banco de dados.
- A RN02 segue a regra de que idade substitui a data de nascimento apenas quando esta for desconhecida e valida o preenchimento obrigatório do campo gestante conforme o sexo.
- A RN03 usa `resideBrasil` para distinguir de forma explícita residentes no Brasil de residentes no exterior, evitando ambiguidade na validação de UF, município e país.
- O projeto também implementa o desafio opcional de paginação e ordenação.
