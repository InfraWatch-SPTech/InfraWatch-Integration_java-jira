package s3.conector;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

public class S3Provider {
    private static final String ACCESS_KEY = ;
    private static final String SECRET_KEY =
    private static final String SESSION_TOKEN = ;
    private static final Region REGION = Region.US_EAST_1;

    public static S3Client criarClienteS3(){
        AwsCredentials credentials = AwsSessionCredentials.create(ACCESS_KEY, SECRET_KEY, SESSION_TOKEN);

        return  S3Client.builder().region(REGION).credentialsProvider(StaticCredentialsProvider.create(credentials)).build();


    }
}