package dao.interface;

import java.util.List;
import dto.Carteira;

public interface ICarteiraDAO {
    void Salvar(Carteira carteira);
    void Alterar(Carteira carteira);
    void Excluir(int carteiraId);

    Carteira BuscarPorId(int carteiraId);
    List<Carteira> Listar();
    List<Carteira> ListarPorUsuario(int usuarioId);
}