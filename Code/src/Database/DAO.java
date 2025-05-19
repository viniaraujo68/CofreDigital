package Database;

import model.Usuario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DAO {

    private static DAO instance = null;

    public DAO(){

    }

    public static DAO getInstance(){
        if(instance == null){
            instance = new DAO();
        }
        return instance;
    }

    public int getUsersCount() throws Exception {
        String query = "SELECT COUNT(*) FROM Usuarios;";
        try (PreparedStatement preparedStatement = Database.getInstance().connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return 0;
            }
        }
    }

    public int getUserIdByEmail(String email) throws Exception {
        String query = "SELECT UID FROM Usuarios WHERE login = ?";
        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("UID");
            } else {
                throw new Exception("Usuário não encontrado.");
            }
        }
    }

    public Usuario getUserByEmail(String email) throws Exception {
        String query = "SELECT * FROM Usuarios WHERE login = ?";
        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setUid(rs.getInt("UID"));
                    u.setNome(rs.getString("nome"));
                    u.setLogin(rs.getString("login"));
                    u.setGrupoId(rs.getInt("grupo_id"));
                    u.setSenhaHash(rs.getString("senha_hash"));
                    u.setTotpSecretoCriptografado(rs.getBytes("totp_secreto_criptografado"));
                    return u;
                } else {
                    throw new Exception("Usuário não encontrado.");
                }
            }
        }
    }


//    public ResultSet getUserByEmail(String email) throws Exception {
//        String query = "SELECT UID FROM Usuarios WHERE login = ?";
//        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
//            stmt.setString(1, email);
//            System.out.println("Stmt: " + stmt);
//            ResultSet rs = stmt.executeQuery();
//            System.out.println(rs);
//            if (rs.next()) {
//                    return rs;
//                } else {
//                    System.out.println("Usuário não encontrado.");
//                throw new Exception("Usuário não encontrado.");
//            }
//        }
//    }

    public int incrementaAcesso(model.Usuario usuario) throws Exception {
        String query = "UPDATE Usuarios SET num_acessos = num_acessos + 1 WHERE UID = ?";
        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
            stmt.setInt(1, usuario.getUid());
            return stmt.executeUpdate();
        }
    }

    public byte[] getChavePrivadaAdmin() throws Exception {
        String sql = """
        SELECT c.chave_privada_criptografada
        FROM Chaveiro c
        JOIN Usuarios u ON c.UID = u.UID
        WHERE u.grupo_id = 1
        LIMIT 1
    """;

        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBytes("chave_privada_criptografada");
            } else {
                throw new Exception("Chave privada do administrador não encontrada.");
            }
        }
    }

    public byte[] getCertificadoAdmin() throws Exception {
        String sql = """
        SELECT c.certificado_digital
        FROM Chaveiro c
        JOIN Usuarios u ON c.UID = u.UID
        WHERE u.grupo_id = 1
        LIMIT 1
    """;

        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBytes("certificado_digital");
            } else {
                throw new Exception("Certificado do administrador não encontrado.");
            }
        }
    }



    public int incrementaQtdTentativas(model.Usuario usuario, String coluna) throws Exception {
        String query = "UPDATE Usuarios SET " + coluna + " = " + coluna + " + 1 WHERE uid = ?";

        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
            stmt.setInt(1, usuario.getUid());
            return stmt.executeUpdate(); // Retorna número de linhas afetadas
        }
    }

    public int getQtdTentativas(model.Usuario usuario, String coluna) throws Exception {
        String query = "SELECT " + coluna + " FROM Usuarios WHERE uid = ?";

        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
            stmt.setInt(1, usuario.getUid());
            return stmt.executeUpdate(); // Retorna número de linhas afetadas
        }
    }

    public int atualizarUltimoBloqueio(model.Usuario usuario) throws Exception {
        String query = "UPDATE Usuarios SET last_time_blocked = CURRENT_TIMESTAMP WHERE uid = ?";

        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
            stmt.setInt(1, usuario.getUid());
            return stmt.executeUpdate(); // Retorna número de linhas afetadas
        }
    }

    public java.sql.Timestamp consultarUltimoBloqueio(model.Usuario usuario) throws Exception {
        String query = "SELECT last_time_blocked FROM Usuarios WHERE uid = ?";

        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
            stmt.setInt(1, usuario.getUid());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("last_time_blocked");
                } else {
                    throw new Exception("Usuário não encontrado com UID: " + usuario.getUid());
                }
            }
        }
    }


    public int getNumeroAcessos(model.Usuario usuario) throws Exception {
        String query = "SELECT num_acessos FROM Usuarios WHERE UID = ?";
        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
                    stmt.setInt(1, usuario.getUid());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("num_acessos");
            } else {
                throw new Exception("Usuário não encontrado.");
            }
        }
    }

    public List<String[]> listarArquivosSecretos(model.Usuario usuario) throws Exception {
        String query = "SELECT nome_arquivo FROM Arquivos WHERE UID = ?";
        List<String[]> arquivos = new ArrayList<>();

        // Simulação de dados - substitua por query real quando tiver a tabela
        arquivos.add(new String[]{"segredo1.txt", usuario.getLogin(), usuario.getGrupo()});
        arquivos.add(new String[]{"relatorio-final.pdf", usuario.getLogin(), usuario.getGrupo()});
        arquivos.add(new String[]{"dados-encriptados.zip", usuario.getLogin(), usuario.getGrupo()});

        return arquivos;
//        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
//            stmt.setInt(1, usuario.getUid());
//            ResultSet rs = stmt.executeQuery();
//            if (rs.next()) {
//                return new String[]{rs.getString("nome_arquivo")};
//            } else {
//                throw new Exception("Usuário não encontrado.");
//            }
//        }
    }

    public int getNumeroConsultas(model.Usuario usuario) throws Exception {
        String query = "SELECT num_consultas FROM Usuarios WHERE UID = ?";
        return 1;
//        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
//            stmt.setInt(1, usuario.getUid());
//            ResultSet rs = stmt.executeQuery();
//            if (rs.next()) {
//                return rs.getInt("num_consultas");
//            } else {
//                throw new Exception("Usuário não encontrado.");
//            }
//        }
    }

    public boolean inserirUsuario(model.Usuario usuario) throws Exception {
        String query = """
        INSERT INTO Usuarios (nome, login, grupo_id, senha_hash, totp_secreto_criptografado)
        VALUES (?, ?, ?, ?, ?);
    """;

        try (PreparedStatement preparedStatement = Database.getInstance().connection.prepareStatement(query)) {
            preparedStatement.setString(1, usuario.getNome());
            preparedStatement.setString(2, usuario.getLogin());
            preparedStatement.setInt(3, usuario.getGrupoId());
            preparedStatement.setString(4, usuario.getSenhaHash());
            preparedStatement.setBytes(5, usuario.getTotpSecretoCriptografado());

            int linhasAfetadas = preparedStatement.executeUpdate();
            return linhasAfetadas > 0;
        }
    }

    public int getEmailCount(String email) throws Exception {
        String query = "SELECT COUNT(*) AS total FROM Usuarios WHERE login = ?";
        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            } else {
                return 0;
            }
        }
    }


    public void insertChaveiro(int uid, byte[] certificado, byte[] chavePrivada) throws Exception {
        String query = "INSERT INTO Chaveiro (uid, certificado_digital, chave_privada_criptografada) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
            stmt.setInt(1, uid);
            stmt.setBytes(2, certificado);
            stmt.setBytes(3, chavePrivada);
            stmt.executeUpdate();
        }
    }


}
