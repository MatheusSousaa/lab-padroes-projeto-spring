package one.digitalinnovation.gof.service.impl;

import java.util.Objects;

import one.digitalinnovation.gof.exceptions.ClienteNaoEncontradoException;
import one.digitalinnovation.gof.exceptions.IdInvalidoException;
import org.springframework.stereotype.Service;

import one.digitalinnovation.gof.model.Cliente;
import one.digitalinnovation.gof.model.Endereco;
import one.digitalinnovation.gof.repository.ClienteRepository;
import one.digitalinnovation.gof.repository.EnderecoRepository;
import one.digitalinnovation.gof.service.ClienteService;
import one.digitalinnovation.gof.service.ViaCepService;

@Service
public class ClienteServiceImpl implements ClienteService {

	private final ClienteRepository clienteRepository;
	private final EnderecoRepository enderecoRepository;
	private final ViaCepService viaCepService;

	public ClienteServiceImpl(
			ClienteRepository clienteRepository,
			EnderecoRepository enderecoRepository,
			ViaCepService viaCepService) {
		this.clienteRepository = clienteRepository;
		this.enderecoRepository = enderecoRepository;
		this.viaCepService = viaCepService;
	}

	@Override
	public Iterable<Cliente> buscarTodos() {
		return clienteRepository.findAll();
	}

	@Override
	public Cliente buscarPorId(Long id) {
		validarId(id);

		return clienteRepository.findById(id)
				.orElseThrow(() -> new ClienteNaoEncontradoException(
						"Cliente não encontrado"));
	}

	@Override
	public void inserir(Cliente cliente) {
		validarCliente(cliente);
		salvarClienteComCep(cliente);
	}

	@Override
	public void atualizar(Long id, Cliente cliente) {
		validarId(id);
		validarCliente(cliente);

		Cliente clienteExistente = buscarPorId(id);

		// Preserve o ID do cliente encontrado.
		cliente.setId(clienteExistente.getId());

		salvarClienteComCep(cliente);
	}

	@Override
	public void deletar(Long id) {
		validarId(id);
		buscarPorId(id);
		clienteRepository.deleteById(id);
	}

	private void salvarClienteComCep(Cliente cliente) {
		Endereco endereco = Objects.requireNonNull(
				cliente.getEndereco(), "Endereço é obrigatório");

		String cep = endereco.getCep();

		if (cep == null || cep.isBlank()) {
			throw new IdInvalidoException("CEP é obrigatório");
		}

		Endereco enderecoEncontrado = enderecoRepository.findById(cep)
				.orElseGet(() -> {
					Endereco novoEndereco = viaCepService.consultarCep(cep);
					return enderecoRepository.save(novoEndereco);
				});

		cliente.setEndereco(enderecoEncontrado);
		clienteRepository.save(cliente);
	}

	private void validarCliente(Cliente cliente) {
		Objects.requireNonNull(cliente, "Cliente não pode ser nulo");
	}

	private void validarId(Long id) {
		if (id == null || id <= 0) {
			throw new IdInvalidoException("ID inválido");
		}
	}
}
