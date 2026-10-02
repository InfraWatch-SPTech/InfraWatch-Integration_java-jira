package s3.conector;
import Dados.DadosHardwareModelo;
import school.sptech.AlertaService;
import school.sptech.config.Jira;
import software.amazon.awssdk.services.s3.S3Client;

public class Main {

    public static void main(String[] args) {
        S3Client s3Client = S3Provider.criarClienteS3();
        S3ReaderService readerService = new S3ReaderService(s3Client);

        // nome do bucket
        String nomeBucket = ;
        // empresa que quer os logs
        String empresa = "XPTO Brasil";
        // qual servidor / equipamengo ir atras
        String equipamento = "4";

        DadosHardwareModelo dados = readerService.lerUltimoArquivoJson(nomeBucket, empresa, equipamento);

        if (dados!= null){
            // url do jira
            String baseUrl = ;
            // email do jira
            String email = ;
            // adicione seu token api
            String apiToken = ;

            Jira jira = new Jira(baseUrl, email, apiToken);
            AlertaService alertaService = new AlertaService(jira);


            // buscar o id equipamento no mysql e checar os limites

            alertaService.verificarEEnviarAlertas(dados);


        } else {
            System.err.println("não foi possivel carregar os dados do s3");
        }
        s3Client.close();
    }
}
