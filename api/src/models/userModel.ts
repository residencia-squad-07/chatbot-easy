import connection from '../db'


export interface Usuario {
    nome: string
    telefone?: string | null
    papel: number
    atividade: number
    id_empresa?: number | null
    primeiro_contato?: number
}


const createUsuario = async ({ nome, telefone, papel, atividade, id_empresa, primeiro_contato = 2 }: Usuario) => {
    const [{ insertId }]: any = await connection.execute(
        `INSERT INTO Usuario (nome, telefone, papel, id_empresa, primeiro_contato) VALUES (?, ?, ?, ?, ?)`,
        [
            nome,
            telefone ?? null,
            papel,
            id_empresa ?? null,
            primeiro_contato
        ]
    )
    return insertId
}

const getAllUsuarios = async () => {
    const [rows]: any = await connection.execute('SELECT * FROM Usuario')
    return rows
}
const getUsuarioByNumber = async (telefone: String) => {
    const [[usuario]]: any = await connection.execute(
        'SELECT * FROM Usuario WHERE telefone = ?',
        [telefone]
    )
    return usuario || null
}
const getRoleByNumber = async (telefone: String): Promise<number | null> => {
    const [[papel]]: any = await connection.execute(
        'SELECT papel FROM Usuario WHERE telefone = ?',
        [telefone]
    )
    return papel || null
}

const getUsuarioById = async (id: number) => {
    const [[usuario]]: any = await connection.execute(
        'SELECT * FROM Usuario WHERE id_user = ?',
        [id]
    )
    return usuario || null
}

const getUsuariosByEmp = async (id_empresa: number) => {
    const [usuarios]: any = await connection.execute(
        'SELECT * FROM Usuario WHERE id_empresa = ?',
        [id_empresa]
    );
    return usuarios;
};

const updateUsuario = async (
    id: number,
    usuario: Partial<Omit<Usuario, 'papel'>> // evita alterar papel por padrão
) => {
    const fields = ['nome', 'telefone','atividade',  'id_empresa']

    const { setClauses, values } = Object.entries(usuario).reduce(
        (acc, [key, value]) => {
            if (fields.includes(key)) {
                acc.setClauses.push(`${key} = ?`)
                acc.values.push(value)
            }
            return acc
        },
        { setClauses: [] as string[], values: [] as any[] }
    )

    if (setClauses.length === 0) {
        throw new Error('Nenhum campo válido para atualização.')
    }

    const query = `UPDATE Usuario SET ${setClauses.join(', ')} WHERE id_user = ?`
    values.push(id)

    const [{ affectedRows }]: any = await connection.execute(query, values)
    return affectedRows
}

const deleteUsuario = async (id: number) => {
    const [{ affectedRows }]: any = await connection.execute(
        'DELETE FROM Usuario WHERE id_user = ?',
        [id]
    )
    return affectedRows
}

const marcarPrimeiroContato = async (telefone: string) => {
    const [{ affectedRows }]: any = await connection.execute(
        `UPDATE Usuario SET primeiro_contato = 'sim' WHERE telefone = ? AND primeiro_contato = 'nao'`,
        [telefone]
    )

    return affectedRows > 0
}

const marcarUsuarioInativo = async (id: number) => {
    const [{ affectedRows }]: any = await connection.execute(
        "UPDATE Usuario SET atividade = 'inativo' WHERE id_user = ?",
        [id]
    )
    return affectedRows
}

export default {
    createUsuario,
    getAllUsuarios,
    getUsuarioById,
    updateUsuario,
    deleteUsuario,
    getUsuarioByNumber,
    getRoleByNumber,
    getUsuariosByEmp,
    marcarPrimeiroContato,
    marcarUsuarioInativo
}