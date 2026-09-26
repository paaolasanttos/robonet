const API_URL = (import.meta.env.VITE_API_URL ?? "http://localhost:4000").replace(/\/$/, "");

async function parseResponse(response) {
  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    return response.json();
  }

  const text = await response.text();
  return text ? { message: text } : null;
}

function getMsgErro(status, data) {
  if (!status) {
    return "Não foi possível conectar ao servidor. Verifique se o backend está disponível.";
  }

  if (status === 400) return data?.message || "Os dados enviados são inválidos.";
  if (status === 401) return data?.message || "Sua sessão expirou. Entre novamente.";
  if (status === 403) return data?.message || "Você não tem permissão para esta operação.";
  if (status === 404) return data?.message || "O recurso não foi encontrado.";
  if (status === 503) return data?.message || "O serviço de e-mail está indisponível.";
  if (status >= 500) return "O servidor encontrou um problema. Tente novamente mais tarde.";
  return data?.message || "Não foi possível concluir a operação.";
}

async function request(path, options = {}) {
  try {
    const session = JSON.parse(localStorage.getItem("robonet.session") || "null");
    const response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(session?.token ? { Authorization: `Bearer ${session.token}` } : {}),
        ...(options.headers || {}),
      },
    });

    const data = await parseResponse(response);

    if (!response.ok) {
      const error = new Error(getMsgErro(response.status, data));
      error.status = response.status;
      throw error;
    }

    return data;
  } catch (error) {
    if (error instanceof TypeError) {
      throw new Error(getMsgErro());
    }
    throw error;
  }
}

export const aulasApi = {
  listar: () => request("/aulas"),
  listarTemas: () => request("/aulas/temas"),
  buscarPorId: (id) => request(`/aulas/${id}`),
  criar: (aula) =>
    request("/aulas", {
      method: "POST",
      body: JSON.stringify(aula),
    }),
  atualizar: (id, aula) =>
    request(`/aulas/${id}`, {
      method: "PUT",
      body: JSON.stringify(aula),
    }),
  excluir: (id) =>
    request(`/aulas/${id}`, {
      method: "DELETE",
    }),
};

export const usuariosApi = {
  listar: () => request("/api/usuarios"),
  criar: (usuario) => request("/api/usuarios", {
    method: "POST",
    body: JSON.stringify(usuario),
  }),
  atualizar: (id, usuario) => request(`/api/usuarios/${id}`, {
    method: "PUT",
    body: JSON.stringify(usuario),
  }),
  atualizarPerfil: (id, perfil) => request(`/api/usuarios/${id}/perfil`, {
    method: "PUT",
    body: JSON.stringify({ perfil }),
  }),
  atualizarStatus: (id, ativo) => request(`/api/usuarios/${id}/ativo`, {
    method: "PATCH",
    body: JSON.stringify({ ativo }),
  }),
  excluir: (id) => request(`/api/usuarios/${id}`, { method: "DELETE" }),
};

export const authApi = {
  solicitarCodigo: (credenciais) => request("/auth/request-code", {
    method: "POST",
    body: JSON.stringify(credenciais),
  }),
  verificarCodigo: (dados) => request("/auth/verify-code", {
    method: "POST",
    body: JSON.stringify(dados),
  }),
  solicitarCodigoSenha: (dados) => request("/auth/password-code", {
    method: "POST",
    body: JSON.stringify(dados),
  }),
  redefinirSenha: (dados) => request("/auth/reset-password", {
    method: "POST",
    body: JSON.stringify(dados),
  }),
};