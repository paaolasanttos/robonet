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
  if (status === 404) return data?.message || "A aula não foi encontrada.";
  if (status >= 500) return "O servidor encontrou um problema. Tente novamente mais tarde.";
  return data?.message || "Não foi possível concluir a operação.";
}

async function request(path, options = {}) {
  try {
    const response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
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