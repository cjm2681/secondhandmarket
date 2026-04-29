package com.secondHand.SecondHandMarket.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    // @Value: application.yaml의 값을 필드에 주입
    // 실제 키는 환경변수(AWS_ACCESS_KEY, AWS_SECRET_KEY)로 관리 → 코드에 노출 방지
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;   // 버킷이 위치한 리전 (예: ap-northeast-2 = 서울)

    @Bean
    public S3Client s3Client() {

        // AwsBasicCredentials: Access Key + Secret Key 조합 인증
        // StaticCredentialsProvider: 고정 자격증명 제공자 (EC2 IAM 역할 등 동적 방식과 구분)
        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                // 명시적 자격증명 설정 (기본값은 환경변수/~/.aws/credentials 순으로 탐색하지만
                //  명시하면 탐색 과정 없이 바로 사용 → 예측 가능한 동작 보장)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}