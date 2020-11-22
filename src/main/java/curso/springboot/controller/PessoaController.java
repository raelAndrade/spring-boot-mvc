package curso.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.model.Telefone;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.ProfissaoRepository;
import curso.springboot.repository.TelefoneRepository;
import curso.springboot.util.ReportUtil;

@Controller
public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@Autowired
	private ProfissaoRepository profissaoRepository;
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private ReportUtil reportUtil;
	
	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView inicio() {
		ModelAndView mv = new ModelAndView("cadastro/cadastropessoa");
		mv.addObject("pessoa", new Pessoa());
		mv.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		mv.addObject("profissoes", profissaoRepository.findAll());
		return mv;
	}
	
	@GetMapping("/pessoaspag")
	public ModelAndView carregaPessoaPorPaginacao(@PageableDefault(size = 5) Pageable pageable, 
			ModelAndView model, 
			@RequestParam("nomepesquisa") String nomepesquisa) {
		
		Page<Pessoa> pagePessoa = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		
		model.addObject("pessoas", pagePessoa);
		model.addObject("pessoa", new Pessoa());
		model.addObject("nomepesquisa", nomepesquisa);
		model.setViewName("cadastro/cadastropessoa");
		
		return model;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa", consumes = {"multipart/form-data"} )
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file) throws IOException {
		
		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));
		
		if(bindingResult.hasErrors()) {
			ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
			modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
			modelAndView.addObject("pessoa", pessoa);
			
			List<String> msg = new ArrayList<String>();
			for(ObjectError error : bindingResult.getAllErrors()) {
				msg.add(error.getDefaultMessage()); // vem das anotações @NotEmpty e outras
			}
			
			modelAndView.addObject("msg", msg);
			modelAndView.addObject("profissoes", profissaoRepository.findAll());
			return modelAndView;
		}
		
		if(file.getSize() > 0) { // Cadastrando um novo curriculo
			pessoa.setCurriculo(file.getBytes());
			pessoa.setTipoFileCurriculo(file.getContentType());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());
		}else {
			if(pessoa.getId() != null && pessoa.getId() > 0) { // Editando
				
				Pessoa pessoaTemp = pessoaRepository.findById(pessoa.getId()).get();
				
				pessoa.setCurriculo(pessoaTemp.getCurriculo() );
				pessoa.setTipoFileCurriculo(pessoaTemp.getTipoFileCurriculo());
				pessoa.setNomeFileCurriculo(pessoaTemp.getNomeFileCurriculo());
			}
		}
		
		pessoaRepository.save(pessoa);
		
		ModelAndView mv = new ModelAndView("cadastro/cadastropessoa");
		mv.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		mv.addObject("pessoa", new Pessoa());
		
		return mv;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView pessoas() {
		ModelAndView mv = new ModelAndView("cadastro/cadastropessoa");
		mv.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("id"))));
		mv.addObject("pessoa", new Pessoa());
		return mv;
	}
	
	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {
		 ModelAndView mv = new ModelAndView("cadastro/cadastropessoa");
		 Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		 mv.addObject("pessoa", pessoa.get());
		 mv.addObject("profissoes", profissaoRepository.findAll());
		 mv.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		 
		 return mv;
	}
	
	@GetMapping("/removerpessoa/{idpessoa}")
	public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {
		 ModelAndView mv = new ModelAndView("cadastro/cadastropessoa");
		 pessoaRepository.deleteById(idpessoa);
		 mv.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		 mv.addObject("pessoa", new Pessoa());
		 return mv;
	}	
	
	@PostMapping("**/pesquisarpessoa")
	public ModelAndView pesquisar(
			@RequestParam("nomepesquisa") String nomepesquisa, 
			@RequestParam("sexopesquisa") String sexopesquisa, 
			@PageableDefault(size = 5, sort = {"nome"}) Pageable pageable) {
		
		Page<Pessoa> pessoas = null;
		
		if(sexopesquisa != null && !sexopesquisa.isEmpty()) {
			pessoas = pessoaRepository.findPessoaBySexoPage(nomepesquisa, sexopesquisa, pageable);
		}else {
			pessoas = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		}
		
		ModelAndView mv = new ModelAndView("cadastro/cadastropessoa");
		mv.addObject("pessoas", pessoas);
		mv.addObject("pessoa", new Pessoa());
		mv.addObject("profissoes", profissaoRepository.findAll());
		mv.addObject("nomepesquisa", nomepesquisa);
		
		return mv;
	}
	
	@GetMapping("**/baixarcurriculo/{idpessoa}")
	public void baixarCurriculo(@PathVariable("idpessoa") Long idpessoa, HttpServletResponse response) throws IOException{
		
		/* Consultar o objeto pessoa no banco de dados */
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
		if(pessoa.getCurriculo() != null) {
			
			/* Setar o tamanho da resposta */
			response.setContentLength(pessoa.getCurriculo().length);
			
			/* Tipo do arquivo para download ou pode ser generica application/octet-stream */
			response.setContentType(pessoa.getTipoFileCurriculo());
			
			/* Define o cabeçalho da resposta */
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", pessoa.getNomeFileCurriculo());
			response.setHeader(headerKey, headerValue);
			
			/* Finaliza a resposta passando o arquivo */
			response.getOutputStream().write(pessoa.getCurriculo());
			
		}
	}
	
	@GetMapping("**/pesquisarpessoa")
	public void imprimePDF(
			@RequestParam("nomepesquisa") String nomepesquisa, 
			@RequestParam("sexopesquisa") String sexopesquisa, 
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		List<Pessoa> pessoas = new ArrayList<Pessoa>();
		
		if(sexopesquisa != null && !sexopesquisa.isEmpty() && nomepesquisa != null && !nomepesquisa.isEmpty()) { /* Busca por nome e sexo*/
			
			pessoas = pessoaRepository.findPessoaByNameSexo(nomepesquisa, sexopesquisa);
			
		}else if(nomepesquisa != null && !nomepesquisa.isEmpty()){ /* Busca somente por nome */
			
			pessoas = pessoaRepository.findPessoaByName(nomepesquisa);
			
		}else if(sexopesquisa != null && !sexopesquisa.isEmpty()){ /* Busca somente por sexo */
				
				pessoas = pessoaRepository.findPessoaBySexo(sexopesquisa);
				
		}else { /* Busca todos */
			Iterable<Pessoa> interator = pessoaRepository.findAll();
			for (Pessoa pessoa : interator) {
				pessoas.add(pessoa);
			}
		}
		
		/* Chamar o serviço que faz a geração do realatório */
		@SuppressWarnings("unchecked")
		byte[] pdf = reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());

		/* Tamanho da resposta */
		response.setContentLength(pdf.length);
		
		/* Definir na resposta o tipo de arquivo */
		response.setContentType("application/octet-stream");
		
		/* Definir o cabeçalho da resposta */
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
		
		response.setHeader(headerKey, headerValue);
		
		/* Finaliza a resposta pro navegador */
		response.getOutputStream().write(pdf);
	}
	
	@GetMapping("/telefones/{idpessoa}")
	public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa) {
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		ModelAndView mv = new ModelAndView("cadastro/telefones");
		mv.addObject("pessoa", pessoa.get());
		mv.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
		return mv;
	}
	
	@PostMapping("**/addfonePessoa/{pessoaid}")
	public ModelAndView addFonePessoa(Telefone telefone, @PathVariable("pessoaid") Long pessoaid) {
		Pessoa pessoa = pessoaRepository.findById(pessoaid).get();
		
		if(telefone != null && telefone.getNumero().isEmpty() || telefone.getTipo().isEmpty()) { 
				
			ModelAndView mv = new ModelAndView("cadastro/telefones");
			mv.addObject("pessoa", pessoa);
			mv.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
			
			List<String> msg = new ArrayList<String>();
			if(telefone.getNumero().isEmpty()) {
				msg.add("Número deve ser informado");
				mv.addObject("msg", msg);				
			}
			if(telefone.getTipo().isEmpty()) {
				msg.add("Tipo deve ser informado");
				mv.addObject("msg", msg);
			}
			return mv;
		}
		
		ModelAndView mv = new ModelAndView("cadastro/telefones");
		
		telefone.setPessoa(pessoa);
		
		telefoneRepository.save(telefone);
		
		mv.addObject("pessoa", pessoa);
		mv.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
		return mv;
	}
	
	@GetMapping("/removertelefone/{idtelefone}")
	public ModelAndView excluirTelefone(@PathVariable("idtelefone") Long idtelefone) {
		
		Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa();
		
		telefoneRepository.deleteById(idtelefone);
		 
		ModelAndView mv = new ModelAndView("cadastro/telefones");
		 
		mv.addObject("pessoa", pessoa);
		mv.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));
		mv.addObject("pessoa", new Pessoa());
		 
		return mv;
	}
	
}
