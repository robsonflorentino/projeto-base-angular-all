package br.com.tricard.db.sql.sgad.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.reflections.Reflections;

public class CodeGenerator {

	private static final Logger LOGGER = Logger.getLogger(CodeGenerator.class);
	private static final String PACOTE_PROJETO = "sgad";

	private static final String DIRETORIO_ARQUIVO = "/home/kenhiti/tribanco/Projeto/trb_workspace/";

	private static final String DIRETORIO_ARQUIVO_REPOSITORY = DIRETORIO_ARQUIVO
			+ "sql-sgad-sgad-db-lib-jar/src/main/java/br/com/tricard/db/sql/sgad/repository/";
	private static final String DIRETORIO_ARQUIVO_BUSINESS_API = DIRETORIO_ARQUIVO
			+ "sgad-business-api/src/main/java/br/com/adquirencia/sgad/business/";
	private static final String DIRETORIO_ARQUIVO_BUSINESS_TEST = DIRETORIO_ARQUIVO
			+ "sgad-business-api/src/test/java/br/com/adquirencia/sgad/business/test/";
	private static final String DIRETORIO_ARQUIVO_BUSINESS_TEST_DATABUILDER = DIRETORIO_ARQUIVO
			+ "sgad-business-api/src/test/java/br/com/adquirencia/sgad/business/test/databuilder/";

	private static final String NOME_CLASSE_CONTAINS = "TblFluxoAcao";

	public static void main(String[] args) {
		try {
			generate();
			System.out.println("Gerado"); // NOSONAR
		} catch (Exception e) {
			LOGGER.error(ExceptionUtils.getRootCause(e));
		}
	}

	/**
	 * @throws Exception
	 * 
	 */
	@SuppressWarnings("rawtypes")
	private static void generate() throws Exception {
		try {
			Reflections reflections = new Reflections("br.com.tricard.db.sql.sgad.models");
			Set<Class<? extends Object>> classes = reflections.getTypesAnnotatedWith(Entity.class);
			for (Class clazz : classes) {
				if (!clazz.getName().contains(NOME_CLASSE_CONTAINS)) {
					continue;
				}
				criarArquivoRepositorio(clazz, criarRepositorio(clazz));
				criarArquivoBizz(clazz, criarBizz(clazz));
				criarArquivoBizzService(clazz, criarBizzService(clazz));
				criarArquivoBizzTest(clazz, criarBizzTest(clazz));
				criarArquivoTestDataBuilder(clazz, criarTestDataBuilder(clazz));

			}
		} catch (Exception e) {
			LOGGER.error(ExceptionUtils.getRootCause(e));
			throw e;
		}
	}

