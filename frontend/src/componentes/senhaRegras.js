export const requisitosSenha = [
  { chave: "tamanho", texto: "Pelo menos 8 caracteres", atendido: (senha) => senha.length >= 8 },
  { chave: "maiuscula", texto: "Uma letra maiúscula", atendido: (senha) => /\p{Lu}/u.test(senha) },
  { chave: "minuscula", texto: "Uma letra minúscula", atendido: (senha) => /\p{Ll}/u.test(senha) },
  { chave: "numero", texto: "Um número", atendido: (senha) => /\d/.test(senha) },
  { chave: "especial", texto: "Um caractere especial", atendido: (senha) => /[^\p{L}\p{N}\s]/u.test(senha) },
];

export const MENSAGEM_SENHA = "A senha ainda não atende a todos os requisitos.";

export function senhaValida(senha) {
  return requisitosSenha.every((requisito) => requisito.atendido(senha));
}
