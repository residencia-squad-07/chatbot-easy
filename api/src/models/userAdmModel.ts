import connection from '../db'
import bcrypt from 'bcrypt'

export interface UserAdm {
    nome: string
    email: string
    senha: string
    id_usuario: number
}

const getAllUserAdm = async () => {
    const [rows]: any = await connection.execute(
        'SELECT id, nome, email, id_usuario FROM User_Adm'
    )
    return rows
}

const getUserAdmById = async (id: number) => {
    const [[result]]: any = await connection.execute(
        'SELECT id, nome, email, id_usuario FROM User_Adm WHERE id = ?',
        [id]
    )
    return result
}

const getUserAdmByEmail = async (email: string) => {
    const [[result]]: any = await connection.execute(
        'SELECT id, nome, email, id_usuario FROM User_Adm WHERE email = ?',
        [email]
    )
    return result
}

const createUserAdm = async ({ nome, email, senha, id_usuario }: UserAdm) => {
    const salt = await bcrypt.genSalt(12)
    const senhaHash = await bcrypt.hash(senha, salt)

    const [{ insertId }]: any = await connection.execute(
        'INSERT INTO User_Adm (nome, email, senha, id_usuario) VALUES (?, ?, ?, ?)',
        [nome, email, senhaHash, id_usuario]
    )

    return insertId
}

const updateUserAdm = async (id: number, userAdm: Partial<UserAdm>) => {
    const allowedFields = ['nome', 'email', 'senha', 'id_usuario']

    const { setClauses, values } = await Object.entries(userAdm).reduce(
        (acc, [key, value]) => {
            if (allowedFields.includes(key)) {
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

    if (userAdm.senha) {
        const salt = await bcrypt.genSalt(12)
        const senhaHash = await bcrypt.hash(userAdm.senha, salt)
        const senhaIndex = values.indexOf(userAdm.senha)
        if (senhaIndex !== -1) values[senhaIndex] = senhaHash
    }

    const query = `UPDATE User_Adm SET ${setClauses.join(', ')} WHERE id = ?`
    values.push(id)

    const [{ affectedRows }]: any = await connection.execute(query, values)
    return affectedRows
}

const deleteUserAdm = async (id: number) => {
    const [{ affectedRows }]: any = await connection.execute(
        'DELETE FROM User_Adm WHERE id = ?',
        [id]
    )
    return affectedRows
}

export default {
    getAllUserAdm,
    getUserAdmById,
    getUserAdmByEmail,
    createUserAdm,
    updateUserAdm,
    deleteUserAdm,
}
