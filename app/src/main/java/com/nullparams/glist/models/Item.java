package com.nullparams.glist.models;

public class Item {

    private String id;
    private String amount;
    private String name;
    private boolean strike;
    private String cost;

    public Item() {
        //empty constructor needed
    }

    public Item(String id, String amount, String name, boolean strike, String cost) {

        this.id = id;
        this.amount = amount;
        this.name = name;
        this.strike = strike;
        this.cost = cost;
    }

    public String getId() {
        return id;
    }

    public String getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public boolean getStrike() {
        return strike;
    }

    public String getCost() {
        return cost;
    }
}
