// Francisco Lou Gardenberg - 2211275
// Vinicius Barros Pessoa de Araujo - 2210392

package model;

import javax.crypto.SecretKey;

public class Usuario {

    private int uid;
    private String nome;
    private String login; // email
    private int grupoId;
    private String senhaHash;
    private byte[] totpSecretoCriptografado;
    private int tentativasSenha;
    private int tentativasTotp;

    // Getters e Setters

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setGrupo(String grupo) {
        switch (grupo) {
            case "Administrador":
                this.grupoId = 1;
                break;
            case "Gerente":
                this.grupoId = 2;
                break;
            case "Funcionário":
                this.grupoId = 3;
                break;
            default:
                throw new IllegalArgumentException("Grupo inválido: " + grupo);
        }
    }

    public String getGrupo() {
        switch (grupoId) {
            case 1:
                return "Administrador";
            case 2:
                return "Usuario";
            default:
                throw new IllegalArgumentException("Grupo inválido: " + grupoId);
        }
    }

    public int getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(int grupoId) {
        this.grupoId = grupoId;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public byte[] getTotpSecretoCriptografado() {
        return totpSecretoCriptografado;
    }

    public void setTotpSecretoCriptografado(byte[] totpSecretoCriptografado) { this.totpSecretoCriptografado = totpSecretoCriptografado; }

    public int getTentativasSenha() { return this.tentativasSenha; }
    public int getTentativasTotp() { return this.tentativasTotp; }
    public void setTentativasSenha(int qtd) { this.tentativasSenha = qtd; }
    public void setTentativasTotp(int qtd) { this.tentativasTotp = qtd; }
}
