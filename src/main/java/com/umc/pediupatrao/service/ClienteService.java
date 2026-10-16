package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    public Cliente novoCliente(Cliente cliente) {
        return salvar(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public void excluir(String id) {
        clienteRepository.deleteById(id);
    }

    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    public Cliente salvar(Cliente cliente) {
        if (cliente.getId() == null || cliente.getId().isBlank()) {
            Cliente salvo = clienteRepository.save(cliente);
            auditoriaService.registrar(
                    "INCLUSÃO DE CLIENTE",
                    "CLIENTE",
                    salvo.getId(),
                    "cadastro",
                    null,
                    salvo.getNome(),
                    null
            );
            return salvo;
        }

        Cliente anterior = clienteRepository.findById(cliente.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado para atualização."));

        auditarCampoSeAlterado(cliente.getId(), "nome", anterior.getNome(), cliente.getNome());
        auditarCampoSeAlterado(cliente.getId(), "telefone", anterior.getTelefone(), cliente.getTelefone());
        auditarCampoSeAlterado(cliente.getId(), "cep", anterior.getCep(), cliente.getCep());
        auditarCampoSeAlterado(cliente.getId(), "logradouro", anterior.getLogradouro(), cliente.getLogradouro());
        auditarCampoSeAlterado(cliente.getId(), "numero", anterior.getNumero(), cliente.getNumero());
        auditarCampoSeAlterado(cliente.getId(), "complemento", anterior.getComplemento(), cliente.getComplemento());
        auditarCampoSeAlterado(cliente.getId(), "bairro", anterior.getBairro(), cliente.getBairro());
        auditarCampoSeAlterado(cliente.getId(), "cidade", anterior.getCidade(), cliente.getCidade());
        auditarCampoSeAlterado(cliente.getId(), "estado", anterior.getEstado(), cliente.getEstado());

        return clienteRepository.save(cliente);
    }

    private void auditarCampoSeAlterado(String clienteId, String campo, String valorAntigo, String valorNovo) {
        if (!Objects.equals(valorAntigo, valorNovo)) {
            auditoriaService.registrar(
                    "ALTERAÇÃO DE CLIENTE",
                    "CLIENTE",
                    clienteId,
                    campo,
                    valorAntigo,
                    valorNovo,
                    null
            );
        }
    }
}
