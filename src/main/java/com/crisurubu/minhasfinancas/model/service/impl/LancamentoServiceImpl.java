package com.crisurubu.minhasfinancas.model.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crisurubu.minhasfinancas.model.entities.Lancamento;
import com.crisurubu.minhasfinancas.model.enums.StatusLancamento;
import com.crisurubu.minhasfinancas.model.enums.TipoLancamento;
import com.crisurubu.minhasfinancas.model.exceptions.RegraNegocioException;
import com.crisurubu.minhasfinancas.model.repository.LancamentoRepository;
import com.crisurubu.minhasfinancas.model.service.LancamentoService;

@Service
public class LancamentoServiceImpl implements LancamentoService{

	private LancamentoRepository repository;
	
	public LancamentoServiceImpl(LancamentoRepository repository) {
		this.repository = repository;
	}
	
	
	@Override
	@Transactional
	public Lancamento salvar(Lancamento lancamento) {
		validar(lancamento);
		lancamento.setStatus(StatusLancamento.PENDENTE);
		return repository.save(lancamento);
	}

	@Override
	@Transactional
	public Lancamento atualizar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		validar(lancamento);
		return repository.save(lancamento);
	}

	@Override
	@Transactional
	public void deletar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		repository.delete(lancamento);
		
	}

	@Override
	@Transactional(readOnly = true)
	public List<Lancamento> buscar(Lancamento lancamentoFiltro) {
		Example example = Example.of(lancamentoFiltro,
										ExampleMatcher.matching()
										.withIgnoreCase()
										.withStringMatcher(StringMatcher.CONTAINING));
		
		
		return repository.findAll(example);
		
		
	}

	@Override
	public void atualizarStatus(Lancamento lancamento, StatusLancamento status) {
		lancamento.setStatus(status);
		atualizar(lancamento);
		
		
	}


	@Override
	public void validar(Lancamento lancamento) {
		if (lancamento.getDescricao() == null || lancamento.getDescricao().trim().equals("")) {
			throw new RegraNegocioException("Informe uma descrição válida");
		}
		if(lancamento.getMes() == null || lancamento.getMes() <1 || lancamento.getMes() > 12) {
			throw new RegraNegocioException("Informe um mes válido");
		}
		if(lancamento.getAno() == null || lancamento.getAno().toString().length() !=4) {
			throw new RegraNegocioException("Informe um ano Válido");
		}
		if(lancamento.getUsuario() == null || lancamento.getUsuario().getId() == null) {
			throw new RegraNegocioException("Informe o Usuario");
			
		}
		if(lancamento.getValor() == null || lancamento.getValor().compareTo(BigDecimal.ZERO) < 1 ) {
			throw new RegraNegocioException("Informe um valor válido");
		}
		if(lancamento.getTipo() == null) {
			throw new RegraNegocioException("Informe um tipo de lancamento");
		}
	}


	@Override
	public Optional<Lancamento> obterPorId(Long id) {
		
		return repository.findById(id);
	}


	@Override
	@Transactional(readOnly = true)
	public BigDecimal obterSaldoPorUsuario(Long id) {
		
		BigDecimal receitas =  repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(id, TipoLancamento.RECEITA, StatusLancamento.EFETIVADO );
		BigDecimal despesas = repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(id, TipoLancamento.DESPESA, StatusLancamento.EFETIVADO);
		
		if(receitas == null) {
			receitas = BigDecimal.ZERO;
		}
		if(despesas == null) {
			despesas = BigDecimal.ZERO;
		}
		
		return receitas.subtract(despesas);
	}


	

}
