import connection from '../db'

const getAdminByEmail = async (email: string) => {
  const [[result]]: any = await connection.execute(
    'SELECT id_admin, nome, email, senha FROM Administrador WHERE email = ?', [email]
  );
  return result;
};

const getUserAdmByEmail = async (email: string) => {
    const [[result]]: any = await connection.execute(
        'SELECT id_user_admin, nome, email, senha, id_user, id_empresa FROM User_Admin WHERE email = ?',
        [email]
    );
    return result;
};

export default {
    getAdminByEmail,
    getUserAdmByEmail
};