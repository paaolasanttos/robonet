import { useEffect, useMemo, useState } from "react";
import { Link, NavLink } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { menuApi } from "../servicos/api";

const paginasAluno = new Set(Object.keys(import.meta.glob("../pagina/*Aluno.jsx")));

const opcoes = [
  { chave: "dashboard", rotulo: "Dashboard", rota: "/home", aliases: ["home", "dashboard", "pagina inicial"] },
  { chave: "aulas", rotulo: "Aulas", rota: "/aulas", rotaAluno: "/aulas-aluno", arquivoAluno: "../pagina/aulasAluno.jsx", aliases: ["aulas", "aulas aluno", "aulasaluno"] },
  { chave: "simulados", rotulo: "Simulados", rota: "/simulados", rotaAluno: "/simulados-aluno", arquivoAluno: "../pagina/simuladosAluno.jsx", aliases: ["simulados", "simulados aluno", "simuladosaluno"] },
  { chave: "torneios", rotulo: "Torneios", rota: "/torneios", aliases: ["torneios"] },
  { chave: "meus-dados", rotulo: "Meus dados", rota: "/meus-dados", aliases: ["meus dados", "meus-dados", "meusdados"] },
  { chave: "turmas", rotulo: "Turmas", rota: "/turmas", somenteAdmin: true, aliases: ["turmas"] },
  { chave: "usuarios", rotulo: "Usuários", rota: "/usuarios", somenteAdmin: true, aliases: ["usuarios"] },
  { chave: "auditoria", rotulo: "Auditoria", rota: "/auditoria", somenteAdmin: true, aliases: ["auditoria", "auditorias"] },
];

function normalizar(valor) {
  return String(valor || "")
    .trim()
    .replace(/\.jsx?$/i, "")
    .split(/[\\/]/)
    .pop()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[_-]/g, " ")
    .replace(/\s+/g, " ");
}

function resolverOpcao(menu) {
  const chaveNormalizada = normalizar(menu);
  return opcoes.find((opcao) => opcao.aliases.some((alias) => normalizar(alias) === chaveNormalizada));
}

export default function MenuPadrao() {
  const { session, logout } = useAuth();
  const perfilUsuario = normalizar(session?.usuario?.perfil);
  const perfil = perfilUsuario === "administrador" ? "admin" : perfilUsuario;
  const [menus, setMenus] = useState([]);
  const [erro, setErro] = useState("");

  useEffect(() => {
    let ativo = true;
    menuApi.listar()
      .then((resultado) => {
        if (ativo) setMenus(Array.isArray(resultado) ? resultado : []);
      })
      .catch((error) => {
        if (ativo) setErro(error.message || "Não foi possível carregar as opções do menu.");
      });
    return () => { ativo = false; };
  }, []);

  const itens = useMemo(() => {
    const vistos = new Set();
    return menus.flatMap((menu) => {
      const opcao = resolverOpcao(menu);
      if (!opcao || (opcao.somenteAdmin && perfil !== "admin") || vistos.has(opcao.chave)) return [];
      vistos.add(opcao.chave);
      const caminhoAluno = opcao.rotaAluno && paginasAluno.has(opcao.arquivoAluno);
      return [{ ...opcao, destino: perfil === "aluno" && caminhoAluno ? opcao.rotaAluno : opcao.rota }];
    });
  }, [menus, perfil]);

  return (
    <nav className="navbar" aria-label="Navegação principal">
      <Link className="navbar-brand" to="/home" aria-label="ROBONET, página inicial">
        <span className="brand-mark">R</span>
        <span><strong>ROBONET</strong></span>
      </Link>
      <div className="navbar-menu">
        {itens.map((item) => (
          <NavLink
            key={item.chave}
            className={({ isActive }) => `navbar-link${isActive ? " navbar-link-active" : ""}`}
            to={item.destino}
          >
            {item.rotulo}
          </NavLink>
        ))}
        {erro && <span className="navbar-menu-error" role="alert">{erro}</span>}
      </div>
      <div className="navbar-user">
        <div className="user-copy">
          <strong>{session?.usuario?.nome || "Usuário"}</strong>
          <span>{session?.usuario?.perfil || "perfil"}</span>
        </div>
        <button className="logout-button" onClick={logout}>Sair</button>
      </div>
    </nav>
  );
}
