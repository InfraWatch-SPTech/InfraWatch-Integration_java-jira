package s3.conector;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import Dados.DadosHardwareModelo;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.Comparator;
import java.util.Optional;

public class S3ReaderService {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    public S3ReaderService(S3Client s3Client) {
        this.s3Client = s3Client;
        this.objectMapper = new ObjectMapper();
        
        // Ignora propriedades desconhecidas do JSON sem falhar o parse
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public DadosHardwareModelo lerUltimoArquivoJson(String bucketName, String empresa, String equipamento) {
        String prefixo = String.format("gold/%s/%s/", empresa, equipamento);

        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefixo)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listReq);

        Optional<S3Object> ultimoObjeto = listResponse.contents().stream()
                .filter(objeto -> objeto.key().toLowerCase().endsWith(".json"))
                .max(Comparator.comparing(S3Object::lastModified));

        if (ultimoObjeto.isEmpty()) {
            System.err.println("Nenhum arquivo JSON encontrado no caminho: " + prefixo);
            return null;
        }

        String keyMaisRecente = ultimoObjeto.get().key();
        System.out.println("Último arquivo selecionado no S3: " + keyMaisRecente);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyMaisRecente)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3InputStream = s3Client.getObject(getObjectRequest)) {
            
            DadosHardwareModelo dados = objectMapper.readValue(s3InputStream, DadosHardwareModelo.class);
            System.out.println("=== Sucesso ao ler o arquivo: " + keyMaisRecente + " ===");
            return dados;

        } catch (Exception e) {
            System.err.println("Erro ao ler ou converter o arquivo '" + keyMaisRecente + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
