import { Navigate, Route, Routes } from "react-router-dom";
import AulasPagina from "./pagina/aulas";
import UsuariosPagina from "./pagina/usuarios";
import LoginPagina from "./pagina/login";
import AcessoNegadoPagina from "./pagina/acessoNegado";
import RotaProtegida from "./componentes/RotaProtegida";
import RecuperarSenhaPagina from "./pagina/recuperarSenha";
import PoliticasPagina from "./pagina/politicas";
import TermoPagina from "./pagina/termo";
import AuditoriaPagina from "./pagina/auditoria";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPagina />} />
      <Route path="/recuperar-senha" element={<RecuperarSenhaPagina />} />
      <Route path="/politicas" element={<PoliticasPagina />} />
      <Route path="/acesso-negado" element={<AcessoNegadoPagina />} />
      <Route path="/termo" element={<TermoPagina />} />
      <Route element={<RotaProtegida />}>
        <Route path="/aulas" element={<AulasPagina />} />
      </Route>
      <Route element={<RotaProtegida roles={["admin"]} />}>
        <Route path="/usuarios" element={<UsuariosPagina />} />
        <Route path="/auditoria" element={<AuditoriaPagina />} />
      </Route>
      <Route path="*" element={<Navigate to="/aulas" replace />} />
    </Routes>
  );
}