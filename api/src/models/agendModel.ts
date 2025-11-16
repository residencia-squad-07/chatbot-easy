import connection from '../db'

export interface Agendamentos {
    data_solicitacao: string
    proxima_execucao?: string | null
    status: number
    id_user: number
}

const getAllAgend = async () => {
  const [rows]: any = await connection.execute('SELECT * FROM Agendamento')
  return rows
}

const createAgend = async ({ data_solicitacao, proxima_execucao, status, id_user }: Agendamentos) => {
    const [{ insertId }]: any = await connection.execute(
    `INSERT INTO Agendamento (data_solicitacao, proxima_execucao, status, id_user) VALUES (?, ?, ?, ?)`,
    [ data_solicitacao, proxima_execucao ?? null, status, id_user ]
  );
  return insertId;
}

const getAgendById = async (id: number) => {
  const [[result]]: any = await connection.execute(
    'SELECT * FROM Agendamento WHERE id = ?',
    [id]
  )
  return result
}

const updateAgendById = async (
  id: number,
  config: Partial<Agendamentos>
) => {
  const fields = ['data_solicitacao', 'proxima_execucao', 'status', 'id_user']

  const { setClauses, values } = Object.entries(config).reduce(
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

  const query = `UPDATE Agendamentos SET ${setClauses.join(', ')} WHERE id = ?`
  values.push(id)

  const [{ affectedRows }]: any = await connection.execute(query, values)
  return affectedRows
}

const deleteAgend = async (id: number) => {
  const [{ affectedRows }]: any = await connection.execute(
    'DELETE FROM Agendamentos WHERE id = ?',
    [id]
  )
  return affectedRows
}

export default {
  getAllAgend,
  createAgend,
  getAgendById,
  updateAgendById,
  deleteAgend
}