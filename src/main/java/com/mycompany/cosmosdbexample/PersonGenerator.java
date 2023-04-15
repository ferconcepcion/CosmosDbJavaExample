package com.mycompany.cosmosdbexample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PersonGenerator {
    
    private final Random random = new Random();

    public List<Person> generatePeopleList(int count) {
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < count; i++) {
        	String firstName = getRandomFirstName();
        	String lastName = getRandomLastName();
        	
            Person person = new Person(
            		Integer.toString(i + 1),
            		firstName,
            		lastName,
            		firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com",
            		getRandomAge());
            people.add(person);
        }
        return people;
    }

    private String getRandomFirstName() {
        String[] firstNames = { "John", "Jane", "Mike", "Lisa", "David", "Julia", "Paul", "Sophie", "Sam", "Amanda" };
        return firstNames[random.nextInt(firstNames.length)];
    }

    private String getRandomLastName() {
        String[] lastNames = { "Smith", "Johnson", "Brown", "Jones", "Davis", "Miller", "Wilson", "Moore", "Taylor", "Anderson" };
        return lastNames[random.nextInt(lastNames.length)];
    }

    private int getRandomAge() {
        return random.nextInt(50) + 20;
    }

}

