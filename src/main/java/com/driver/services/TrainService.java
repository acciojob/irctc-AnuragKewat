package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        List<Station> stations = trainEntryDto.getStationRoute();
        StringBuilder sb = new StringBuilder();
        for(Station station: stations) {
            sb.append(station.toString());
            sb.append("_");
        }
        Train train = new Train();
        train.setRoute(sb.toString());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train = trainRepository.save(train);
        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        int noOfSeats = train.getNoOfSeats();
        List<Ticket> bookedTickets = train.getBookedTickets();
        String route = train.getRoute();

        String toStation = seatAvailabilityEntryDto.getToStation().toString();
        String fromStation = seatAvailabilityEntryDto.getFromStation().toString();

        int count=0;
        if(route.indexOf(toStation) > route.indexOf(fromStation)) {
            for(Ticket ticket: bookedTickets) {
                String[] str = route.split("_");
                int from=0, to=0;
                int fromTicket=0, toTicket=0;
                for(int i=0; i<str.length; i++) {
                    if(str[i].equals(fromStation)) from = i;
                    else if(str[i].equals(toStation)) to = i;
                    if(str[i].equals(ticket.getFromStation().toString())) fromTicket = i;
                    else if(str[i].equals(ticket.getToStation().toString())) toTicket = i;
                }
                if(to<=fromTicket && from>=toTicket) count++;
            }
        }

       return train.getNoOfSeats() - count;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train = trainRepository.findById(trainId).get();

        if(!train.getRoute().contains(station.toString())) throw new RuntimeException("Train is not passing from this station");

        List<Ticket> tickets = train.getBookedTickets();

        int count=0;
        for(Ticket ticket: tickets) {
            if(ticket.getFromStation().equals(station) && train.getRoute().contains(ticket.getToStation().toString())) count+=ticket.getPassengersList().size();
        }

        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Train train = trainRepository.findById(trainId).get();
        List<Ticket> tickets = train.getBookedTickets();

        int oldestAge = 0;
        for(Ticket ticket: tickets) {
            List<Passenger> passengers = ticket.getPassengersList();
            for(Passenger passenger: passengers) {
                oldestAge = Math.max(oldestAge, passenger.getAge());
            }
        }

        return oldestAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> TrainList = new ArrayList<>();
        List<Train> trains = trainRepository.findAll();
        for(Train t:trains){
            String s = t.getRoute();
            String[] ans = s.split("_");
            for(int i=0;i<ans.length;i++){
                if(ans[i].equals(station.toString())){
                    int startTimeInMin = (startTime.getHour() * 60) + startTime.getMinute();
                    int lastTimeInMin = (endTime.getHour() * 60) + endTime.getMinute();


                    int departureTimeInMin = (t.getDepartureTime().getHour() * 60) + t.getDepartureTime().getMinute();
                    int reachingTimeInMin  = departureTimeInMin + (i * 60);
                    if(reachingTimeInMin>=startTimeInMin && reachingTimeInMin<=lastTimeInMin)
                        TrainList.add(t.getTrainId());
                }
            }
        }
        return TrainList;
    }

}