	@SuppressWarnings("rawtypes")
	private static void criarArquivoRepositorio(Class clazz, String fileContent) throws Exception { // NOSONAR
		try {
			File file = new File(DIRETORIO_ARQUIVO_REPOSITORY + clazz.getSimpleName() + "Repository.java");

			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(fileContent);
			bw.close();
		} catch (Exception e) {
			LOGGER.error(ExceptionUtils.getRootCause(e));
			throw e;
		}
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static String criarRepositorio(Class clazz) {
		try {
			Method[] methods = clazz.getDeclaredMethods();
			Class idClass = null;
			for (Method method : methods) {
				if (method.getAnnotationsByType(Id.class).length > 0
						|| method.getAnnotationsByType(EmbeddedId.class).length > 0) {
					idClass = method.getReturnType();
				}
			}

			StringBuilder sb = new StringBuilder();
			sb.append("package com.br." + PACOTE_PROJETO + ".repository;\n");
			sb.append("import javax.enterprise.context.RequestScoped;\n");
			sb.append("import javax.inject.Inject;\n"); // NOSONAR
			sb.append("import javax.persistence.EntityManager;\n");
			sb.append("import com.br." + PACOTE_PROJETO + ".repository.base.Repository;\n"); // NOSONAR
			sb.append("import com.br." + PACOTE_PROJETO + ".models." + clazz.getSimpleName() + ";\n");
			sb.append("\n/**\n"); // NOSONAR
			sb.append(" * \n"); // NOSONAR
			sb.append(" * @author ServiceCodeGeneratorSgad\n"); // NOSONAR
			sb.append(" *\n");
			sb.append(" */\n"); // NOSONAR
			sb.append("@RequestScoped\n");
			sb.append("public class " + clazz.getSimpleName() + "Repository extends Repository<" + clazz.getSimpleName() // NOSONAR
					+ ", " + idClass.getSimpleName() + "> {\n"); // NOSONAR
			sb.append("\n");
			sb.append("public " + clazz.getSimpleName() + "Repository() {\n");
			sb.append("}\n");
			sb.append("}\n");
			return sb.toString();
		} catch (Exception e) {
			LOGGER.error(ExceptionUtils.getRootCause(e));
			throw e;
		}
	}

	/**
	 * Envolve o texto do {@link StringBuilder} informado com uma chamada ao
	 * metodo ResponseUtils.toResponse()
	 * 
	 * @param stringBuilder
	 *            O proprio StringBuilder (se necessario)
	 * @return
	 */
	private static StringBuilder envolverStringBuilderComToResponse(StringBuilder stringBuilder) {

		StringBuilder builderEnvolvido = new StringBuilder();
		builderEnvolvido.append("return ResponseUtils.toResponse(");
		builderEnvolvido.append(stringBuilder.toString());
		builderEnvolvido.append(");\n");
		return builderEnvolvido;
	}

	/**
	 * 
	 * Adiciona Javadoc para metodos delete
	 * 
	 * @param sb
	 * @param nomeEntidade
	 * @return
	 */
	private static StringBuilder adicionarComentarioDelete(StringBuilder sb, String nomeEntidade) {

		sb.append("\n/**\n");
		sb.append(" * Exclui uma entidade " + nomeEntidade + " informada\n");
		sb.append(" * \n");
		sb.append(" * @param id Identificador da entidade\n");
		sb.append(" * @author ServiceCodeGeneratorSgad\n");
		sb.append(" * @return");
		sb.append(" *\n");
		sb.append(" */\n");

		return sb;
	}

	/**
	 * Adiciona Javadoc para metodos getById
	 * 
	 * @param sb
	 * @param nomeEntidade
	 * @return
	 */
	private static StringBuilder adicionarComentarioGetById(StringBuilder sb, String nomeEntidade) {

		sb.append("\n/**\n");
		sb.append(" * Recupera uma entidade " + nomeEntidade + " com base no identificador\n");
		sb.append(" * \n");
		sb.append(" * @author ServiceCodeGeneratorSgad\n");
		sb.append(" * @param id Identificado" + "r da entidade\n");
		sb.append(" * @return Entidade " + nomeEntidade + " recuperada \n");
		sb.append(" *\n");
		sb.append(" */\n");

		return sb;
	}

	/**
	 * Adicionar Javadoc para metodos update e partialUpdate
	 * 
	 * @param sb
	 * @param nomeEntidade
	 * @return
	 */
	private static StringBuilder adicionarComentarioUpdates(StringBuilder sb, String nomeEntidade) {

		sb.append("\n/**\n");
		sb.append(" * Atualiza uma entidade " + nomeEntidade + " informada\n");
		sb.append(" * \n");
		sb.append(" * @author ServiceCodeGeneratorSgad\n");
		sb.append(" * @param id Identificador da entidade\n");
		sb.append(" * @param obj Objeto que representa um registro da entidade\n");
		sb.append(" * @return Objeto atualizado\n");
		sb.append(" *\n");
		sb.append(" */\n");

		return sb;
	}

	@SuppressWarnings("rawtypes")
	private static String criarBizzService(Class clazz) throws NoSuchFieldException, SecurityException { // NOSONAR
		Field[] fields = clazz.getDeclaredFields();
		StringBuilder sb = new StringBuilder();

		///// Cabecalho

		sb.append("package com.br.sgad.bl.services;\n\n");

		sb.append(criarImportsBizzService(clazz));
		sb.append("\n/**\n");
		sb.append(" * Classe de servicos de negocio relacionados a " + clazz.getSimpleName() + "\n");
		sb.append(" * \n");
		sb.append(" * @author ServiceCodeGeneratorSgad\n");
		sb.append(" *\n");
		sb.append(" */\n");
		sb.append("@RequestScoped\n"); // NOSONAR
		sb.append("@Path(\"/" + clazz.getSimpleName().toLowerCase() + "\")\n"); // NOSONAR
		sb.append("@Api(value = \"" + clazz.getSimpleName() + "\")\n");
		sb.append("@JsonInclude(Include.ALWAYS) \n");
		sb.append("public class " + clazz.getSimpleName() + "BizzService{\n\n");

		sb.append("@Inject\n");
		sb.append("private " + clazz.getSimpleName() + "Bizz bizz;\n\n"); // NOSONAR

		///// getAll

		sb.append("\n/**\n");
		sb.append(" * Recupera todas as entidades" + clazz.getSimpleName() + "\n");
		sb.append(" * \n");
		sb.append(" * @author ServiceCodeGeneratorSgad\n");
		sb.append("{0}\n");
		sb.append(" * @return Lista de entidades " + clazz.getSimpleName() + "\n");
		sb.append(" *\n");
		sb.append(" */\n");
		sb.append("@GET\n"); // NOSONAR
		sb.append("@ApiOperation(value = \"Consulta de entidades " + clazz.getSimpleName()
				+ ".\", notes = \"Serviço responsável por retornar uma lista de entidades do tipo "
				+ clazz.getSimpleName() + ".\")\n"); // NOSONAR
		sb.append("@Produces(MediaType.APPLICATION_JSON)\n"); // NOSONAR
		sb.append("@Consumes(MediaType.APPLICATION_JSON)\n"); // NOSONAR

		sb.append("public ResponseEntity get" + clazz.getSimpleName() + "ALL(\n");
		sb.append(
				"@ApiParam(value = \"Índice da página desejada.\", required = false) @QueryParam(\"page\") @DefaultValue(\"1\") Integer page\n");
		sb.append(
				", @ApiParam(value = \"Quantidade de registros por página.\", required = false) @QueryParam(\"pagesize\") @DefaultValue(\"10\") Integer pagesize\n");

		StringBuilder stringBuilderParametrosGetAll = new StringBuilder();
		stringBuilderParametrosGetAll.append(" * @param page Identicador da pagina de resultados a ser retornada \n");
		stringBuilderParametrosGetAll.append(" * @param pagesize Tamanho da pagina de resultados \n");

		for (Field field : fields) {
			field.setAccessible(true);
			if (!field.getName().equals("serialVersionUID") && !field.getName().equals(getIdName(clazz))) { // NOSONAR
				sb.append(",@ApiParam(value = \".\", required = false) @QueryParam(\"" + field.getName() + "\")"
						+ (field.getType().getSimpleName().equals("Character") ? "String" // NOSONAR
								: field.getType().getSimpleName())
						+ " " + field.getName());

				stringBuilderParametrosGetAll.append(" * @param " + field.getName());
			}
		}

		StringBuilder builderCorpoGetAll = new StringBuilder();
		builderCorpoGetAll.append("bizz.getALL(page, pagesize "); // NOSONAR

		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			if (!fields[i].getName().equals("serialVersionUID") && !fields[i].getName().equals(getIdName(clazz))) { // NOSONAR

				builderCorpoGetAll.append(", " + ((fields[i].getType().getSimpleName().equals("Character") // NOSONAR
						? ("(" + fields[i].getName() + " == null ? null : " + fields[i].getName() + ".charAt(0)) ")
						: fields[i].getName())));
			}
		}
		builderCorpoGetAll.append(")");
		builderCorpoGetAll = envolverStringBuilderComToResponse(builderCorpoGetAll);
		sb.append(")\n{");
		sb.append(builderCorpoGetAll.toString());
		sb.append("}\n");

		StringBuilder saida = new StringBuilder();
		saida.append(sb.toString().replaceAll("\\{0\\}", stringBuilderParametrosGetAll.toString()));

		///// getById

		sb = new StringBuilder();
		sb = adicionarComentarioGetById(sb, clazz.getSimpleName());

		sb.append("@GET\n");
		sb.append(createPath(clazz));
		sb.append("@ApiOperation(value = \"Consulta de uma entidade " + clazz.getSimpleName()
				+ " pelo seu identificador.\", notes = \"Serviço responsável por retornar uma entidade "
				+ clazz.getSimpleName() + ", baseado no seu identificador.\")\n");
		sb.append("@Produces(MediaType.APPLICATION_JSON)\n");
		sb.append("@Consumes(MediaType.APPLICATION_JSON)\n");
		sb.append("public ResponseEntity get" + clazz.getSimpleName() + "ById(\n");
		sb.append(createIdParams(clazz));
		sb.append("){\n");

		StringBuilder builderCorpoFindById = envolverStringBuilderComToResponse(
				new StringBuilder("bizz.get" + clazz.getSimpleName() + "ById(id)"));
		sb.append(builderCorpoFindById.toString());
		sb.append("\n}\n");
		saida.append(sb.toString());

		///// save

		sb = new StringBuilder();
		sb.append("\n/**\n");
		sb.append(" * Salva uma entidade " + clazz.getSimpleName() + " informada\n");
		sb.append(" * \n");
		sb.append(" * @author ServiceCodeGeneratorSgad\n");
		sb.append(" * @param obj Objeto que representa um registro da entidade\n");
		sb.append(" * @return Entidade atualizada\n");
		sb.append(" *\n");
		sb.append(" */\n");

		sb.append("@POST\n");
		sb.append("@ApiOperation(value = \"Cadastro de um " + clazz.getSimpleName()
				+ ".\", notes = \"Serviço responsável por cadastrar um registro de " + clazz.getSimpleName()
				+ ".\")\n");
		sb.append("@Produces(MediaType.APPLICATION_JSON)\n");
		sb.append("@Consumes(MediaType.APPLICATION_JSON)\n");
		sb.append("public ResponseEntity save" + clazz.getSimpleName() + "(\n");
		sb.append("@ApiParam(value = \"Objeto que representa um registro da entidade " + clazz.getSimpleName()
				+ ".\", required = true) " + clazz.getSimpleName() + " obj) {\n"); // NOSONAR

		StringBuilder builderCorpoSave = new StringBuilder("bizz.save(obj)");
		builderCorpoSave = envolverStringBuilderComToResponse(builderCorpoSave);
		sb.append(builderCorpoSave);
		sb.append("}\n");

		saida.append(sb);

		///// update

		sb = new StringBuilder();
		sb = adicionarComentarioUpdates(sb, clazz.getSimpleName());

		sb.append("@PUT\n");
		sb.append(createPath(clazz));
		sb.append("@ApiOperation(value = \"Atualização de uma entidade do tipo " + clazz.getSimpleName()
				+ " \", notes = \"Serviço responsável por atualizar um registro de uma entidade "
				+ clazz.getSimpleName() + " na base de dados.\")\n"); // NOSONAR
		sb.append("@Produces(MediaType.APPLICATION_JSON)\n");
		sb.append("@Consumes(MediaType.APPLICATION_JSON)\n");
		sb.append("public ResponseEntity update" + clazz.getSimpleName() + "(\n");
		sb.append(createIdParams(clazz));
		sb.append(",@ApiParam(value = \"Objeto que representa um registro da entidade " + clazz.getSimpleName() // NOSONAR
				+ ".\", required = true) " + clazz.getSimpleName() + " obj) {\n");

		StringBuilder builderCorpoUpdate = new StringBuilder("bizz.update(id, obj)");
		builderCorpoUpdate = envolverStringBuilderComToResponse(builderCorpoUpdate);
		sb.append(builderCorpoUpdate);
		sb.append("}\n");

		saida.append(sb.toString());

		///// delete

		sb = new StringBuilder();
		sb = adicionarComentarioDelete(sb, clazz.getSimpleName());
		sb.append("@DELETE\n");
		sb.append(createPath(clazz));
		sb.append("@ApiOperation(value = \"Serviço responsável pela remoção de uma entidade " + clazz.getSimpleName()
				+ " \", notes = \"Serviço responsável pela remoção de uma entidade " + clazz.getSimpleName()
				+ " pelo seu identificador.\")\n");
		sb.append("@Produces(MediaType.APPLICATION_JSON)\n");
		sb.append("@Consumes(MediaType.APPLICATION_JSON)\n");
		sb.append("public ResponseEntity delete" + clazz.getSimpleName() + "(\n");
		sb.append(createIdParams(clazz));
		sb.append("){\n");
		sb.append("bizz.delete(id); \n");
		sb.append("return ResponseUtils.toResponse(null);");
		sb.append("}\n");

		sb.append("}");

		saida.append(sb.toString());

		return saida.toString();
	}

