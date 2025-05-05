package com.example.luggageassistant.model;

import java.util.List;

public class ChatRequest {
    public String model;
    public List<Message> messages;
    public double temperature;

    public ChatRequest(String model, List<Message> messages, double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }
}