package org.example.messageservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
@RequiredArgsConstructor
public class GrpcClientConfig {

    private final GrpcChannelFactory channels;

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userGrpcStub() {
        return UserServiceGrpc.newBlockingStub(
                channels.createChannel("user-service"));
    }
}