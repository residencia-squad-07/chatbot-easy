import joi from "joi";

const adminSchema = joi.object({
  nome: joi.string().optional(),
  email: joi.string().email().optional(),
  senha: joi.string().optional()
}).min(1);

const configErpSchema = joi.object({
  url_api: joi.string().optional(),
  token_api: joi.string().optional(),
  status: joi.string().valid('ativo','inativo'),
  id_empresa: joi.number().optional()
}).min(1);

const empresaSchema = joi.object({
  nome_empresa: joi.string().optional(),
  cnpj: joi.string().optional(),
  token_api: joi.string().optional()
}).min(1);

const solicitacaoSchema = joi.object({
  tipo_solicitacao: joi.string().valid('recibo', 'despesa', 'relatorio'),
  data_solicitacao: joi.date().optional(),
  status: joi.string().valid('sucesso', 'erro', 'pendente'),
  id_usuario: joi.number().optional()
}).min(1);

const usuarioSchema = joi.object({
  nome: joi.string().optional(),
  telefone: joi.string().optional(),
  papel: joi.string().valid('administrador', 'funcionario'),
  id_empresa: joi.number().optional()
}).min(1)

const agendSchema = joi.object({
  data_solicitacao: joi.date().optional(),
  proxima_execucao: joi.date().optional(),
  status: joi.string().valid('ativo',`inativo`),
  id_usuario: joi.number().optional()
})

export default {
  adminSchema,
  configErpSchema,
  empresaSchema,
  solicitacaoSchema,
  usuarioSchema,
  agendSchema
}