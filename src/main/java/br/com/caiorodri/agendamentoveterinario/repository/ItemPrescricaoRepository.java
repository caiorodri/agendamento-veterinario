package br.com.caiorodri.agendamentoveterinario.repository;

import br.com.caiorodri.agendamentoveterinario.model.ItemPrescricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemPrescricaoRepository extends JpaRepository<ItemPrescricao, Long> {

    List<ItemPrescricao> findByResultadoConsultaId(Long idResultadoConsulta);

}