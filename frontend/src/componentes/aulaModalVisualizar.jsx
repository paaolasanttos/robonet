import { useEffect, useState } from "react";
import { aulasApi } from "../servicos/api";

function valueOrDash(value) {
  return value || "—";
}

export default function AulaViewModal({ aula, onClose }) {
  const [usuarios, setUsuarios] = useState([]);
  const [usuariosLoading, setUsuariosLoading] = useState(true);
  const [usuariosError, setUsuariosError] = useState("");

  useEffect(() => {
    let active = true;
    setUsuarios([]);
    setUsuariosLoading(true);
    setUsuariosError("");

    aulasApi.listarUsuarios(aula.id)
      .then((data) => {
        if (active) setUsuarios(Array.isArray(data) ? data : []);
      })
      .catch((error) => {
        if (active) setUsuariosError(error.message || "Não foi possível carregar os usuários associados.");
      })
      .finally(() => {
        if (active) setUsuariosLoading(false);
      });

    return () => {
      active = false;
    };
  }, [aula.id]);

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <div className="modal" role="dialog" aria-modal="true" aria-labelledby="view-title">
        <div className="modal-header">
          <div>
            <span className="eyebrow">Detalhes</span>
            <h2 id="view-title">{valueOrDash(aula.titulo)}</h2>
          </div>
          <button className="icon-button" onClick={onClose} aria-label="Fechar">×</button>
        </div>

        <div className="details">
          <div className="detail-item">
            <span>Tema</span>
            <strong>{valueOrDash(aula.tema)}</strong>
          </div>
          <div className="detail-item">
            <span>Título</span>
            <strong>{valueOrDash(aula.titulo)}</strong>
          </div>
          <div className="detail-item detail-description">
            <span>Descrição</span>
            <p>{valueOrDash(aula.descricao)}</p>
          </div>
          <div className="detail-item detail-description">
            <span>Usuários associados</span>
            {usuariosLoading ? (
              <p>Carregando usuários...</p>
            ) : usuariosError ? (
              <p role="alert">{usuariosError}</p>
            ) : usuarios.length === 0 ? (
              <p>Nenhum usuário associado a esta aula.</p>
            ) : (
              <ul className="associated-users">
                {usuarios.map((usuario) => (
                  <li key={usuario.id}>
                    <strong>{valueOrDash(usuario.nome)}</strong>
                    <p>{valueOrDash(usuario.email)} · {usuario.concluida ? "Concluída" : "Pendente"}</p>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        <div className="modal-footer">
          <button className="button button-primary" onClick={onClose}>Fechar</button>
        </div>
      </div>
    </div>
  );
}