package dao.interface;

import java.util.List;
import dto.Movimentacao;

public interface IMovimentacaoDAO {
    void Salvar(Movimentacao movimentacao);

    Movimentacao BuscarPorId(int movimentacaoId);
    List<Movimentacao> Listar();
    List<Movimentacao> ListarPorUsuario(int usuarioId);
}