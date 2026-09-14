
const URL_API = 'http://localhost:8080/api'

export async function listarUsuarios() {
    const res = await fetch(URL_API + '/usuarios')
    if (!res.ok) throw new Error('Erro ao buscar usuários')
    return res.json()
}

export async function criarUsuario(dados: any) {
    const res = await fetch(URL_API + '/usuarios', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dados),
    })
    if (!res.ok) {
        const erro = await res.json().catch(() => ({}))
        throw new Error(erro.message || 'Erro ao criar usuário')
    }
    return res.json()
}

export async function mudarPerfil(id: number, perfil: string) {
    const res = await fetch(URL_API + '/usuarios/' + id + '/perfil', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ perfil: perfil }),
    })
    if (!res.ok) throw new Error('Erro ao mudar perfil')
    return res.json()
}

export async function mudarStatus(id: number, ativo: boolean) {
    const res = await fetch(URL_API + '/usuarios/' + id + '/ativo', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ativo: ativo }),
    })
    if (!res.ok) throw new Error('Erro ao mudar status')
    return res.json()
}

export async function excluirUsuario(id: number) {
    const res = await fetch(URL_API + '/usuarios/' + id, { method: 'DELETE' })
    if (!res.ok) throw new Error('Erro ao excluir')
}
