const API = '/notificacao';
let pagina = 1;
let ordenarPor = 'dataNotificacao';
let ordem = 'ASC';
const $ = id => document.getElementById(id);
const campos = ['agravo','cid10','dataNotificacao','dataPrimeirosSintomas','nomePaciente','dataNascimento','idadeValor','idadeUnidade','sexo','gestante','nomeMae','resideBrasil','ufResidencia','municipioResidencia','paisResidencia','observacoes'];

function valor(id) { return $(id).value.trim(); }
function mensagem(texto, erro=false) {
  $('mensagem').textContent = texto;
  $('mensagem').className = erro ? 'erro' : '';
}
function limparFormulario() {
  $('form-notificacao').reset();
  $('id').value = '';
  $('paisResidencia').value = 'Brasil';
  $('resideBrasil').value = 'true';
  $('titulo-form').textContent = 'Cadastrar notificação';
  mensagem('');
  if (typeof atualizarCamposCondicionais === 'function') atualizarCamposCondicionais();
}
$('form-notificacao').addEventListener('submit', async e => {
  e.preventDefault();
  const dados = {};
  campos.forEach(c => dados[c] = valor(c) || null);
  if (dados.idadeValor !== null) dados.idadeValor = Number(dados.idadeValor);
  dados.resideBrasil = dados.resideBrasil === 'true';
  const id = valor('id');
  try {
    const resposta = await fetch(id ? `${API}/${id}` : API, {
      method: id ? 'PUT' : 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify(dados)
    });
    if (!resposta.ok) {
      const problema = await resposta.json();
      throw new Error(problema.detail || problema.title || 'Não foi possível salvar.');
    }
    mensagem(id ? 'Notificação atualizada!' : 'Notificação cadastrada!');
    limparFormulario();
    consultar();
  } catch (erro) { mensagem(erro.message, true); }
});
$('limpar').addEventListener('click', limparFormulario);

async function consultar() {
  const params = new URLSearchParams({
    pagina, tamanho: 10, ordenarPor, ordem,
    duplicadas: $('f-duplicadas').checked
  });
  if (valor('f-agravo')) params.set('agravo', valor('f-agravo'));
  if (valor('f-paciente')) params.set('paciente', valor('f-paciente'));
  if (valor('f-uf')) params.set('uf', valor('f-uf'));
  if (valor('f-inicio')) params.set('dataInicial', valor('f-inicio'));
  if (valor('f-fim')) params.set('dataFinal', valor('f-fim'));
  try {
    const resposta = await fetch(`${API}?${params}`);
    if (!resposta.ok) throw new Error('Erro ao consultar notificações.');
    const resultado = await resposta.json();
    $('lista').innerHTML = '';
    resultado.content.forEach(n => {
      const tr = document.createElement('tr');
      [n.id,n.agravo,n.nomePaciente,n.dataNotificacao || '',n.dataNascimento || '',n.ufResidencia || ''].forEach(v => {
        const td = document.createElement('td'); td.textContent = v; tr.appendChild(td);
      });
      const acoes = document.createElement('td');
      const editar = document.createElement('button');
      editar.textContent = 'Editar'; editar.className = 'acao-tabela';
      editar.addEventListener('click', () => preencherEdicao(n));
      const excluir = document.createElement('button');
      excluir.textContent = 'Excluir'; excluir.className = 'acao-tabela excluir';
      excluir.addEventListener('click', () => excluirNotificacao(n.id));
      acoes.append(editar, excluir); tr.appendChild(acoes);
      $('lista').appendChild(tr);
    });
    $('pagina-atual').textContent = `Página ${resultado.number + 1} de ${Math.max(resultado.totalPages,1)} (${resultado.totalElements} registros)`;
    $('anterior').disabled = resultado.first;
    $('proxima').disabled = resultado.last || resultado.totalPages === 0;
  } catch (erro) { $('lista').innerHTML = `<tr><td colspan="7">${erro.message}</td></tr>`; }
}
function preencherEdicao(n) {
  $('id').value = n.id;
  campos.forEach(c => $(c).value = n[c] ?? '');
  $('resideBrasil').value = String(n.resideBrasil ?? true);
  atualizarCamposCondicionais();
  $('titulo-form').textContent = `Editar notificação #${n.id}`;
  window.scrollTo({top:0,behavior:'smooth'});
}
async function excluirNotificacao(id) {
  if (!confirm(`Deseja realmente excluir a notificação #${id}?`)) return;
  const resposta = await fetch(`${API}/${id}`, {method:'DELETE'});
  if (!resposta.ok) { alert('Não foi possível excluir.'); return; }
  consultar();
}
$('buscar').addEventListener('click', () => { pagina=1; consultar(); });
$('anterior').addEventListener('click', () => { if(pagina>1){pagina--;consultar();} });
$('proxima').addEventListener('click', () => { pagina++;consultar(); });
document.querySelectorAll('.ordem').forEach(botao => botao.addEventListener('click', () => {
  const campo = botao.dataset.sort;
  ordem = ordenarPor === campo && ordem === 'ASC' ? 'DESC' : 'ASC';
  ordenarPor = campo; pagina=1; consultar();
}));
consultar();

function atualizarCamposCondicionais() {
  const temNascimento = Boolean(valor('dataNascimento'));
  $('idadeValor').disabled = temNascimento;
  $('idadeUnidade').disabled = temNascimento;
  if (temNascimento) {
    $('idadeValor').value = '';
    $('idadeUnidade').value = '';
  }

  const brasil = $('resideBrasil').value === 'true';
  $('ufResidencia').disabled = !brasil;
  $('municipioResidencia').disabled = !brasil;
  if (brasil) {
    $('paisResidencia').value = 'Brasil';
    $('paisResidencia').readOnly = true;
  } else {
    $('ufResidencia').value = '';
    $('municipioResidencia').value = '';
    $('paisResidencia').readOnly = false;
    if (valor('paisResidencia').toLowerCase() === 'brasil') $('paisResidencia').value = '';
  }

  const sexo = valor('sexo');
  if (sexo === 'M') $('gestante').value = 'NAO_SE_APLICA';
}

$('dataNascimento').addEventListener('change', atualizarCamposCondicionais);
$('resideBrasil').addEventListener('change', atualizarCamposCondicionais);
$('sexo').addEventListener('change', atualizarCamposCondicionais);
atualizarCamposCondicionais();
