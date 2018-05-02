package com.home_turf.location_spike.mock_data;

import java.util.ArrayList;
import java.util.List;

public class MockGameData {
    static private List<MockGameData> games = new ArrayList<>();
    // Mock Data
    private static final List<String> SPORTS = new ArrayList<>();
    private static final String BASEBALL_STRING = "baseball.png";
    private static final String BASKETBALL_STRING = "basketball.png";
    private static final String FASTPITCH_STRING = "fastpitch.png";
    private static final String SLOWPITCH_STRING = "slowpitch.png";
    private static final String PINGPONG_STRING = "pingpong.png";
    private static final String TENNIS_STRING = "tennis.png";


    private Double longitude;
    private Double latitude;
    private String name;
    private String filename;
    private String snippet;


    MockGameData(Double lat, Double lon, String n, String f, String s) {
        this.longitude = lon;
        this.latitude = lat;
        this.name = n;
        this.filename = f;
        this.snippet = s;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public String getSnippet() {
        return snippet;
    }

    static public List<MockGameData> getGames() {
        return games;
    }
    static public List<MockGameData> newGames(int n) {
        // Clear List of Games
        if (games == null) { games = new ArrayList<>(); }
        else { games.clear(); }

        // MOCK SPORT NAMES
        SPORTS.clear();
        SPORTS.add(BASEBALL_STRING);
        SPORTS.add(BASKETBALL_STRING);
        SPORTS.add(FASTPITCH_STRING);
        SPORTS.add(SLOWPITCH_STRING);
        SPORTS.add(PINGPONG_STRING);
        SPORTS.add(TENNIS_STRING);

        // Create Random Games
        for(int i = 0; i < n; i++) {
            games.add( newGame() );
        }
        return games;
    }
    static MockGameData newGame() {
        if (games == null) { games = new ArrayList<>(); }
//            Double lat = Math.random()*(180);
//            if (lat > 90) lat -= 90;
//            Double lon = Math.random()*(360);
//            if (lon > 180) lon -= 180;
        Double lat = Math.random()*.01 + 39.12;
        Double lon = -Math.random()*.01 - 94.53;
        return new MockGameData(lat, lon, "Game #" + String.valueOf(games.size()), randomSportString(), randomSnippet());
    }
    static String randomSportString() {
        if (SPORTS.size() > 0 ) {
            int r = (int) (Math.random() * SPORTS.size());
            try {
                return SPORTS.get(r);
            } catch (Exception e) {
                return SPORTS.get(0);
            }
        } else { return ""; }
    }
    static String randomSnippet() {
        int r = (int) (Math.random() * 3);
        switch (r) {
            case 0:
                return "Beginner's looking for fun.";
            case 1:
                return "Intermediate or Expert players.";
            case 2:
                return "Senior league";
        }
        return "";
    }
}