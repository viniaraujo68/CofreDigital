package Database;

import model.Usuario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.nio.file.Files;
import java.nio.file.Paths;

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


    public void insertChaveiro(int uid, String certificado, String chavePrivada) throws Exception {
        String query = "INSERT INTO Chaveiro (uid, certificado_digital, chave_privada_criptografada) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = Database.getInstance().connection.prepareStatement(query)) {
            stmt.setInt(1, uid);
            stmt.setString(2, certificado);
            stmt.setString(3, chavePrivada);
            stmt.executeUpdate();
        }
    }


}
