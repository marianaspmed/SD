package pt.tecnico.bicloin.hub;

import java.util.ArrayList;
import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.tecnico.bicloin.hub.Exceptions.*;
import pt.tecnico.bicloin.hub.domain.HubNetwork;
import pt.tecnico.bicloin.hub.domain.HubUserNetwork;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.InitRecRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.InitRecResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetLatRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetLatResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SetLonRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetLonResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.GetLatRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.GetLatResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.GetLonRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.GetLonResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.GetDistanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.GetDistanceResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.PingRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.PingResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SetBalanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetBalanceResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SetHasBikeRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetHasBikeResponse;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc.HubServiceImplBase;

public class HubServiceImpl extends HubServiceImplBase {

	private HubNetwork network = new HubNetwork();
	private HubUserNetwork userNetwork = new HubUserNetwork();
    
	public HubServiceImpl(HubNetwork hubNetwork, HubUserNetwork userNetwork) {
		this.network = hubNetwork;
		this.userNetwork = userNetwork;
	}

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        String output = "UP";
        PingResponse response = PingResponse.newBuilder().
        setOutput(output).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void sysStatus(SysStatusRequest request, StreamObserver<SysStatusResponse> responseObserver) {
        SysStatusResponse response = SysStatusResponse.newBuilder().addAllOutput(network.systemStatus()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void infoStation(InfoStationRequest request, StreamObserver<InfoStationResponse> responseObserver) {
        
        InfoStationResponse response;
		try {
			response = InfoStationResponse.newBuilder().setName(network.getStationById(request.getAbrv()).getName()).
			setLatitude(network.getStationById(request.getAbrv()).getLat()).
			setLongitude(network.getStationById(request.getAbrv()).getLon()).
			setDocksCapacity(network.getStationById(request.getAbrv()).getDocks()).
			setAvailableBikes(network.getStationById(request.getAbrv()).getnBikes()).
			setAward(network.getStationById(request.getAbrv()).getAward()).
			setBikeUp(network.getStationById(request.getAbrv()).getBikesUp()).
			setBikeDown(network.getStationById(request.getAbrv()).getBikesDown()).
			build();
		} catch (NoSuchStationException e) {
			response = InfoStationResponse.newBuilder().setName("").
			setLatitude(0).
			setLongitude(0).
			setDocksCapacity(0).
			setAvailableBikes(0).
			setAward(0).
			setBikeUp(0).
			setBikeDown(0).
			build();
		}

        // Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();

    }

    @Override
    public void locateStation(LocateStationRequest request, StreamObserver<LocateStationResponse> responseObserver) {
    	
    	List<String> list = new ArrayList<String>();
    	list = network.locateStation(request.getLatitude(), request.getLongitude(), request.getK());
    	
    	LocateStationResponse response = LocateStationResponse.newBuilder().addAllStationsIds(list).
    	build();
    	
    	// Send a single response through the stream.
    	responseObserver.onNext(response);
    	// Notify the client that the operation has been completed.
    	responseObserver.onCompleted();
    }

    @Override
	public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
			// tratar excecao user sem conta

			BalanceResponse response = BalanceResponse.newBuilder().setBalance(userNetwork.getUserByUsername(request.getUsername()).getBalance()).
			build();
			
	        // Send a single response through the stream.
			responseObserver.onNext(response);
			// Notify the client that the operation has been completed.
			responseObserver.onCompleted();
	        
	}

	@Override
	public void setLat(SetLatRequest request, StreamObserver<SetLatResponse> responseObserver) {

		SetLatResponse response = SetLatResponse.newBuilder().setStatus(userNetwork.getUserByUsername(request.getUsername()).setLat(request.getLat())).
		build();

		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();

	}

	@Override
	public void setBalance(SetBalanceRequest request, StreamObserver<SetBalanceResponse> responseObserver) {

		userNetwork.getUserByUsername(request.getUsername()).setBalance(0);

		SetBalanceResponse response = SetBalanceResponse.newBuilder().setValue(userNetwork.getUserByUsername(request.getUsername()).getBalance()).build();

		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();

	}

	@Override
	public void setHasBike(SetHasBikeRequest request, StreamObserver<SetHasBikeResponse> responseObserver) {
		userNetwork.getUserByUsername(request.getUsername()).setHasBike(false);
		SetHasBikeResponse response = SetHasBikeResponse.newBuilder().build();

		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();

	}

	@Override
	public void setLon(SetLonRequest request, StreamObserver<SetLonResponse> responseObserver) {

		SetLonResponse response = SetLonResponse.newBuilder().setStatus(userNetwork.getUserByUsername(request.getUsername()).setLon(request.getLon())).
		build();

		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();

	}

	@Override
	public void getLat(GetLatRequest request, StreamObserver<GetLatResponse> responseObserver) {

		GetLatResponse response = GetLatResponse.newBuilder().setLat(userNetwork.getUserByUsername(request.getUsername()).getLat()).
		build();

		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();

	}

	@Override
	public void getLon(GetLonRequest request, StreamObserver<GetLonResponse> responseObserver) {

		GetLonResponse response = GetLonResponse.newBuilder().setLon(userNetwork.getUserByUsername(request.getUsername()).getLon()).
		build();

		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();

	}

	@Override
	public void getDistance(GetDistanceRequest request, StreamObserver<GetDistanceResponse> responseObserver) {
		GetDistanceResponse response;
		try{
		response = GetDistanceResponse.newBuilder().setDistance(network.getStationById(request.getAbrv()).getDistance(request.getUserLat(), request.getUserLon())).
				build();
		} catch (NoSuchStationException e) {
			response = GetDistanceResponse.newBuilder().setDistance(0).
					build();
		}
		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();

	}

    @Override
	public void topUp(TopUpRequest request, StreamObserver<TopUpResponse> responseObserver) {

			TopUpResponse response;
			try {
				response = TopUpResponse.newBuilder().setBalance(userNetwork.getUserByUsername(request.getUsername()).topUpBalance(request.getAmount())).
						build();
			} catch (InvalidDepositValueException e) {
				response = TopUpResponse.newBuilder().setBalance(userNetwork.getUserByUsername(request.getUsername()).getBalance()).build();
			}
	        // Send a single response through the stream.
			responseObserver.onNext(response);
			// Notify the client that the operation has been completed.
			responseObserver.onCompleted();
	}

    @Override
	public void bikeUp(BikeUpRequest request, StreamObserver<BikeUpResponse> responseObserver) {

		BikeUpResponse response;
		try {
			response = BikeUpResponse.newBuilder().setStatus(network.getStationById(request.getAbrv()).bike_up((float) request.getLatitude(),
					(float) request.getLongitude(), userNetwork.getUserByUsername(request.getUsername()))).
					build();
		} catch (NoSuchStationException e) {
			System.err.println(e.getMessage());
			response = BikeUpResponse.newBuilder().setStatus(e.getMessage()).
					build();

		} catch (TooFarException e) {
			System.err.println(e.getMessage());
			response = BikeUpResponse.newBuilder().setStatus(e.getMessage()).
					build();

		} catch (UserHasBikeException e) {
			System.err.println(e.getMessage());
			response = BikeUpResponse.newBuilder().setStatus(e.getMessage()).
					build();
		} catch (NotEnoughBalanceException e) {
			System.err.println(e.getMessage());
			response = BikeUpResponse.newBuilder().setStatus(e.getMessage()).
					build();
		}

		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();
	}

    @Override	
	public void bikeDown(BikeDownRequest request, StreamObserver<BikeDownResponse> responseObserver) {
		
		BikeDownResponse response;
		try {
			response = BikeDownResponse.newBuilder().setStatus(network.getStationById(request.getAbrv()).bike_down((float) request.getLatitude(), (float) request.getLongitude(), userNetwork.getUserByUsername(request.getUsername()))).
			build();
		} catch (NoSuchStationException e) {
			System.err.println(e.getMessage());
			response = BikeDownResponse.newBuilder().setStatus(e.getMessage()).
			build();
		} catch (FullStationException e) {
			System.err.println(e.getMessage());
			response = BikeDownResponse.newBuilder().setStatus(e.getMessage()).
		    build();
		} catch (NoBikeException e) {
			System.err.println(e.getMessage());
			response = BikeDownResponse.newBuilder().setStatus(e.getMessage()).
			build();
		}

        // Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();
	}

	@Override	
	public void initRec(InitRecRequest request, StreamObserver<InitRecResponse> responseObserver) {
		
		InitRecResponse response = InitRecResponse.newBuilder().build();
		network.initRec();

        // Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();
	}
}