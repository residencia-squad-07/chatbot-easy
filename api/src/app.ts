import express from "express";
import messageRoutes from "./routes/messageRoutes";
import empresaRoutes from "./routes/empresaRoutes";
import adminRoutes from "./routes/adminRoutes";
import solicitacoesRoutes from "./routes/solicitacoesRoutes";
import configErpRoutes from "./routes/configErpRoutes";
import userRoutes from "./routes/userRoutes";
import omieRoutes from "./routes/omieRoutes";
import agendRoutes from "./routes/agendRoutes";

const app = express();

app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));

app.use("/wpp", messageRoutes);
app.use("/empresa", empresaRoutes);
app.use("/admin", adminRoutes);
app.use("/solicitacoes", solicitacoesRoutes);
app.use("/config-erp", configErpRoutes);
app.use("/users", userRoutes);
app.use("/omie", omieRoutes);
app.use("/agend", agendRoutes);

export default app;
