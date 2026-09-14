import { Navigate, Route, Routes } from "react-router-dom";
import AulasPagina from "./pagina/aulas";

export default function App() {
  return (
    <Routes>
      <Route path="/aulas" element={<AulasPagina />} />
      <Route path="*" element={<Navigate to="/aulas" replace />} />
    </Routes>
  );
}