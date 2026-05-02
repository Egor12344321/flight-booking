package com.tbank.grpc.service;


import com.tbank.grpc.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class FlightServiceImpl extends FlightServiceGrpc.FlightServiceImplBase {
    @Override
    public void searchFlights(SearchRequest request, StreamObserver<SearchResponse> responseObserver) {
        super.searchFlights(request, responseObserver);
    }

    @Override
    public void getFlight(GetFlightRequest request, StreamObserver<FlightData> responseObserver) {
        super.getFlight(request, responseObserver);
    }

    @Override
    public void reserveSeats(ReserveRequest request, StreamObserver<ReserveResponse> responseObserver) {
        super.reserveSeats(request, responseObserver);
    }

    @Override
    public void releaseReservation(ReleaseRequest request, StreamObserver<ReleaseResponse> responseObserver) {
        super.releaseReservation(request, responseObserver);
    }
}
