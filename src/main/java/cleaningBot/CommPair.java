package cleaningBot;

import io.grpc.ManagedChannel;
import services.grpc.BotServicesGrpc;

public class CommPair {
    private ManagedChannel managedChannel;
    private BotServicesGrpc.BotServicesStub communicationStub;

    public CommPair(ManagedChannel managedChannel, BotServicesGrpc.BotServicesStub communicationStub) {
        this.managedChannel = managedChannel;
        this.communicationStub = communicationStub;
    }

    public synchronized ManagedChannel getManagedChannel() {
        return managedChannel;
    }

    public synchronized void setManagedChannel(ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    public synchronized BotServicesGrpc.BotServicesStub getCommunicationStub() {
        return communicationStub;
    }

    public synchronized void setCommunicationStub(BotServicesGrpc.BotServicesStub communicationStub) {
        this.communicationStub = communicationStub;
    }

    @Override
    public String toString() {
        return "CommPair{" +
                "managedChannel=" + managedChannel +
                ", communicationStub=" + communicationStub +
                '}';
    }
}
