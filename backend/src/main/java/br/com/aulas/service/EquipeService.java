package br.com.aulas.service;

import br.com.aulas.dto.AlunoTurmaResponse;
import br.com.aulas.dto.EquipeRequest;
import br.com.aulas.dto.EquipeResponse;
import br.com.aulas.dto.TurmaResponse;
import br.com.aulas.exception.RegistroNaoEncontradoException;
import br.com.aulas.model.Equipe;
import br.com.aulas.model.TorneioEquipe;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.EquipeMembroRepository;
import br.com.aulas.repository.EquipeRepository;
import br.com.aulas.repository.TorneioEquipeRepository;
import br.com.aulas.repository.TorneioRepository;
import br.com.aulas.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final EquipeMembroRepository equipeMembroRepository;
    private final UsuarioRepository usuarioRepository;
    private final TorneioEquipeRepository torneioEquipeRepository;
    private final TorneioRepository torneioRepository;
    private final TurmaService turmaService;
    private final AuditoriaService auditoriaService;

    public EquipeService(EquipeRepository equipeRepository, EquipeMembroRepository equipeMembroRepository,
                         UsuarioRepository usuarioRepository, TorneioEquipeRepository torneioEquipeRepository,
                         TorneioRepository torneioRepository, TurmaService turmaService,
                         AuditoriaService auditoriaService) {
        this.equipeRepository = equipeRepository;
        this.equipeMembroRepository = equipeMembroRepository;
        this.usuarioRepository = usuarioRepository;
        this.torneioEquipeRepository = torneioEquipeRepository;
        this.torneioRepository = torneioRepository;
        this.turmaService = turmaService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<EquipeResponse> listar(Usuario usuario) {
        if ("admin".equals(usuario.getPerfil())) {
            return montarRespostas(equipeRepository.findAllByOrderByNomeAsc());
        }

        List<Long> turmas = turmaService.idsTurmasDoUsuario(usuario.getId());
        if (turmas.isEmpty()) {
            return List.of();
        }
        return montarRespostas(equipeRepository.findByIdTurmaInOrderByNomeAsc(turmas));
    }

    @Transactional(readOnly = true)
    public List<AlunoTurmaResponse> listarAlunos(Usuario usuario, Long turmaId) {
        validarAcessoTurma(usuario, turmaId);

        return usuarioRepository.findAllById(turmaService.idsUsuariosDaTurma(turmaId)).stream()
                .filter(aluno -> Boolean.TRUE.equals(aluno.getAtivo()) && "aluno".equals(aluno.getPerfil()))
                .sorted(Comparator.comparing(Usuario::getNome))
                .map(aluno -> new AlunoTurmaResponse(aluno.getId(), aluno.getNome(), aluno.getRgm(),
                        equipeMembroRepository.equipeDoUsuario(aluno.getId())))
                .toList();
    }

    @Transactional
    public EquipeResponse cadastrar(Usuario usuario, EquipeRequest dados) {
        validarAcessoTurma(usuario, dados.getTurmaId());

        String nome = dados.getNome().trim();
        if (equipeRepository.existsByIdTurmaAndNomeIgnoreCase(dados.getTurmaId(), nome)) {
            throw new IllegalArgumentException("Já existe uma equipe com esse nome nessa turma.");
        }
        validarMembros(dados, null);

        Equipe equipe = new Equipe();
        equipe.setNome(nome);
        equipe.setIdTurma(dados.getTurmaId());
        Equipe salva = equipeRepository.save(equipe);
        salvarMembros(salva.getId(), dados.getMembros());

        auditoriaService.registrar("EQUIPES", "EQUIPE_CRIADA",
                "Equipe " + salva.getId() + " - " + nome + " com " + dados.getMembros().size() + " integrante(s)", true);
        return montarRespostas(List.of(salva)).get(0);
    }

    @Transactional
    public EquipeResponse atualizar(Usuario usuario, Long id, EquipeRequest dados) {
        Equipe equipe = buscar(id);
        validarAcessoTurma(usuario, equipe.getIdTurma());
        validarAcessoTurma(usuario, dados.getTurmaId());

        if (estaEmTorneioAtivo(id)) {
            throw new IllegalArgumentException("A equipe está em um torneio que ainda não terminou e não pode ser alterada.");
        }

        String nome = dados.getNome().trim();
        if (equipeRepository.existsByIdTurmaAndNomeIgnoreCaseAndIdNot(dados.getTurmaId(), nome, id)) {
            throw new IllegalArgumentException("Já existe uma equipe com esse nome nessa turma.");
        }
        validarMembros(dados, id);

        equipe.setNome(nome);
        equipe.setIdTurma(dados.getTurmaId());
        equipeRepository.save(equipe);
        equipeMembroRepository.excluirMembros(id);
        salvarMembros(id, dados.getMembros());

        auditoriaService.registrar("EQUIPES", "EQUIPE_ALTERADA",
                "Equipe " + id + " - " + nome + " com " + dados.getMembros().size() + " integrante(s)", true);
        return montarRespostas(List.of(equipe)).get(0);
    }

    @Transactional
    public void excluir(Usuario usuario, Long id) {
        Equipe equipe = buscar(id);
        validarAcessoTurma(usuario, equipe.getIdTurma());

        if (torneioEquipeRepository.existsByIdEquipe(id)) {
            throw new IllegalArgumentException("A equipe já foi inscrita em um torneio e não pode ser excluída.");
        }

        equipeMembroRepository.excluirMembros(id);
        equipeRepository.delete(equipe);
        auditoriaService.registrar("EQUIPES", "EQUIPE_EXCLUIDA", "Equipe " + id + " - " + equipe.getNome(), true);
    }

    private void validarMembros(EquipeRequest dados, Long idEquipe) {
        List<Long> ids = dados.getMembros().stream().distinct().toList();
        if (ids.size() != dados.getMembros().size()) {
            throw new IllegalArgumentException("Um aluno não pode ser adicionado duas vezes na mesma equipe.");
        }

        List<Usuario> alunos = usuarioRepository.findAllById(ids);
        if (alunos.size() != ids.size()) {
            throw new IllegalArgumentException("Algum integrante selecionado não foi encontrado.");
        }

        for (Usuario aluno : alunos) {
            if (!Boolean.TRUE.equals(aluno.getAtivo()) || !"aluno".equals(aluno.getPerfil())) {
                throw new IllegalArgumentException("Somente alunos ativos podem fazer parte de uma equipe.");
            }
            if (!dados.getTurmaId().equals(turmaService.turmaDoAluno(aluno.getId()))) {
                throw new IllegalArgumentException("O aluno " + aluno.getNome() + " não é da turma selecionada.");
            }

            Long equipeAtual = equipeMembroRepository.equipeDoUsuario(aluno.getId());
            if (equipeAtual != null && !equipeAtual.equals(idEquipe)) {
                throw new IllegalArgumentException("O aluno " + aluno.getNome() + " já faz parte de outra equipe.");
            }
        }
    }

    private void salvarMembros(Long idEquipe, List<Long> membros) {
        for (Long idAluno : membros) {
            equipeMembroRepository.salvar(idEquipe, idAluno);
        }
    }

    private boolean estaEmTorneioAtivo(Long idEquipe) {
        for (TorneioEquipe inscricao : torneioEquipeRepository.findByIdEquipe(idEquipe)) {
            boolean ativo = torneioRepository.findById(inscricao.getIdTorneio())
                    .map(torneio -> !TorneioService.FINALIZADO.equals(torneio.getStatus()))
                    .orElse(false);
            if (ativo) {
                return true;
            }
        }
        return false;
    }

    private void validarAcessoTurma(Usuario usuario, Long turmaId) {
        if (turmaId != null && "admin".equals(usuario.getPerfil())) {
            turmaService.validarTurma(turmaId);
            return;
        }
        if (turmaId == null || !turmaService.idsTurmasDoUsuario(usuario.getId()).contains(turmaId)) {
            throw new AccessDeniedException("O professor não dá aula nessa turma.");
        }
    }

    private Equipe buscar(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Equipe não encontrada."));
    }

    private List<EquipeResponse> montarRespostas(List<Equipe> equipes) {
        if (equipes.isEmpty()) {
            return List.of();
        }

        Map<Long, String> turmas = turmaService.listar().stream()
                .collect(Collectors.toMap(TurmaResponse::id, TurmaResponse::nome));

        return equipes.stream()
                .map(equipe -> {
                    List<Long> idsMembros = equipeMembroRepository.idsUsuariosDaEquipe(equipe.getId());
                    Map<Long, String> nomes = usuarioRepository.findAllById(idsMembros).stream()
                            .collect(Collectors.toMap(Usuario::getId, Usuario::getNome));
                    return new EquipeResponse(
                            equipe.getId(),
                            equipe.getNome(),
                            equipe.getIdTurma(),
                            turmas.get(equipe.getIdTurma()),
                            idsMembros.stream().map(idMembro -> new EquipeResponse.Membro(idMembro, nomes.get(idMembro))).toList(),
                            torneioEquipeRepository.existsByIdEquipe(equipe.getId())
                    );
                })
                .toList();
    }
}
