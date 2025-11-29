import mysql from "mysql2/promise";
const pool = mysql.createPool({
    host: "0.tcp.sa.ngrok.io",
    user: "residencia",
    password: "senha@123",
    database: "easy",
    port: Number(11080),
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
})
export default pool;

/*
const pool = mysql.createPool({
    host: process.env.MYSQLHOST || "database",
    user: process.env.MYSQLUSER || "root",
    password: process.env.MYSQLPASSWORD || "root",
    database: process.env.MYSQLDATABASE || "auth_chatbot",
    port: Number(process.env.MYSQLPORT) || 3306,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
})
*/