	@SuppressWarnings("rawtypes")
	private static void criarArquivoBizzService(Class clazz, String fileContent) throws Exception { // NOSONAR
		try {
			File file = new File(
					DIRETORIO_ARQUIVO_BUSINESS_API + "services/" + clazz.getSimpleName() + "BizzService.java");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(fileContent);
			bw.close();
		} catch (Exception e) {
			LOGGER.error(ExceptionUtils.getRootCause(e));
			throw e;
		}
	}

	@SuppressWarnings("rawtypes")
	private static String criarBizz(Class clazz) throws NoSuchFieldException, SecurityException { // NOSONAR
		Field[] fields = clazz.getDeclaredFields();
		StringBuilder sb = new StringBuilder();

		sb.append("package com.br.sgad.bl.bizz;\n\n");

		sb.append("import java.util.Date;\n"); // NOSONAR
		sb.append("import java.util.List;\n"); // NOSONAR
		sb.append("import javax.enterprise.context.RequestScoped;\n");
		sb.append("import javax.inject.Inject;\n");
		sb.append("import com.br.sgad.bl.consumers.Consumer" + clazz.getSimpleName() + ";\n");
		sb.append("import com.br.sgad.bl.dto." + clazz.getSimpleName() + "DTO;\n"); // NOSONAR
		sb.append("\n/**\n");
		sb.append(" * \n");
		sb.append(" * @author ServiceCodeGeneratorSgad\n");
		sb.append(" *\n");
		sb.append(" */\n");
		sb.append("@RequestScoped\n");
		sb.append("public class " + clazz.getSimpleName() + "Bizz{\n\n");

		sb.append("@Inject\n");
		sb.append("private " + clazz.getSimpleName() + "Repository repository;\n\n");

		sb.append("@Transactional(Transactional.TxType.NOT_SUPPORTED)\n");
		sb.append("public List<" + clazz.getSimpleName() + "> getALL(Integer page, Integer pagesize, ");
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			if (!fields[i].getName().equals("serialVersionUID") && !fields[i].getName().equals(getIdName(clazz))) { // NOSONAR
				sb.append(fields[i].getType().getSimpleName() + " " + fields[i].getName());
				if (i != (fields.length - 1)) {
					sb.append(", ");
				}
			}
		}
		sb.append("){\n ");

