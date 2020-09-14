package com.lfdeus.softplan.controller;

import com.lfdeus.softplan.dto.ProcessoDTO;
import com.lfdeus.softplan.dto.UsuarioDTO;
import com.lfdeus.softplan.model.Processo;
import com.lfdeus.softplan.model.Usuario;
import com.lfdeus.softplan.repository.ProcessoRepository;
import com.lfdeus.softplan.repository.UsuarioRepository;
import com.lfdeus.softplan.uteis.Uteis;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("processo")
public class ProcessoController {

    private final ProcessoRepository repo;
    private final UsuarioRepository usuarioRepo;

    public ProcessoController(ProcessoRepository processoRepository,
                              UsuarioRepository usuarioRepo) {
        this.repo = processoRepository;
        this.usuarioRepo = usuarioRepo;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity todos(@RequestParam(required = false, name = "id") Long usuario,
                                @RequestParam(required = false, name = "p") String pendente) {
        try {
            List<Processo> lista;
            boolean pendenteBoolean = (pendente != null && pendente.equalsIgnoreCase("t"));
            if (!Uteis.emptyNumber(usuario) && usuario > 0) {
                Optional<Usuario> usuarioData = usuarioRepo.findById(usuario);
                if (usuarioData.isPresent()) {
                    if (pendenteBoolean) {
                        lista = repo.findByUsuariosParecerContainsAndDataParecer(usuarioData.get(), null);
                    } else {
                        lista = repo.findByUsuariosParecerContains(usuarioData.get());
                    }
                } else {
                    return new ResponseEntity<>("Usuário não encontrado", HttpStatus.NOT_FOUND);
                }
            } else {
                lista = repo.findAll();
            }

            List<ProcessoDTO> listaDTO = new ArrayList<>();
            for (Processo processo : lista) {
                listaDTO.add(new ProcessoDTO(processo));
            }
            return new ResponseEntity<>(listaDTO, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseBody
    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity obter(@PathVariable Long id) {
        try {
            Optional<Processo> processoData = repo.findById(id);
            if (processoData.isPresent()) {
                return new ResponseEntity<>(new ProcessoDTO(processoData.get()), HttpStatus.OK);
            }
            return new ResponseEntity<>("Processo não encontrado", HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity save(@RequestBody ProcessoDTO dto) {
        try {
            dto.validarDados(false);
            Processo processo = new Processo();
            processo.setId(null);
            processo.setDataProcesso(new Date());
            processo.setDescricao(dto.getDescricao());
            processo.setProcesso(dto.getProcesso());
            processo.setUsuarioProcesso(new Usuario(dto.getUsuarioProcesso()));
            return new ResponseEntity<>(new ProcessoDTO(repo.save(processo)), HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseBody
    @RequestMapping(path = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity update(@PathVariable Long id,
                                 @RequestParam(required = false) String parecer,
                                 @RequestBody ProcessoDTO dto) {
        try {
            boolean validarParecer = (parecer != null && parecer.equalsIgnoreCase("t"));
            dto.validarDados(validarParecer);
            Optional<Processo> processoData = repo.findById(id);
            if (processoData.isPresent()) {
                Processo processoNew = processoData.get();

                if (validarParecer) {
                    processoNew.setDataParecer(new Date());
                    processoNew.setParecer(dto.getParecer());
                    processoNew.setUsuarioParecer(new Usuario(dto.getUsuarioParecer()));

                } else {

                    processoNew.setDescricao(dto.getDescricao());
                    processoNew.setProcesso(dto.getProcesso());
                    processoNew.setUsuarioProcesso(new Usuario(dto.getUsuarioProcesso()));
                    processoNew.setUsuariosParecer(new HashSet<>());
                    for (UsuarioDTO usuarioDTO : dto.getUsuariosParecer()) {
                        processoNew.getUsuariosParecer().add(new Usuario(usuarioDTO));
                    }
                }
                return new ResponseEntity<>(new ProcessoDTO(repo.save(processoNew)), HttpStatus.OK);
            }
            return new ResponseEntity<>("Usuário não encontrado", HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseBody
    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity delete(@PathVariable Long id) {
        try {
            Optional<Processo> processoData = repo.findById(id);
            if (processoData.isPresent()) {
                repo.deleteById(id);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>("Usuário não encontrado", HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
