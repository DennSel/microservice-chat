package org.example.userservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.grpc.GetUserRequest;
import org.example.grpc.UserResponse;
import org.example.grpc.UserServiceGrpc;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;

    @Override
    public void getUser(GetUserRequest req, StreamObserver<UserResponse> obs) {

        User user = userRepository
                .findById(req.getUserId())
                .orElseThrow();

        obs.onNext(UserResponse.newBuilder()
                .setUserId(user.getUserId())
                .setDisplayName(user.getDisplayName())
                .build());
        obs.onCompleted();
    }
}