		sb.append("return repository.findByExample(new " + clazz.getSimpleName() + "(");
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			if (!fields[i].getName().equals("serialVersionUID") && !fields[i].getName().equals(getIdName(clazz))) { // NOSONAR
				sb.append(fields[i].getName());

				if (i != fields.length - 1) {
					sb.append(", ");
				}
			}
		}
		sb.append("), page, pagesize);\n");
		sb.append("}\n\n"); // NOSONAR

		sb.append("@Transactional(Transactional.TxType.NOT_SUPPORTED)\n");
		sb.append("public " + clazz.getSimpleName() + " get" + clazz.getSimpleName() + "ById("
				+ createBasicIdParams(clazz) + ") {\n"); // NOSONAR
		sb.append("if (id == null) {\n"); // NOSONAR
		sb.append("throw new IllegalArgumentException(MsgConstants.IDENTIFICADOR_NAO_PODE_SER_NULO);\n"); // NOSONAR
		sb.append("}\n");

		sb.append("return repository.findById(" + clazz.getSimpleName() + ".class, id.longValue());\n");
		sb.append("}\n\n");

		sb.append("@Transactional\n");
		sb.append("public " + clazz.getSimpleName() + " save(" + clazz.getSimpleName() + " obj) {\n");
		sb.append("if (obj == null) {\n"); // NOSONAR
		sb.append("throw new IllegalArgumentException(MsgConstants.NENHUM_PARAMETRO_FOI_INFORMADO);\n");
		sb.append("}\n");
		sb.append("return repository.save(obj);\n");
		sb.append("}\n\n");

		sb.append("@Transactional\n");
		sb.append("public " + clazz.getSimpleName() + "  update( " + createBasicIdParams(clazz) + ", "
				+ clazz.getSimpleName() + " obj) {\n");
		sb.append("if (id == null) {\n");
		sb.append("throw new IllegalArgumentException(MsgConstants.IDENTIFICADOR_NAO_PODE_SER_NULO);\n");
		sb.append("}\n");
		sb.append("if (obj == null) {\n");
		sb.append("throw new IllegalArgumentException(MsgConstants.NENHUM_PARAMETRO_PARA_ATUALIZAR_FOI_INFORMADO);\n");
		sb.append("}\n");

		String name = "";

		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(Id.class)) {
				name = method.getName();
				break;
			}
		}

		sb.append("obj." + name.replace("get", "set") + "(id);\n");
		sb.append("repository.update(obj);\n");

		sb.append("return obj;\n");
		sb.append("}\n\n");

		sb.append("@Transactional\n");
		sb.append("public void delete(" + createBasicIdParams(clazz) + ") {\n");
		sb.append("if (id == null) {\n");
		sb.append("throw new IllegalArgumentException(MsgConstants.IDENTIFICADOR_NAO_PODE_SER_NULO);\n");
		sb.append("}\n");
		sb.append("repository.delete(this.get" + clazz.getSimpleName() + "ById(id));\n");
		sb.append("}\n\n");

		sb.append("}");
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private static String criarTestDataBuilder(Class clazz) throws NoSuchFieldException, SecurityException { // NOSONAR
		Field[] fields = clazz.getDeclaredFields();
		StringBuilder sb = new StringBuilder();

		sb.append("package com.br.sgad.bl.test.databuilder;\n\n");

		sb.append("import java.util.ArrayList;\n");
		sb.append("import java.util.Date;\n");
		sb.append("import java.util.List;\n");
		sb.append("import de.akquinet.jbosscc.needle.db.testdata.AbstractTestdataBuilder;\n");
		sb.append("\n/**\n");
		sb.append(" * \n");
		sb.append(" * @author ServiceCodeGeneratorSgad\n");
		sb.append(" *\n");
		sb.append(" */\n");
		sb.append("public class " + clazz.getSimpleName() + "TestDataBuilder extends AbstractTestdataBuilder<"
				+ clazz.getSimpleName() + "> {\n\n");

		sb.append("/**\n"); // NOSONAR
		sb.append("* @return\n");
		sb.append("*/\n");
		sb.append("@Override\n");
		sb.append(" public " + clazz.getSimpleName() + " build() {\n");
		sb.append("return new " + clazz.getSimpleName() + "(");
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			if (!fields[i].getName().equals("serialVersionUID")) { // NOSONAR
				sb.append("null");
				if (i != (fields.length - 1)) {
					sb.append(", ");
				}
			}
		}
		sb.append(");\n");
		sb.append("}\n");
		sb.append("\n");

		sb.append("/**\n");
		sb.append("* @return\n");
		sb.append("*/\n");
		sb.append(" public List<" + clazz.getSimpleName() + "> buildList() {\n");
		sb.append("List<" + clazz.getSimpleName() + "> list = new ArrayList<" + clazz.getSimpleName() + ">();\n");
		sb.append("list.add(new " + clazz.getSimpleName() + "(");
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			if (!fields[i].getName().equals("serialVersionUID")) { // NOSONAR
				sb.append("null");
				if (i != (fields.length - 1)) {
					sb.append(", ");
				}
			}
		}
		sb.append("));\n");
		sb.append("return list;\n");
		sb.append("}\n");

		sb.append("}");
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private static String criarBizzTest(Class clazz) throws NoSuchFieldException, SecurityException { // NOSONAR
		Field[] fields = clazz.getDeclaredFields();
		StringBuilder sb = new StringBuilder();

		sb.append("package com.br.sgad.bl.test;\n\n");

		sb.append("import java.util.List;\n");
		sb.append("import javax.inject.Inject;\n");
		sb.append("import org.junit.Assert;\n");
		sb.append("import org.junit.Rule;\n");
		sb.append("import org.junit.Test;\n");
		sb.append("import org.mockito.Mockito;\n");
		sb.append("import com.br.sgad.bl.bizz." + clazz.getSimpleName() + "Bizz;\n");
		sb.append("import com.br.sgad.bl.test.databuilder." + clazz.getSimpleName() + "TestDataBuilder;\n");
		sb.append("import de.akquinet.jbosscc.needle.annotation.InjectIntoMany;\n");
		sb.append("import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;\n");
		sb.append("import de.akquinet.jbosscc.needle.junit.NeedleRule;\n");

		sb.append("public class " + clazz.getSimpleName() + "BizzTest{\n\n");

		sb.append("/**\n");
		sb.append("*\n");
		sb.append("*/\n");
		sb.append("@Rule\n");
		sb.append("public NeedleRule rule = new NeedleRule();\n");

		sb.append("/**\n");
		sb.append("*\n");
		sb.append("*/\n");
		sb.append("@Inject\n");
		sb.append("private " + clazz.getSimpleName() + "Repository repository;\n");

		sb.append("/**\n");
		sb.append("*\n");
		sb.append("*/\n");
		sb.append("@InjectIntoMany\n");
		sb.append("@ObjectUnderTest\n");
		sb.append("private " + clazz.getSimpleName() + "Bizz bizz;\n");

		sb.append("@Mock\n");
		sb.append("private " + clazz.getSimpleName() + " mockObj;\n");

		sb.append("@Before\n");
		sb.append("public void setup(){\n");
		sb.append("MockitoAnnotations.initMocks(this);\n");
		sb.append("}\n\n");

		// TESTE POST NULL
		sb.append("/**\n");
		sb.append("* Cadastro de um Objeto " + clazz.getSimpleName() + " com o valor nulo.\n");
		sb.append("*\n");
		sb.append("* @throws Exception\n"); // NOSONAR
		sb.append("*/\n");
		sb.append("@Test\n"); // NOSONAR
		sb.append("public void save" + clazz.getSimpleName() + "_" + clazz.getSimpleName()
				+ "Null_IllegalArgumentException() {\n"); // NOSONAR
		sb.append(clazz.getSimpleName() + " response = null;\n"); // NOSONAR
		sb.append("try {\n");
		sb.append("response = bizz.save(null);\n"); // NOSONAR
		sb.append("} catch (Exception e) {\n");
		sb.append("Assert.assertTrue(e.getMessage().equals(\"Nenhum parâmetro foi informado.\"));\n");
		sb.append("}\n");
		sb.append("Assert.assertNull(response);\n"); // NOSONAR
		sb.append("}\n");

		// TESTE POST NOT NULL
		sb.append("/**\n");
		sb.append("* Cadastro de um Objeto " + clazz.getSimpleName() + ".\n");
		sb.append("*\n");
		sb.append("* @throws Exception\n");
		sb.append("*/\n");
		sb.append("@Test\n");
		sb.append("public void save" + clazz.getSimpleName() + "_" + clazz.getSimpleName() + "NotNull_" // NOSONAR
				+ clazz.getSimpleName() + "() {\n"); // NOSONAR
		sb.append(clazz.getSimpleName() + " response = null;\n");
		sb.append("try {\n");
		sb.append("Mockito.when(repository.save(mockObj)).thenReturn(new " + clazz.getSimpleName()
				+ "TestDataBuilder().build());"); // NOSONAR
		sb.append("response = bizz.save(mockObj);\n");
		sb.append("} catch (Exception e) {\n");
		sb.append("}\n");
		sb.append("Assert.assertNotNull(response);\n"); // NOSONAR
		sb.append("}\n");

		// TESTE GET BY ID - OK
		sb.append("/**\n");
		sb.append("* Consulta de um Objeto " + clazz.getSimpleName() + " pelo seu id.\n");
		sb.append("*\n");
		sb.append("* @throws Exception\n");
		sb.append("*/\n");
		sb.append("@Test\n");
		sb.append(
				"public void get" + clazz.getSimpleName() + "ById_" + "IdNotNull_" + clazz.getSimpleName() + "() {\n");
		sb.append(clazz.getSimpleName() + " response = null;\n");
		sb.append("try {\n");
		sb.append("Mockito.when(repository.findById(" + clazz.getSimpleName() + ".class, 1L))" + ".thenReturn(new "
				+ clazz.getSimpleName() + "TestDataBuilder().build());\n");
		sb.append("response = bizz.get" + clazz.getSimpleName() + "ById(1L);\n");
		sb.append("} catch (Exception e) {\n");
		sb.append("}\n");
		sb.append("Assert.assertNotNull(response);\n");
		sb.append("}\n");

		// TESTE GET BY ID NULL
		sb.append("/**\n");
		sb.append("* Consulta de um Objeto " + clazz.getSimpleName() + " com id null.\n");
		sb.append("*\n");
		sb.append("* @throws Exception\n");
		sb.append("*/\n");
		sb.append("@Test\n");
		sb.append("public void get" + clazz.getSimpleName() + "ById_" + "IdNull_IllegalArgumentException() {\n");
		sb.append(clazz.getSimpleName() + " response = null;\n");
		sb.append("try {\n");
		sb.append("response = repository.findById(" + clazz.getSimpleName() + ".class, null);\n");
		sb.append("} catch (Exception e) {\n");
		sb.append("Assert.assertTrue(e.getMessage().equals(\"O identificador não pode ser nulo.\"));\n"); // NOSONAR
		sb.append("}\n");
		sb.append("Assert.assertNull(response);\n");
		sb.append("}\n");

		// TESTE GET ALL
		sb.append("/**\n");
		sb.append("* Consulta de uma lista de " + clazz.getSimpleName() + ".\n");
		sb.append("*\n");
		sb.append("* @throws Exception\n");
		sb.append("*/\n");
		sb.append("@Test\n");
		sb.append("public void findAll_TodosParametrosNulos_Lista" + clazz.getSimpleName() + "() {\n");
		sb.append("List<" + clazz.getSimpleName() + "> response = null;\n");
		sb.append("try {\n");
		sb.append("Mockito.when(repository.findByExample(Mockito.any(" + clazz.getSimpleName()
				+ ".class),Mockito.anyInt(),Mockito.anyInt()))");
		sb.append(".thenReturn(new " + clazz.getSimpleName() + "TestDataBuilder().buildList());\n");
		sb.append("response = bizz.getALL(null,null,");
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			if (!fields[i].getName().equals("serialVersionUID") && !fields[i].getName().equals(getIdName(clazz))) { // NOSONAR
				sb.append("null");
				if (i != (fields.length - 1)) {
					sb.append(", ");
				}
			}
		}
		sb.append(");\n");
		sb.append("} catch (Exception e) {\n");
		sb.append("}\n");
		sb.append("Assert.assertNotNull(response);\n");
		sb.append("}\n");

		// TESTE UPDATE OBJ NULL
		sb.append("/**\n");
		sb.append("* Update de um Objeto " + clazz.getSimpleName() + " com o obj nulo.\n"); // NOSONAR
		sb.append("*\n");
		sb.append("* @throws Exception\n");
		sb.append("*/\n");
		sb.append("@Test\n");
		sb.append("public void update" + clazz.getSimpleName() + "_" + clazz.getSimpleName() // NOSONAR
				+ "Null_IllegalArgumentException() {\n");
		sb.append(clazz.getSimpleName() + " response = null;\n");
		sb.append("try {\n");
		sb.append("response = bizz.update(1L, null);\n");
		sb.append("} catch (Exception e) {\n");
		sb.append("Assert.assertTrue(e.getMessage().equals(\"Nenhum parâmetro para atualizar foi informado.\"));\n");
		sb.append("}\n");
		sb.append("Assert.assertNull(response);\n");
		sb.append("}\n");

		// TESTE UPDATE NOT NULL
		sb.append("/**\n");
		sb.append("* Update de um Objeto " + clazz.getSimpleName() + ".\n");
		sb.append("*\n");
		sb.append("* @throws Exception\n");
		sb.append("*/\n");
		sb.append("@Test\n");
		sb.append("public void update" + clazz.getSimpleName() + "_" + clazz.getSimpleName() + "NotNull_"
				+ clazz.getSimpleName() + "() {\n");
		sb.append("boolean control = false;\n");
		sb.append("try {\n");
		sb.append("Mockito.doNothing().when(repository).update(mockObj);\n");
		sb.append("bizz.update(1L, mockObj);\n");
		sb.append("control = true;\n");
		sb.append("} catch (Exception e) {\n");
		sb.append("}\n");
		sb.append("Assert.assertTrue(control);\n");
		sb.append("}\n");

		// TESTE UPDATE ID NULL E OBJ NULL
		sb.append("/**\n");
		sb.append("* Update de um Objeto " + clazz.getSimpleName() + " com o id e obj nulo.\n");
		sb.append("*\n");
		sb.append("* @throws Exception\n");
		sb.append("*/\n");
		sb.append("@Test\n");
		sb.append(
				"public void update" + clazz.getSimpleName() + "_TodosParamentrosNull_IllegalArgumentException() {\n");
		sb.append(clazz.getSimpleName() + " response = null;\n");
		sb.append("try {\n");
		sb.append("response = bizz.update(null, null);\n");
		sb.append("} catch (Exception e) {\n");
		sb.append("Assert.assertTrue(e.getMessage().equals(\"O identificador não pode ser nulo.\"));\n");
		sb.append("}\n");
		sb.append("Assert.assertNull(response);\n");
		sb.append("}\n");

		// TESTE DELETE ID NULL
		sb.append("/**\n");
		sb.append("* Remover de um Objeto " + clazz.getSimpleName() + " com o id nulo.\n");
		sb.append("*\n");
		sb.append("* @throws Exception\n");
		sb.append("*/\n");
		sb.append("@Test\n");
		sb.append("public void delete" + clazz.getSimpleName() + "_IdNull_IllegalArgumentException() {\n");
		sb.append("try {\n");
		sb.append("bizz.delete(null);\n");
		sb.append("} catch (Exception e) {\n");
		sb.append("Assert.assertTrue(e.getMessage().equals(\"O identificador não pode ser nulo.\"));\n");
		sb.append("}\n");
		sb.append("}\n");

		// TESTE DELETE ID NOT NULL
		sb.append("/**\n");
		sb.append("* Remover de um Objeto " + clazz.getSimpleName() + ".\n");
		sb.append("*\n");
		sb.append("* @throws Exception\n");
		sb.append("*/\n");
		sb.append("@Test\n");
		sb.append("public void delete" + clazz.getSimpleName() + "_IdNotNull_Vazio() {\n");
		sb.append("try {\n");
		sb.append("Mockito.doNothing().when(repository).delete(mockObj);\n");
		sb.append("bizz.delete(1L);\n");
		sb.append("} catch (Exception e) {\n");
		sb.append("}\n");
		sb.append("}\n");

		// TESTE UPDATE PARCIAL OBJ NULL
		sb.append("/**\n");
		sb.append("* Update parcial de um Objeto " + clazz.getSimpleName() + " com o obj nulo.\n");
		sb.append("*\n");
		sb.append("* @throws Exception\n");
		sb.append("*/\n");
		sb.append("@Test\n");
		sb.append("public void partialUpdate" + clazz.getSimpleName() + "_" + clazz.getSimpleName() // NOSONAR
				+ "Null_IllegalArgumentException() {\n");
		sb.append(clazz.getSimpleName() + " response = null;\n");
		sb.append("try {\n");
		sb.append("response = bizz.partialUpdate(1L, null);\n");
		sb.append("} catch (Exception e) {\n");
		sb.append("Assert.assertTrue(e.getMessage().equals(\"Nenhum parâmetro para atualizar foi informado.\"));\n");
		sb.append("}\n");
		sb.append("Assert.assertNull(response);\n");
		sb.append("}\n");

		// TESTE UPDATE PARCIAL NOT NULL
		sb.append("/**\n");
		sb.append("* Update Parcial de um Objeto " + clazz.getSimpleName() + ".\n");
		sb.append("*\n");
		sb.append("* @throws Exception\n");
		sb.append("*/\n");
		sb.append("@Test\n");
		sb.append("public void partialUpdate" + clazz.getSimpleName() + "_" + clazz.getSimpleName() + "NotNull_"
				+ clazz.getSimpleName() + "() {\n");
		sb.append(clazz.getSimpleName() + " response = null;\n");
		sb.append("try {\n");
		sb.append("Mockito.when(repository.partialUpdate(mockObj, 1L)).thenReturn(new " + clazz.getSimpleName()
				+ "TestDataBuilder().build());\n");
		sb.append("response = bizz.partialUpdate(1L, mockObj);\n");
		sb.append("} catch (Exception e) {\n");
		sb.append("}\n");
		sb.append("Assert.assertNotNull(response);\n");
		sb.append("}\n");

		// TESTE UPDATE PARCIAL ID NULL E OBJ NULL
		sb.append("/**\n");
		sb.append("* Update Parcial de um Objeto " + clazz.getSimpleName() + " com o id e obj nulo.\n");
		sb.append("*\n");
		sb.append("* @throws Exception\n");
		sb.append("*/\n");
		sb.append("@Test\n");
		sb.append("public void partialUpdate" + clazz.getSimpleName()
				+ "_TodosParamentrosNull_IllegalArgumentException() {\n");
		sb.append(clazz.getSimpleName() + " response = null;\n");
		sb.append("try {\n");
		sb.append("response = bizz.partialUpdate(null, null);\n");
		sb.append("} catch (Exception e) {\n");
		sb.append("Assert.assertTrue(e.getMessage().equals(\"O identificador não pode ser nulo.\"));\n");
		sb.append("}\n");
		sb.append("Assert.assertNull(response);\n");
		sb.append("}\n");
		sb.append("}");
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private static void criarArquivoBizz(Class clazz, String fileContent) throws Exception { // NOSONAR
		try {
			File file = new File(DIRETORIO_ARQUIVO_BUSINESS_API + "bizz/" + clazz.getSimpleName() + "Bizz.java");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(fileContent);
			bw.close();
		} catch (Exception e) {
			throw e;
		}
	}

	@SuppressWarnings("rawtypes")
	private static void criarArquivoTestDataBuilder(Class clazz, String fileContent) throws Exception { // NOSONAR
		try {
			File file = new File(
					DIRETORIO_ARQUIVO_BUSINESS_TEST_DATABUILDER + clazz.getSimpleName() + "TestDataBuilder.java");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(fileContent);
			bw.close();
		} catch (Exception e) {
			throw e;
		}
	}

	@SuppressWarnings("rawtypes")
	private static void criarArquivoBizzTest(Class clazz, String fileContent) throws Exception { // NOSONAR
		try {
			File file = new File(DIRETORIO_ARQUIVO_BUSINESS_TEST + clazz.getSimpleName() + "BizzTest.java");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(fileContent);
			bw.close();
		} catch (Exception e) {
			LOGGER.error(ExceptionUtils.getRootCause(e));
			throw e;
		}
	}

	@SuppressWarnings("rawtypes")
	private static String criarImportsBizzService(Class clazz) {
		try {
			StringBuilder imports = new StringBuilder();
			imports.append("import javax.enterprise.context.RequestScoped;" + "import javax.inject.Inject;"
					+ "import javax.ws.rs.Consumes;" + "import javax.ws.rs.DELETE;" + "import javax.ws.rs.DefaultValue;"
					+ "import javax.ws.rs.GET;" + "import javax.ws.rs.POST;" + "import javax.ws.rs.PUT;"
					+ "import javax.ws.rs.Path;" + "import javax.ws.rs.PathParam;" + "import javax.ws.rs.Produces;"
					+ "import javax.ws.rs.QueryParam;" + "import javax.ws.rs.core.MediaType;"
					+ "import com.br.sgad.bl.bizz." + clazz.getSimpleName() + "Bizz;"
					+ "import com.fasterxml.jackson.annotation.JsonInclude;"
					+ "import com.fasterxml.jackson.annotation.JsonInclude.Include;"
					+ "import com.br.tricard.api.response.ResponseEntity;"
					+ "import com.br.tricard.api.response.ResponseUtils;" + "import io.swagger.annotations.Api;"
					+ "import io.swagger.annotations.ApiOperation;" + "import io.swagger.annotations.ApiParam;");
			return imports.toString();
		} catch (Exception e) {
			LOGGER.error(ExceptionUtils.getRootCause(e));
			throw e;
		}
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static String getIdName(Class clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		String idName = null;
		for (Method method : methods) {
			if (method.getAnnotationsByType(Id.class).length == 1
					|| method.getAnnotationsByType(EmbeddedId.class).length == 1) {
				String methodName = method.getName().replace("get", "");
				String firstChar = methodName.substring(0, 1).toLowerCase();
				methodName = firstChar + methodName.substring(1);
				idName = methodName;
			}
		}
		return idName;
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	@SuppressWarnings("rawtypes")
	private static String createPath(Class clazz) throws NoSuchFieldException, SecurityException {// NOSONAR
		StringBuilder sb = new StringBuilder();
		String idName = getIdName(clazz);
		if (idName.endsWith("id")) {
			Field field = clazz.getDeclaredField("id");
			field.setAccessible(true);
			Field[] idFields = field.getType().getDeclaredFields();
			sb.append("@Path(\"");
			for (Field f : idFields) {
				if (!f.getName().equals("serialVersionUID")) { // NOSONAR
					sb.append("/{" + f.getName() + "}");
				}
			}
			sb.append("\")\n");
		} else {
			sb.append("@Path(\"/{id}\")\n");
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	@SuppressWarnings("rawtypes")
	private static String createIdParams(Class clazz) throws NoSuchFieldException, SecurityException {// NOSONAR
		StringBuilder sb = new StringBuilder();
		String idName = getIdName(clazz);
		if (idName.endsWith("id")) {
			Field field = clazz.getDeclaredField("id");
			field.setAccessible(true);
			Field[] idFields = field.getType().getDeclaredFields();
			for (int i = 0; i < idFields.length; i++) {
				if (!idFields[i].getName().equals("serialVersionUID")) { // NOSONAR
					sb.append(
							"@ApiParam(value = \".\", required = true) @PathParam(\"" + idFields[i].getName() + "\")"
									+ (idFields[i].getType().getSimpleName().equals("Date") ? "CustomDateParam" // NOSONAR
											: idFields[i].getType().getSimpleName().equals("Character") ? "String" // NOSONAR
													: idFields[i].getType().getSimpleName())
									+ " " + idFields[i].getName());
					if (i != (idFields.length - 1)) {// NOSONAR
						sb.append(",\n");
					}
				}
			}
		} else {
			sb.append("@ApiParam(value = \"Identificador de uma entidade " + clazz.getSimpleName()
					+ ".\", required = true) @PathParam(\"id\")"
					+ clazz.getDeclaredField(idName).getType().getSimpleName() + " id");
		}
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private static String createBasicIdParams(Class clazz) throws NoSuchFieldException, SecurityException { // NOSONAR
		StringBuilder sb = new StringBuilder();
		String idName = getIdName(clazz);
		if (idName.endsWith("id")) {
			Field field = clazz.getDeclaredField("id");
			field.setAccessible(true);
			Field[] idFields = field.getType().getDeclaredFields();
			for (int i = 0; i < idFields.length; i++) {
				if (!idFields[i].getName().equals("serialVersionUID")) { // NOSONAR
					sb.append((idFields[i].getType().getSimpleName().equals("Date") ? "CustomDateParam" // NOSONAR
							: idFields[i].getType().getSimpleName().equals("Character") ? "String" // NOSONAR
									: idFields[i].getType().getSimpleName())
							+ " " + idFields[i].getName());
					if (i != (idFields.length - 1)) {// NOSONAR
						sb.append(",\n");
					}
				}
			}
		} else {
			sb.append(clazz.getDeclaredField(idName).getType().getSimpleName() + " id");
		}
		return sb.toString();
	}
}
