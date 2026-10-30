package br.com.aulas.service;

import br.com.aulas.dto.TorneioDetalheResponse;
import br.com.aulas.dto.TorneioDetalheResponse.EquipeTorneio;
import br.com.aulas.dto.TorneioDetalheResponse.LutaResponse;
import br.com.aulas.dto.TorneioDetalheResponse.Posicao;
import br.com.aulas.dto.TorneioRequest;
import br.com.aulas.dto.TorneioResponse;
import br.com.aulas.dto.TurmaResponse;
import br.com.aulas.exception.RegistroNaoEncontradoException;
import br.com.aulas.model.Equipe;
import br.com.aulas.model.Luta;
import br.com.aulas.model.Torneio;
import br.com.aulas.model.TorneioEquipe;
import br.com.aulas.model.Usuario;
import br.com.aulas.repository.EquipeRepository;
import br.com.aulas.repository.LutaRepository;
import br.com.aulas.repository.TorneioEquipeRepository;
import br.com.aulas.repository.TorneioRepository;
import br.com.aulas.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TorneioService {

    public static final String ABERTO = "Aberto";
    public static final String ANDAMENTO = "Andamento";
    public static final String FINALIZADO = "Finalizado";

    private final TorneioRepository torneioRepository;
    private final TorneioEquipeRepository torneioEquipeRepository;
    private final LutaRepository lutaRepository;
    private final EquipeRepository equipeRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaService turmaService;
    private final AuditoriaService auditoriaService;

    public TorneioService(TorneioRepository torneioRepository, TorneioEquipeRepository torneioEquipeRepository,
                          LutaRepository lutaRepository, EquipeRepository equipeRepository,
                          UsuarioRepository usuarioRepository, TurmaService turmaService,
                          AuditoriaService auditoriaService) {
        this.torneioRepository = torneioRepository;
        this.torneioEquipeRepository = torneioEquipeRepository;
        this.lutaRepository = lutaRepository;
        this.equipeRepository = equipeRepository;
        this.usuarioRepository = usuarioRepository;
        this.turmaService = turmaService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<TorneioResponse> listar(Usuario usuario) {
        return torneioRepository.findAllByOrderByDtInicioDescIdDesc().stream()
                .map(torneio -> toResponse(torneio, usuario))
                .toList();
    }

    @Transactional(readOnly = true)
    public TorneioDetalheResponse detalhar(Usuario usuario, Long id) {
        Torneio torneio = buscar(id);
        List<Equipe> equipes = equipesDoTorneio(id);
        Map<Long, EquipeTorneio> mapaEquipes = montarMapaEquipes(equipes);
        List<Luta> lutas = lutaRepository.findByIdTorneioOrderByFaseAscOrdemAsc(id);
        int totalFases = totalFases(lutas);

        List<LutaResponse> lutasResposta = lutas.stream()
                .map(luta -> new LutaResponse(
                        luta.getId(),
                        luta.getFase(),
                        luta.getOrdem(),
                        mapaEquipes.get(luta.getIdEquipeA()),
                        mapaEquipes.get(luta.getIdEquipeB()),
                        luta.getIdVencedor(),
                        lutaApta(luta)
                ))
                .toList();

        boolean podeLancarResultado = ANDAMENTO.equals(torneio.getStatus()) && podeLancarResultado(usuario, torneio, equipes);

        return new TorneioDetalheResponse(
                toResponse(torneio, usuario),
                new ArrayList<>(mapaEquipes.values()),
                lutasResposta,
                montarRanking(lutas, totalFases, mapaEquipes),
                totalFases,
                podeLancarResultado
        );
    }

    @Transactional
    public TorneioResponse cadastrar(Usuario usuario, TorneioRequest dados) {
        validarDatas(dados);

        Torneio torneio = new Torneio();
        preencher(torneio, dados);
        torneio.setStatus(ABERTO);
        torneio.setIdProfessor(usuario.getId());
        Torneio salvo = torneioRepository.save(torneio);

        auditoriaService.registrar("TORNEIOS", "TORNEIO_CRIADO", "Torneio " + salvo.getId() + " - " + salvo.getNome(), true);
        return toResponse(salvo, usuario);
    }

    @Transactional
    public TorneioResponse atualizar(Usuario usuario, Long id, TorneioRequest dados) {
        Torneio torneio = buscar(id);
        validarResponsavel(usuario, torneio);
        validarAberto(torneio);
        validarDatas(dados);

        preencher(torneio, dados);
        Torneio salvo = torneioRepository.save(torneio);

        auditoriaService.registrar("TORNEIOS", "TORNEIO_ALTERADO", "Torneio " + id + " - " + salvo.getNome(), true);
        return toResponse(salvo, usuario);
    }

    @Transactional
    public TorneioDetalheResponse definirEquipes(Usuario usuario, Long id, List<Long> idsEquipes) {
        Torneio torneio = buscar(id);
        validarResponsavel(usuario, torneio);
        validarAberto(torneio);

        List<Long> ids = idsEquipes == null ? List.of() : idsEquipes.stream().distinct().toList();
        List<Equipe> equipes = equipeRepository.findAllById(ids);
        if (equipes.size() != ids.size()) {
            throw new IllegalArgumentException("Alguma equipe selecionada não foi encontrada.");
        }

        if (!"admin".equals(usuario.getPerfil())) {
            List<Long> turmasProfessor = turmaService.idsTurmasDoUsuario(usuario.getId());
            for (Equipe equipe : equipes) {
                if (!turmasProfessor.contains(equipe.getIdTurma())) {
                    throw new AccessDeniedException("O professor só pode selecionar equipes das turmas em que dá aula.");
                }
            }
        }

        torneioEquipeRepository.deleteByIdTorneio(id);
        for (Long idEquipe : ids) {
            torneioEquipeRepository.save(new TorneioEquipe(id, idEquipe));
        }

        auditoriaService.registrar("TORNEIOS", "TORNEIO_EQUIPES",
                "Torneio " + id + " - " + ids.size() + " equipe(s) selecionada(s)", true);
        return detalhar(usuario, id);
    }

    @Transactional
    public TorneioDetalheResponse gerarChaveamento(Usuario usuario, Long id) {
        Torneio torneio = buscar(id);
        validarResponsavel(usuario, torneio);
        validarAberto(torneio);

        List<Long> equipes = new ArrayList<>(torneioEquipeRepository.findByIdTorneio(id).stream()
                .map(TorneioEquipe::getIdEquipe)
                .toList());
        if (equipes.size() < 2) {
            throw new IllegalArgumentException("Selecione pelo menos 2 equipes para gerar o chaveamento.");
        }
        Collections.shuffle(equipes);

        int vagas = 1;
        int totalFases = 0;
        while (vagas < equipes.size()) {
            vagas = vagas * 2;
            totalFases++;
        }

        for (int fase = 1; fase <= totalFases; fase++) {
            int quantidadeLutas = vagas / (int) Math.pow(2, fase);
            for (int ordem = 1; ordem <= quantidadeLutas; ordem++) {
                Luta luta = new Luta();
                luta.setIdTorneio(id);
                luta.setFase(fase);
                luta.setOrdem(ordem);
                lutaRepository.save(luta);
            }
        }

        int lutasPrimeiraFase = vagas / 2;
        int quantidadeFolgas = vagas - equipes.size();
        List<Integer> lutasComFolga = new ArrayList<>();
        for (int ordem = 1; ordem <= lutasPrimeiraFase && lutasComFolga.size() < quantidadeFolgas; ordem += 2) {
            lutasComFolga.add(ordem);
        }
        for (int ordem = 2; ordem <= lutasPrimeiraFase && lutasComFolga.size() < quantidadeFolgas; ordem += 2) {
            lutasComFolga.add(ordem);
        }

        int proximaEquipe = 0;
        for (int ordem = 1; ordem <= lutasPrimeiraFase; ordem++) {
            Luta luta = buscarLuta(id, 1, ordem);
            luta.setIdEquipeA(equipes.get(proximaEquipe++));
            if (!lutasComFolga.contains(ordem)) {
                luta.setIdEquipeB(equipes.get(proximaEquipe++));
            }
            lutaRepository.save(luta);
        }

        for (Integer ordem : lutasComFolga) {
            Luta luta = buscarLuta(id, 1, ordem);
            avancarVencedor(luta, luta.getIdEquipeA(), totalFases);
        }

        torneio.setStatus(ANDAMENTO);
        torneioRepository.save(torneio);

        auditoriaService.registrar("TORNEIOS", "TORNEIO_CHAVEAMENTO",
                "Torneio " + id + " - chaveamento gerado com " + equipes.size() + " equipes e " + totalFases + " fase(s)", true);
        return detalhar(usuario, id);
    }

    @Transactional
    public TorneioDetalheResponse registrarVencedor(Usuario usuario, Long idLuta, Long idEquipe) {
        Luta luta = lutaRepository.findById(idLuta)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Luta não encontrada."));
        Torneio torneio = buscar(luta.getIdTorneio());

        if (!ANDAMENTO.equals(torneio.getStatus())) {
            throw new IllegalArgumentException("O torneio não está em andamento.");
        }
        if (!podeLancarResultado(usuario, torneio, equipesDoTorneio(torneio.getId()))) {
            throw new AccessDeniedException("Somente o administrador ou professores das turmas do torneio podem registrar o vencedor.");
        }
        if (!lutaApta(luta)) {
            throw new IllegalArgumentException("Essa luta não está apta para receber resultado.");
        }
        if (idEquipe == null || (!idEquipe.equals(luta.getIdEquipeA()) && !idEquipe.equals(luta.getIdEquipeB()))) {
            throw new IllegalArgumentException("O vencedor deve ser uma das equipes da luta.");
        }

        int totalFases = totalFases(lutaRepository.findByIdTorneioOrderByFaseAscOrdemAsc(torneio.getId()));
        avancarVencedor(luta, idEquipe, totalFases);

        String nomeVencedor = equipeRepository.findById(idEquipe).map(Equipe::getNome).orElse(String.valueOf(idEquipe));
        auditoriaService.registrar("TORNEIOS", "LUTA_RESULTADO",
                "Torneio " + torneio.getId() + ", fase " + luta.getFase() + ", luta " + luta.getOrdem() + ": vencedor " + nomeVencedor, true);

        if (luta.getFase() == totalFases) {
            torneio.setStatus(FINALIZADO);
            torneioRepository.save(torneio);
            auditoriaService.registrar("TORNEIOS", "TORNEIO_FINALIZADO",
                    "Torneio " + torneio.getId() + " - campeão " + nomeVencedor, true);
        }

        return detalhar(usuario, torneio.getId());
    }

    @Transactional
    public void excluir(Long id) {
        Torneio torneio = buscar(id);
        torneioRepository.delete(torneio);
        auditoriaService.registrar("TORNEIOS", "TORNEIO_EXCLUIDO", "Torneio " + id + " - " + torneio.getNome(), true);
    }

    private void avancarVencedor(Luta luta, Long idVencedor, int totalFases) {
        luta.setIdVencedor(idVencedor);
        luta.setDtResultado(LocalDateTime.now());
        lutaRepository.save(luta);

        if (luta.getFase() < totalFases) {
            int ordemProximaLuta = (luta.getOrdem() + 1) / 2;
            Luta proximaLuta = buscarLuta(luta.getIdTorneio(), luta.getFase() + 1, ordemProximaLuta);
            if (luta.getOrdem() % 2 == 1) {
                proximaLuta.setIdEquipeA(idVencedor);
            } else {
                proximaLuta.setIdEquipeB(idVencedor);
            }
            lutaRepository.save(proximaLuta);
        }
    }

    private List<Posicao> montarRanking(List<Luta> lutas, int totalFases, Map<Long, EquipeTorneio> mapaEquipes) {
        List<Posicao> ranking = new ArrayList<>();

        for (Luta luta : lutas) {
            if (luta.getIdVencedor() == null || luta.getIdEquipeA() == null || luta.getIdEquipeB() == null) {
                continue;
            }

            Long perdedor = luta.getIdVencedor().equals(luta.getIdEquipeA()) ? luta.getIdEquipeB() : luta.getIdEquipeA();
            int posicaoPerdedor = (int) Math.pow(2, totalFases - luta.getFase()) + 1;
            ranking.add(novaPosicao(posicaoPerdedor, perdedor, mapaEquipes));

            if (luta.getFase() == totalFases) {
                ranking.add(novaPosicao(1, luta.getIdVencedor(), mapaEquipes));
            }
        }

        ranking.sort(Comparator.comparingInt(Posicao::posicao).thenComparing(Posicao::equipe));
        return ranking;
    }

    private Posicao novaPosicao(int posicao, Long idEquipe, Map<Long, EquipeTorneio> mapaEquipes) {
        EquipeTorneio equipe = mapaEquipes.get(idEquipe);
        return new Posicao(posicao, idEquipe, equipe != null ? equipe.nome() : "-", equipe != null ? equipe.turma() : "-");
    }

    private boolean lutaApta(Luta luta) {
        return luta.getIdEquipeA() != null && luta.getIdEquipeB() != null && luta.getIdVencedor() == null;
    }

    private int totalFases(List<Luta> lutas) {
        return lutas.stream().mapToInt(Luta::getFase).max().orElse(0);
    }

    private boolean podeLancarResultado(Usuario usuario, Torneio torneio, List<Equipe> equipes) {
        if ("admin".equals(usuario.getPerfil())) {
            return true;
        }
        if (!"professor".equals(usuario.getPerfil())) {
            return false;
        }
        if (torneio.getIdProfessor().equals(usuario.getId())) {
            return true;
        }

        List<Long> turmasProfessor = turmaService.idsTurmasDoUsuario(usuario.getId());
        return equipes.stream().anyMatch(equipe -> turmasProfessor.contains(equipe.getIdTurma()));
    }

    private void validarResponsavel(Usuario usuario, Torneio torneio) {
        if (!"admin".equals(usuario.getPerfil()) && !torneio.getIdProfessor().equals(usuario.getId())) {
            throw new AccessDeniedException("Somente o administrador ou o professor que criou o torneio pode alterá-lo.");
        }
    }

    private void validarAberto(Torneio torneio) {
        if (!ABERTO.equals(torneio.getStatus())) {
            throw new IllegalArgumentException("O chaveamento deste torneio já foi gerado e ele não pode mais ser alterado.");
        }
    }

    private void validarDatas(TorneioRequest dados) {
        if (dados.getDtFim() != null && dados.getDtFim().isBefore(dados.getDtInicio())) {
            throw new IllegalArgumentException("A data de término não pode ser anterior à data de início.");
        }
    }

    private void preencher(Torneio torneio, TorneioRequest dados) {
        torneio.setNome(dados.getNome().trim());
        torneio.setLocal(dados.getLocal() == null || dados.getLocal().isBlank() ? null : dados.getLocal().trim());
        torneio.setDtInicio(dados.getDtInicio());
        torneio.setDtFim(dados.getDtFim());
        torneio.setRegras(dados.getRegras() == null || dados.getRegras().isBlank() ? null : dados.getRegras().trim());
    }

    private Torneio buscar(Long id) {
        return torneioRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Torneio não encontrado."));
    }

    private Luta buscarLuta(Long idTorneio, int fase, int ordem) {
        return lutaRepository.findByIdTorneioAndFaseAndOrdem(idTorneio, fase, ordem)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Luta não encontrada."));
    }

    private List<Equipe> equipesDoTorneio(Long idTorneio) {
        List<Long> ids = torneioEquipeRepository.findByIdTorneio(idTorneio).stream()
                .map(TorneioEquipe::getIdEquipe)
                .toList();
        return equipeRepository.findAllById(ids);
    }

    private Map<Long, EquipeTorneio> montarMapaEquipes(List<Equipe> equipes) {
        Map<Long, String> turmas = turmaService.listar().stream()
                .collect(Collectors.toMap(TurmaResponse::id, TurmaResponse::nome));

        Map<Long, EquipeTorneio> mapa = new LinkedHashMap<>();
        equipes.stream()
                .sorted(Comparator.comparing(Equipe::getNome))
                .forEach(equipe -> mapa.put(equipe.getId(),
                        new EquipeTorneio(equipe.getId(), equipe.getNome(), turmas.get(equipe.getIdTurma()))));
        return mapa;
    }

    private TorneioResponse toResponse(Torneio torneio, Usuario usuario) {
        String professor = usuarioRepository.findById(torneio.getIdProfessor()).map(Usuario::getNome).orElse("-");
        boolean podeGerenciar = ("admin".equals(usuario.getPerfil()) || torneio.getIdProfessor().equals(usuario.getId()))
                && ABERTO.equals(torneio.getStatus());

        return new TorneioResponse(
                torneio.getId(),
                torneio.getNome(),
                torneio.getLocal(),
                torneio.getDtInicio(),
                torneio.getDtFim(),
                torneio.getRegras(),
                torneio.getStatus(),
                professor,
                torneioEquipeRepository.countByIdTorneio(torneio.getId()),
                podeGerenciar
        );
    }
}
