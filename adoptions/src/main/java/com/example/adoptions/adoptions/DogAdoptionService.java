package com.example.adoptions.adoptions;

import com.example.adoptions.adoptions.grpc.AdoptionsGrpc;
import com.example.adoptions.adoptions.grpc.Dogs;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Service
class DogAdoptionGrpcService
        extends AdoptionsGrpc.AdoptionsImplBase {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionGrpcService(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @Override
    public void all(Empty request, StreamObserver<Dogs> responseObserver) {

        var dogs = this.dogAdoptionService.dogs()
                .stream()
                .map( ourDog ->
                        com.example.adoptions.adoptions.grpc.Dog.newBuilder()
                                .setName(ourDog.name())
                                .setId(ourDog.id())
                                .setDescription(ourDog.description())
                                .build())
                .toList() ;
        responseObserver.onNext(Dogs.newBuilder().addAllDogs(dogs).build());
        responseObserver.onCompleted();

    }
}


@Controller
class DogAdoptionGraphqlController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionGraphqlController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @QueryMapping
    Collection<Dog> dogs() {
        return this.dogAdoptionService.dogs();
    }
}


@Controller
@ResponseBody
class DogAdoptionMvcController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionMvcController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @GetMapping("/dogs")
    Collection<Dog> dogs() {
        return this.dogAdoptionService.dogs();
    }

    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        this.dogAdoptionService.adopt(dogId, owner);
    }
}

@Service
@Transactional
class DogAdoptionService {

    private final DogRepository dogRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    DogAdoptionService(DogRepository dogRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.dogRepository = dogRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    Collection<Dog> dogs() {
        return this.dogRepository.findAll();
    }

    void adopt(int dogId, String owner) {
        dogRepository.findById(dogId).ifPresent(dog -> {
            var updated = dogRepository.save(new Dog(dog.id(), dog.name(), owner, dog.description()));
            System.out.println("adopted dog: " + updated + "!");
            applicationEventPublisher.publishEvent(new DogAdoptionEvent(dogId));
        });
    }
}


interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String owner, String description) {
}