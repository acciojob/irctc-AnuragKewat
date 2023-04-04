package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        List<Ticket> tickets = train.getBookedTickets();
        if(bookTicketEntryDto.getNoOfSeats() > train.getNoOfSeats()-tickets.size())
            throw new RuntimeException("Less tickets are available");

        if(!train.getRoute().contains(bookTicketEntryDto.getFromStation().toString()) || !train.getRoute().contains(bookTicketEntryDto.getToStation().toString()))
            throw new RuntimeException("Invalid stations");

        String route = train.getRoute();
        String[] str = route.split("_");
        int from=0, to=0;
        for(int i=0; i<str.length; i++) {
            if(str[i].equals(bookTicketEntryDto.getFromStation())) from = i;
            else if(str[i].equals(bookTicketEntryDto.getToStation())) to = i;
        }

        int totalFare = 300 * (to-from);
        Ticket ticket = new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTrain(train);
        ticket.setPassengersList(new ArrayList<>());
        ticket.setTotalFare(totalFare);

        List<Passenger> passengers = new ArrayList<>();
        for(int id: bookTicketEntryDto.getPassengerIds()) {
            Passenger passenger = passengerRepository.findById(id).get();
            List<Ticket> tickets1 = new ArrayList<>();
            tickets1.add(ticket);
            passenger.setBookedTickets(tickets1);
            passengers.add(passenger);
        }

        ticket.setPassengersList(passengers);
        Ticket ticket1 = ticketRepository.save(ticket);

       return ticket1.getTicketId();

    }
}
