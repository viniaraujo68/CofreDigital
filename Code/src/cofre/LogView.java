// Francisco Lou Gardenberg - 2211275
// Vinicius Barros Pessoa de Araujo - 2210392

package cofre;

import Database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class LogView {

    public static HashMap<Integer, String> mensagens = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Connection conn = Database.getInstance().connection;

        // Carrega todas as mensagens com seus respectivos códigos
        String queryMensagens = "SELECT MID, texto FROM Mensagens;";
        try (PreparedStatement stmt = conn.prepareStatement(queryMensagens);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                mensagens.put(rs.getInt("MID"), rs.getString("texto"));
            }
        }

        // Consulta os registros em ordem cronológica
        String queryRegistros = """
                SELECT MID, data_hora, usuario, arquivo 
                FROM Registros 
                ORDER BY datetime(data_hora) ASC;
                """;

        try (PreparedStatement stmt = conn.prepareStatement(queryRegistros);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int mid = rs.getInt("MID");
                String dataHora = rs.getString("data_hora");
                String usuario = rs.getString("usuario");
                String arquivo = rs.getString("arquivo");

                // Busca a mensagem pelo MID
                String mensagem = mensagens.get(mid);
                if (mensagem == null) {
                    mensagem = "[Mensagem desconhecida com MID=" + mid + "]";
                }

                // Substitui placeholders por valores reais, se existirem
                if (usuario != null) {
                    mensagem = mensagem.replace("<login_name>", usuario);
                }
                if (arquivo != null) {
                    mensagem = mensagem.replace("<arq_name>", arquivo);
                }

                // Exibe o registro
                System.out.println(dataHora + " | " + mensagem);
            }
        }
    }
}
