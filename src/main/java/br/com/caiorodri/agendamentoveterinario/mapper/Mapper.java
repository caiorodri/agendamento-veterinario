package br.com.caiorodri.agendamentoveterinario.mapper;

import java.util.List;

import br.com.caiorodri.agendamentoveterinario.dto.*;
import br.com.caiorodri.agendamentoveterinario.model.Agendamento;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoStatus;
import br.com.caiorodri.agendamentoveterinario.model.AgendamentoTipo;
import br.com.caiorodri.agendamentoveterinario.model.Animal;
import br.com.caiorodri.agendamentoveterinario.model.Endereco;
import br.com.caiorodri.agendamentoveterinario.model.Especie;
import br.com.caiorodri.agendamentoveterinario.model.Perfil;
import br.com.caiorodri.agendamentoveterinario.model.Raca;
import br.com.caiorodri.agendamentoveterinario.model.Sexo;
import br.com.caiorodri.agendamentoveterinario.model.Status;
import br.com.caiorodri.agendamentoveterinario.model.Usuario;

@org.mapstruct.Mapper(componentModel = "spring")
public interface Mapper {

    UsuarioSimplesDTO usuarioToUsuarioSimplesDto(Usuario usuario);
    AnimalSimplesDTO animalToAnimalSimplesDto(Animal animal);

    UsuarioDTO usuarioToDto(Usuario usuario);
    Usuario dtoToUsuario(UsuarioDTO usuarioDTO);

    List<UsuarioDTO> usuarioListToDtoList(List<Usuario> usuarios);
    List<Usuario> dtoListToUsuarioList(List<UsuarioDTO> usuariosDTO);
    
    AnimalDTO animalToDto(Animal animal);
    Animal dtoToAnimal(AnimalDTO animalDTO);

    List<AnimalDTO> animalListToDtoList(List<Animal> animais);
    List<Animal> dtoListToAnimalList(List<AnimalDTO> animaisDTO);

    AgendamentoDTO agendamentoToDto(Agendamento agendamento);
    Agendamento dtoToAgendamento(AgendamentoDTO agendamentoDTO);

    List<AgendamentoDTO> agendamentoListToDtoList(List<Agendamento> agendamentos);
    List<Agendamento> dtoListToAgendamentoList(List<AgendamentoDTO> agendamentosDTO);

    AgendamentoStatusDTO agendamentoStatusToDto(AgendamentoStatus model);
    AgendamentoStatus dtoToAgendamentoStatus(AgendamentoStatusDTO dto);

    List<AgendamentoStatusDTO> agendamentoStatusListToDtoList(List<AgendamentoStatus> models);
    List<AgendamentoStatus> dtoListToAgendamentoStatusList(List<AgendamentoStatusDTO> dtos);

    AgendamentoTipoDTO agendamentoTipoToDto(AgendamentoTipo model);
    AgendamentoTipo dtoToAgendamentoTipo(AgendamentoTipoDTO dto);

    List<AgendamentoTipoDTO> agendamentoTipoListToDtoList(List<AgendamentoTipo> models);
    List<AgendamentoTipo> dtoListToAgendamentoTipoList(List<AgendamentoTipoDTO> dtos);

    EnderecoDTO enderecoToDto(Endereco model);
    Endereco dtoToEndereco(EnderecoDTO dto);

    List<EnderecoDTO> enderecoListToDtoList(List<Endereco> models);
    List<Endereco> dtoListToEnderecoList(List<EnderecoDTO> dtos);

    EspecieDTO especieToDto(Especie model);
    Especie dtoToEspecie(EspecieDTO dto);

    List<EspecieDTO> especieListToDtoList(List<Especie> models);
    List<Especie> dtoListToEspecieList(List<EspecieDTO> dtos);

    PerfilDTO perfilToDto(Perfil model);
    Perfil dtoToPerfil(PerfilDTO dto);

    List<PerfilDTO> perfilListToDtoList(List<Perfil> models);
    List<Perfil> dtoListToPerfilList(List<PerfilDTO> dtos);

    RacaDTO racaToDto(Raca model);
    Raca dtoToRaca(RacaDTO dto);

    List<RacaDTO> racaListToDtoList(List<Raca> models);
    List<Raca> dtoListToRacaList(List<RacaDTO> dtos);

    SexoDTO sexoToDto(Sexo model);
    Sexo dtoToSexo(SexoDTO dto);

    List<SexoDTO> sexoListToDtoList(List<Sexo> models);
    List<Sexo> dtoListToSexoList(List<SexoDTO> dtos);

    StatusDTO statusToDto(Status model);
    Status dtoToStatus(StatusDTO dto);

    List<StatusDTO> statusListToDtoList(List<Status> models);
    List<Status> dtoListToStatusList(List<StatusDTO> dtos);

}
