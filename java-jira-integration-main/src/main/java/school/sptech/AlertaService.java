package school.sptech;

import Dados.DadosHardwareModelo;
import mysqlConector.Conexao;
import org.springframework.jdbc.core.JdbcTemplate;
import school.sptech.config.Jira;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AlertaService {

    private final JdbcTemplate template;
    private final Jira jira;

    public AlertaService(Jira jira) {
        Conexao conexao = new Conexao();
        this.template = new JdbcTemplate(conexao.getConexao());
        this.jira = jira;
    }

    /**
     * Busca o ID do equipamento no MySQL usando o ID direto ou o nome do equipamento.
     */
    public Integer buscarIdEquipamento(String codigoEquipamento) {
        if (codigoEquipamento == null || codigoEquipamento.trim().isEmpty()) {
            System.err.println("Código ou ID do equipamento veio nulo/vazio do S3.");
            return null;
        }

        try {
            // 1. Tenta buscar pelo ID numérico diretamente (ex: "1" -> 1)
            try {
                Integer id = Integer.parseInt(codigoEquipamento.trim());
                String sql = "SELECT idEquipamento FROM equipamento WHERE idEquipamento = ?;";
                return template.queryForObject(sql, Integer.class, id);
            } catch (NumberFormatException e) {
                // 2. Se não for número, busca pelo campo 'nome' na tabela 'equipamento'
                String sql = "SELECT idEquipamento FROM equipamento WHERE nome = ?;";
                return template.queryForObject(sql, Integer.class, codigoEquipamento.trim());
            }
        } catch (Exception e) {
            System.err.println("Erro SQL ao buscar equipamento '" + codigoEquipamento + "' no MySQL: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica os componentes configurados e ativos para o equipamento no banco.
     */
    public void verificarEEnviarAlertas(DadosHardwareModelo dados) {
        if (dados == null) {
            System.err.println("Objeto DadosHardwareModelo está nulo.");
            return;
        }

        Integer idEquipamento = buscarIdEquipamento(dados.getEquipamento());

        if (idEquipamento == null) {
            System.err.println("Processamento interrompido: equipamento não encontrado no banco MySQL.");
            return;
        }

        System.out.println("Equipamento encontrado no MySQL com ID: " + idEquipamento);

        // Busca no banco todos os componentes e limites ativos para este equipamento
        String sql = """
            SELECT p.limite_atencao, p.limite_critico, c.nome AS nome_componente, c.tipo AS tipo_componente
            FROM parametro_alerta p
            JOIN componente c ON p.fkComponente = c.idComponente
            WHERE p.fkEquipamento = ? AND p.ativo = 1;
        """;

        List<Map<String, Object>> componentesMonitorados = template.queryForList(sql, idEquipamento);

        if (componentesMonitorados.isEmpty()) {
            System.out.println("Nenhum parâmetro de alerta ativo cadastrado para o equipamento ID: " + idEquipamento);
            return;
        }

        // Percorre apenas os componentes cadastrados para este equipamento
        for (Map<String, Object> comp : componentesMonitorados) {
            String nomeComp = (String) comp.get("nome_componente");
            String tipoComp = (comp.get("tipo_componente") != null) ? comp.get("tipo_componente").toString().toUpperCase() : "";
            Double limiteAtencao = ((Number) comp.get("limite_atencao")).doubleValue();
            Double limiteCritico = ((Number) comp.get("limite_critico")).doubleValue();

            // Identifica qual valor do JSON corresponde a este componente
            Double valorMedio = extrairValorMetrica(dados, tipoComp, nomeComp);

            if (valorMedio != null) {
                avaliarEEnviarAlerta(nomeComp, valorMedio, limiteAtencao, limiteCritico);
            } else {
                System.out.println("Métrica não encontrada no JSON para o componente: " + nomeComp);
            }
        }
    }

    /**
     * Mapeia o tipo/nome do componente vindo do banco para a métrica do JSON
     */
    private Double extrairValorMetrica(DadosHardwareModelo dados, String tipoComp, String nomeComp) {
        String identificador = (tipoComp + " " + nomeComp).toUpperCase();

        if (identificador.contains("CPU") || identificador.contains("PROCESSADOR")) {
            return dados.getCpuMedia();
        } else if (identificador.contains("RAM") || identificador.contains("MEMORIA")) {
            return dados.getRamMedia();
        } else if (identificador.contains("DISCO") || identificador.contains("ARMAZENAMENTO")) {
            return dados.getDiscoMedia();
        } else if (identificador.contains("TEMPERATURA")) {
            return dados.getTemperaturaMedia();
        } else if (identificador.contains("REDE")) {
            return dados.getRedeMedia();
        }

        return null;
    }

    /**
     * Compara o valor lido com os limites e dispara a issue no Jira
     */
    private void avaliarEEnviarAlerta(String nomeComponente, Double valorMedio, Double limiteAtencao, Double limiteCritico) {
        try {
            String mensagemCritica = String.format("ALERTA CRÍTICO: O componente %s alcançou %.2f%%!", nomeComponente, valorMedio);
            String mensagemAtencao = String.format("ALERTA DE ATENÇÃO: O componente %s alcançou %.2f%%!", nomeComponente, valorMedio);

            if (valorMedio >= limiteCritico) {
                System.out.println("Disparando Alerta Crítico no Jira para " + nomeComponente);
                String response = jira.createIssue("KAN", mensagemCritica, "Task");
                System.out.println("Issue criada no Jira: " + response);

            } else if (valorMedio >= limiteAtencao) {
                System.out.println("Disparando Alerta de Atenção no Jira para " + nomeComponente);
                String response = jira.createIssue("KAN", mensagemAtencao, "Task");
                System.out.println("Issue criada no Jira: " + response);

            } else {
                System.out.println("Componente " + nomeComponente + " (Média: " + valorMedio + "%) dentro dos limites.");
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao comunicar com a API do Jira: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro ao processar alerta para " + nomeComponente + ": " + e.getMessage());
        }
    }
}
